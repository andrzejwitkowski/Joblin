package pl.joblin.bootstrap

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import pl.joblin.adapters.web.ApiKeyAuthFilter
import pl.joblin.adapters.web.JoblinPrincipal
import pl.joblin.domain.UserRepository

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
    private val users: UserRepository,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { csrf ->
                csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                    .ignoringRequestMatchers(AntPathRequestMatcher("/ingest/**"))
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/ingest/**").permitAll()
                    .requestMatchers("/oauth2/**", "/login/**").permitAll()
                    .requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico", "/favicon.svg", "/icons.svg").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2Login { oauth ->
                oauth
                    .defaultSuccessUrl("/", true)
                    .userInfoEndpoint { u -> u.oidcUserService(oidcUserService()) }
                    .failureHandler { _, response, _ ->
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Login rejected")
                    }
            }
            .logout { logout ->
                logout.logoutUrl("/api/logout")
                    .logoutSuccessHandler { _, response, _ ->
                        response.status = HttpServletResponse.SC_OK
                        response.contentType = "application/json"
                        response.writer.write("""{"ok":true}""")
                    }
            }
            .exceptionHandling { ex ->
                ex.authenticationEntryPoint { request: HttpServletRequest, response: HttpServletResponse, _: AuthenticationException ->
                    if (request.requestURI.startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED)
                    } else {
                        response.sendRedirect("/oauth2/authorization/google")
                    }
                }
            }
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    private fun oidcUserService(): OidcUserService {
        val delegate = OidcUserService()
        return object : OidcUserService() {
            override fun loadUser(userRequest: OidcUserRequest): OidcUser {
                val oidc = delegate.loadUser(userRequest)
                val email = oidc.email?.lowercase()
                    ?: throw OAuth2AuthenticationException(OAuth2Error("email_missing"), "No email")
                val user = users.findByEmail(email)
                    ?: throw OAuth2AuthenticationException(OAuth2Error("not_seeded"), "User not seeded")
                return JoblinPrincipal(user, oidc)
            }
        }
    }
}

package pl.joblin.adapters.web

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import pl.joblin.domain.ApiKeyFormat
import pl.joblin.domain.User
import pl.joblin.domain.UserRepository

@Component
class ApiKeyAuthFilter(
    private val users: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        return !request.requestURI.startsWith("/ingest/")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val raw = extractKey(request)
        if (raw.isNullOrBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing API key")
            return
        }
        val parsed = ApiKeyFormat.parse(raw)
        if (parsed == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API key format")
            return
        }
        val (apiKeyId, secret) = parsed
        val user = users.findByApiKeyId(apiKeyId)
        if (user == null || !passwordEncoder.matches(secret, user.apiKeyHash)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API key")
            return
        }
        request.setAttribute(ATTR_USER, user)
        filterChain.doFilter(request, response)
    }

    private fun extractKey(request: HttpServletRequest): String? {
        request.getHeader("X-Api-Key")?.takeIf { it.isNotBlank() }?.let { return it }
        val auth = request.getHeader("Authorization") ?: return null
        return if (auth.startsWith("Bearer ", ignoreCase = true)) auth.substring(7).trim() else null
    }

    companion object {
        const val ATTR_USER = "joblin.apiKeyUser"

        fun userFrom(request: HttpServletRequest): User =
            request.getAttribute(ATTR_USER) as? User
                ?: throw IllegalStateException("API key user missing")
    }
}

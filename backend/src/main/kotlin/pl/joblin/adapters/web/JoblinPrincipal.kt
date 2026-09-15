package pl.joblin.adapters.web

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.oauth2.core.oidc.OidcUserInfo
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import pl.joblin.domain.User

class JoblinPrincipal(
    val user: User,
    private val delegate: OidcUser,
) : OidcUser {
    override fun getName(): String = user.id
    override fun getAttributes(): MutableMap<String, Any> = delegate.attributes
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_${user.role.name}"))
    override fun getClaims(): MutableMap<String, Any> = delegate.claims
    override fun getUserInfo(): OidcUserInfo? = delegate.userInfo
    override fun getIdToken(): OidcIdToken = delegate.idToken
}

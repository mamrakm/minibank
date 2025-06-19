package cz.ememsoft.minibank.config

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Converts JWT tokens from Keycloak into Spring Security authentication tokens.
 * 
 * Maps Keycloak realm roles to Spring Security authorities with ROLE_ prefix.
 * Extracts user information from JWT claims for security context.
 */
@Component
class JwtAuthenticationConverter : Converter<Jwt, Mono<AbstractAuthenticationToken>> {

    companion object {
        private const val REALM_ACCESS_CLAIM = "realm_access"
        private const val ROLES_CLAIM = "roles"
        private const val ROLE_PREFIX = "ROLE_"
        private const val USERNAME_CLAIM = "preferred_username"
    }

    /**
     * Converts a JWT token into a Spring Security authentication token.
     * 
     * @param jwt The JWT token from Keycloak
     * @return Mono containing the authentication token with authorities
     */
    override fun convert(jwt: Jwt): Mono<AbstractAuthenticationToken> {
        return Mono.just(
            JwtAuthenticationToken(
                jwt,
                extractAuthorities(jwt),
                extractUsername(jwt)
            )
        )
    }

    /**
     * Extracts authorities (roles) from the JWT token.
     * 
     * Reads Keycloak realm roles from the token and converts them to Spring Security authorities.
     * Adds ROLE_ prefix as required by Spring Security.
     */
    private fun extractAuthorities(jwt: Jwt): Collection<GrantedAuthority> {
        val realmAccess = jwt.getClaim<Map<String, Any>>(REALM_ACCESS_CLAIM) ?: return emptyList()
        val roles = realmAccess[ROLES_CLAIM] as? List<*> ?: return emptyList()
        
        return roles
            .filterIsInstance<String>()
            .filter { role -> isValidBankingRole(role) }
            .map { role -> SimpleGrantedAuthority("$ROLE_PREFIX$role") }
            .toList()
    }

    /**
     * Extracts the username from the JWT token.
     * 
     * Uses the preferred_username claim from Keycloak.
     */
    private fun extractUsername(jwt: Jwt): String {
        return jwt.getClaim<String>(USERNAME_CLAIM) ?: jwt.subject
    }

    /**
     * Validates that the role is a valid banking role.
     * 
     * Only allows predefined banking roles for security.
     */
    private fun isValidBankingRole(role: String): Boolean {
        return when (role) {
            "CUSTOMER",
            "PREMIUM_CUSTOMER", 
            "BANK_EMPLOYEE",
            "BANK_MANAGER",
            "ADMIN" -> true
            else -> false
        }
    }
}
package cz.ememsoft.minibank.api

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Authentication controller for testing authentication and authorization.
 * 
 * Provides endpoints to verify JWT token validation and user roles.
 */
@RestController
@RequestMapping("/auth")
class AuthController {

    /**
     * Get current user information from JWT token.
     * 
     * Returns the authenticated user's details and roles.
     */
    @GetMapping("/me")
    fun getCurrentUser(authentication: Authentication): Mono<Map<String, Any>> {
        return Mono.just(when (authentication) {
            is JwtAuthenticationToken -> {
                mapOf(
                    "username" to authentication.name,
                    "authorities" to authentication.authorities.map { it.authority },
                    "tokenAttributes" to mapOf(
                        "sub" to authentication.token.subject,
                        "email" to authentication.token.getClaim<String>("email"),
                        "preferred_username" to authentication.token.getClaim<String>("preferred_username"),
                        "realm_access" to authentication.token.getClaim<Map<String, Any>>("realm_access")
                    )
                )
            }
            else -> {
                mapOf(
                    "username" to authentication.name,
                    "authorities" to authentication.authorities.map { it.authority },
                    "type" to authentication.javaClass.simpleName
                )
            }
        })
    }

    /**
     * Test endpoint requiring CUSTOMER role.
     */
    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/customer")
    fun customerEndpoint(authentication: Authentication): Mono<Map<String, String>> {
        return Mono.just(mapOf(
            "message" to "Hello ${authentication.name}! You have CUSTOMER access.",
            "role" to "CUSTOMER"
        ))
    }

    /**
     * Test endpoint requiring ADMIN role.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    fun adminEndpoint(authentication: Authentication): Mono<Map<String, String>> {
        return Mono.just(mapOf(
            "message" to "Hello ${authentication.name}! You have ADMIN access.",
            "role" to "ADMIN"
        ))
    }

    /**
     * Public endpoint for testing - no authentication required.
     */
    @GetMapping("/public")
    fun publicEndpoint(): Mono<Map<String, String>> {
        return Mono.just(mapOf(
            "message" to "This is a public endpoint - no authentication required.",
            "status" to "accessible"
        ))
    }
}
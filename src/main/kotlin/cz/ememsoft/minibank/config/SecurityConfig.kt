package cz.ememsoft.minibank.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * Security configuration for the Minibank application.
 * 
 * Configures OAuth2 Resource Server with JWT token validation against Keycloak.
 * Implements role-based access control for banking operations.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    /**
     * Main security filter chain configuration.
     * 
     * Configures:
     * - Public endpoints (health check, API docs)
     * - Protected endpoints requiring authentication
     * - OAuth2 resource server with JWT
     * - CORS configuration
     */
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .authorizeExchange { exchanges ->
                exchanges
                    // Public endpoints - no authentication required
                    .pathMatchers(
                        "/actuator/health",
                        "/actuator/health/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/auth/public"
                    ).permitAll()
                    
                    // Client management - Admin or Bank Employee required
                    .pathMatchers("/clients/**").hasAnyRole("ADMIN", "BANK_EMPLOYEE", "BANK_MANAGER")
                    
                    // Account management - Customer or higher
                    .pathMatchers("/accounts/**").hasAnyRole("CUSTOMER", "PREMIUM_CUSTOMER", "BANK_EMPLOYEE", "BANK_MANAGER", "ADMIN")
                    
                    // Transaction operations - Customer or higher
                    .pathMatchers("/transactions/**").hasAnyRole("CUSTOMER", "PREMIUM_CUSTOMER", "BANK_EMPLOYEE", "BANK_MANAGER", "ADMIN")
                    
                    // All other endpoints require authentication
                    .anyExchange().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }
            .build()
    }

    /**
     * JWT Authentication Converter.
     * 
     * Converts JWT tokens from Keycloak into Spring Security authorities.
     * Maps Keycloak realm roles to Spring Security roles.
     */
    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        return JwtAuthenticationConverter()
    }

    /**
     * CORS configuration for cross-origin requests.
     * 
     * Allows frontend applications to access the API from different origins.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration().apply {
            allowedOriginPatterns = listOf("*")
            allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")
            allowedHeaders = listOf("*")
            allowCredentials = true
            exposedHeaders = listOf("Authorization")
        }
        
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
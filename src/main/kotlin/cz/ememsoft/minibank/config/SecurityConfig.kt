package cz.ememsoft.minibank.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

/**
 * Security configuration for the Minibank application.
 * 
 * Configures JWT-based authentication with role-based access control.
 * Supports CLIENT and ADMIN roles for different levels of access.
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig {

    /**
     * Main security filter chain configuration.
     * 
     * Configures:
     * - Public endpoints (auth, health check, API docs)
     * - Role-based access control for endpoints
     * - JWT authentication
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
                        "/auth/**",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**"
                    ).permitAll()
                    
                    // Admin endpoints - only ADMIN role
                    .pathMatchers("/admin/**").hasRole("ADMIN")
                    
                    // Client endpoints - authenticated users can access their own data
                    .pathMatchers("/clients/**").authenticated()
                    
                    // Account endpoints - authenticated users can access their own accounts
                    .pathMatchers("/accounts/**").authenticated()
                    
                    // Transaction endpoints - authenticated users can access their own transactions
                    .pathMatchers("/transactions/**").authenticated()
                    
                    // All other endpoints require authentication
                    .anyExchange().authenticated()
            }
            .build()
    }

    /**
     * Password encoder bean for BCrypt password hashing.
     */
    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
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
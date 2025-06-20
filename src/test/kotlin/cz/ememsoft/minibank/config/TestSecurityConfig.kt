package cz.ememsoft.minibank.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import reactor.core.publisher.Mono

/**
 * Test security configuration that supports multiple testing scenarios:
 * 1. Permit-all mode for basic functionality tests
 * 2. Role-based authentication for security tests
 * 3. Mock users for different permission levels
 */
@TestConfiguration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class TestSecurityConfig {

    companion object {
        // Test users with different roles
        const val TEST_CLIENT_ID = 1L
        const val TEST_CLIENT_EMAIL = "client@test.com"
        const val TEST_CLIENT_PASSWORD = "password123"
        
        const val TEST_ADMIN_ID = 2L
        const val TEST_ADMIN_EMAIL = "admin@test.com"
        const val TEST_ADMIN_PASSWORD = "admin123"
        
        const val TEST_OTHER_CLIENT_ID = 3L
        const val TEST_OTHER_CLIENT_EMAIL = "other@test.com"
        const val TEST_OTHER_CLIENT_PASSWORD = "other123"
    }

    /**
     * Security filter chain for tests - can be configured via system properties
     * to enable/disable security for different test scenarios.
     */
    @Bean
    @Primary
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // Check if security should be disabled for basic functionality tests
        val securityDisabled = System.getProperty("test.security.disabled", "false").toBoolean()
        
        return if (securityDisabled) {
            // Permit all for basic functionality tests
            http
                .authorizeExchange { exchanges ->
                    exchanges.anyExchange().permitAll()
                }
                .csrf { it.disable() }
                .build()
        } else {
            // Role-based security for security tests
            http
                .csrf { it.disable() }
                .authorizeExchange { exchanges ->
                    exchanges
                        .pathMatchers("/auth/**", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .pathMatchers("/admin/**").hasRole("ADMIN")
                        .anyExchange().authenticated()
                }
                .authenticationManager(testAuthenticationManager())
                .build()
        }
    }

    /**
     * Mock authentication manager for testing with predefined users.
     */
    @Bean
    fun testAuthenticationManager(): ReactiveAuthenticationManager {
        return ReactiveAuthenticationManager { authentication ->
            val username = authentication.name
            val password = authentication.credentials as String
            
            when (username) {
                TEST_CLIENT_EMAIL -> {
                    if (password == TEST_CLIENT_PASSWORD) {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_CLIENT"))
                        val auth = UsernamePasswordAuthenticationToken(TEST_CLIENT_ID, null, authorities)
                        Mono.just(auth as Authentication)
                    } else {
                        Mono.empty()
                    }
                }
                TEST_ADMIN_EMAIL -> {
                    if (password == TEST_ADMIN_PASSWORD) {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
                        val auth = UsernamePasswordAuthenticationToken(TEST_ADMIN_ID, null, authorities)
                        Mono.just(auth as Authentication)
                    } else {
                        Mono.empty()
                    }
                }
                TEST_OTHER_CLIENT_EMAIL -> {
                    if (password == TEST_OTHER_CLIENT_PASSWORD) {
                        val authorities = listOf(SimpleGrantedAuthority("ROLE_CLIENT"))
                        val auth = UsernamePasswordAuthenticationToken(TEST_OTHER_CLIENT_ID, null, authorities)
                        Mono.just(auth as Authentication)
                    } else {
                        Mono.empty()
                    }
                }
                else -> Mono.empty()
            }
        }
    }

    @Bean
    @Primary
    fun testPasswordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
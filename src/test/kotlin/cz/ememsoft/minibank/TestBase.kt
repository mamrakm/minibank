
package cz.ememsoft.minibank

import cz.ememsoft.minibank.config.TestSecurityConfig
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import reactor.core.publisher.Mono
import reactor.util.context.Context

/**
 * Base class for integration tests with Testcontainers and security testing utilities.
 * 
 * Provides:
 * - PostgreSQL test container setup
 * - Security context management for different user roles
 * - JWT token generation utilities
 * - Test data creation helpers
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestSecurityConfig::class)
@ActiveProfiles("test")
abstract class TestBase {

    companion object {
        @Container
        @JvmStatic
        val postgreSQLContainer = PostgreSQLContainer("postgres:17").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
            // Create schema during container startup
            withInitScript("create_test_schema.sql")
            withReuse(true)
        }

        // Test user constants
        const val TEST_CLIENT_ID = TestSecurityConfig.TEST_CLIENT_ID
        const val TEST_CLIENT_KEYCLOAK_ID = "test-client-keycloak-id"
        const val TEST_ADMIN_ID = TestSecurityConfig.TEST_ADMIN_ID
        const val TEST_ADMIN_KEYCLOAK_ID = "test-admin-keycloak-id"

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/testdb"
            }
            registry.add("spring.r2dbc.username") { "testuser" }
            registry.add("spring.r2dbc.password") { "testpass" }

            // Configure Liquibase for schema migrations (if needed)
            registry.add("spring.liquibase.url") {
                "jdbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/testdb"
            }
            registry.add("spring.liquibase.user") { "testuser" }
            registry.add("spring.liquibase.password") { "testpass" }
            registry.add("spring.liquibase.default-schema") { "bank" }
        }
    }

    /**
     * Security testing utilities
     */

    /**
     * Creates an authentication token for a client user.
     */
    fun createClientAuthentication(clientId: Long = TEST_CLIENT_ID): Authentication {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_CLIENT"))
        return UsernamePasswordAuthenticationToken(clientId, null, authorities)
    }

    /**
     * Creates an authentication token for an admin user.
     */
    fun createAdminAuthentication(adminId: Long = TEST_ADMIN_ID): Authentication {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
        return UsernamePasswordAuthenticationToken(adminId, null, authorities)
    }

    /**
     * Creates a security context for a client user.
     */
    fun createClientSecurityContext(clientId: Long = TEST_CLIENT_ID): SecurityContext {
        val auth = createClientAuthentication(clientId)
        return SecurityContextImpl(auth)
    }

    /**
     * Creates a security context for an admin user.
     */
    fun createAdminSecurityContext(adminId: Long = TEST_ADMIN_ID): SecurityContext {
        val auth = createAdminAuthentication(adminId)
        return SecurityContextImpl(auth)
    }

    /**
     * Creates a reactive context with client authentication.
     */
    fun withClientContext(clientId: Long = TEST_CLIENT_ID): Context {
        return Context.of("SECURITY_CONTEXT_KEY", Mono.just(createClientSecurityContext(clientId)))
    }

    /**
     * Creates a reactive context with admin authentication.
     */
    fun withAdminContext(adminId: Long = TEST_ADMIN_ID): Context {
        return Context.of("SECURITY_CONTEXT_KEY", Mono.just(createAdminSecurityContext(adminId)))
    }

    /**
     * Enables security for tests. Call this in tests that need to verify security behavior.
     */
    fun enableSecurity() {
        System.setProperty("test.security.disabled", "false")
    }

    /**
     * Disables security for tests. Call this in tests that only test business logic.
     */
    fun disableSecurity() {
        System.setProperty("test.security.disabled", "true")
    }

    /**
     * Test data creation helpers
     */
    
    /**
     * Creates test client entity data.
     */
    fun createTestClientData(
        id: Long = 1L,
        keycloakUserId: String = TEST_CLIENT_KEYCLOAK_ID,
        email: String = "test@example.com",
        role: UserRoleEnum = UserRoleEnum.CLIENT
    ) = mapOf(
        "id" to id,
        "keycloak_user_id" to keycloakUserId,
        "first_name" to "Test",
        "last_name" to "User",
        "email" to email,
        "role" to role.ordinal,
        "phone_number" to "+1234567890",
        "address" to "123 Test St",
        "status" to 0 // ACTIVE
    )
}

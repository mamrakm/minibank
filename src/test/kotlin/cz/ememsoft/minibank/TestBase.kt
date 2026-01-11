package cz.ememsoft.minibank

import dasniko.testcontainers.keycloak.KeycloakContainer
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

/**
 * Abstract base class for all integration tests. It sets up and configures
 * Testcontainers for PostgreSQL and Keycloak, providing a realistic environment
 * for end-to-end testing.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class TestBase {

    companion object {
        @Container
        @JvmStatic
        val postgreSQLContainer = PostgreSQLContainer("postgres:17").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
            withInitScript("create_test_schema.sql") // Ensures the 'bank' schema is created
            withReuse(true)
        }

        @Container
        @JvmStatic
        val keycloakContainer: KeycloakContainer = KeycloakContainer("quay.io/keycloak/keycloak:latest")
            .withRealmImportFile("config/keycloak/minibank-realm.json")
            .withReuse(true)

        /**
         * Dynamically sets the application properties at runtime to connect
         * to the Testcontainers instances.
         */
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // PostgreSQL properties
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/${postgreSQLContainer.databaseName}"
            }
            registry.add("spring.r2dbc.username") { postgreSQLContainer.username }
            registry.add("spring.r2dbc.password") { postgreSQLContainer.password }

            // Liquibase properties (uses JDBC)
            registry.add("spring.liquibase.url") { postgreSQLContainer.jdbcUrl }
            registry.add("spring.liquibase.user") { postgreSQLContainer.username }
            registry.add("spring.liquibase.password") { postgreSQLContainer.password }
            registry.add("spring.liquibase.default-schema") { "bank" }

            // Keycloak properties for the OAuth2 Resource Server
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") {
                "${keycloakContainer.authServerUrl}realms/minibank"
            }

            val keycloakBaseUrl = keycloakContainer.authServerUrl.removeSuffix("/")
            registry.add("keycloak.base-url") { keycloakBaseUrl }
            registry.add("keycloak.realm") { "minibank" }
            registry.add("keycloak.client-id") { "minibank-api" }
            registry.add("keycloak.client-secret") { "minibank-secret" }
            registry.add("keycloak.admin-realm") { "master" }
        }
    }
}

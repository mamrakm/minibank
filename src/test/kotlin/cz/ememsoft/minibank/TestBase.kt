
package cz.ememsoft.minibank

import cz.ememsoft.minibank.config.TestSecurityConfig
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig::class)
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
}
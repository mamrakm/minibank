package cz.ememsoft.minibank

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class TestBase {

    // Declare container as 'object' to ensure it's a singleton
    object ContainerConfig {
        @Container
        val postgreSQLContainer = PostgreSQLContainer("postgres:latest").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
            withReuse(true)
        }.also { it.start() } // Ensure it starts before Spring loads properties
    }

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") {
                "r2dbc:postgresql://${ContainerConfig.postgreSQLContainer.host}:${ContainerConfig.postgreSQLContainer.firstMappedPort}/testdb"
            }
            registry.add("spring.r2dbc.username") { "testuser" }
            registry.add("spring.r2dbc.password") { "testpass" }
        }
    }
}

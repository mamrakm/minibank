package cz.ememsoft.minibank

import cz.ememsoft.minibank.api.dto.request.CreateClientRequestDto
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class ClientTests @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val accountRepository: AccountRepository,
    private val clientRepository: ClientRepository,
) {
    companion object {
        @Container
        val postgreSQLContainer = PostgreSQLContainer("postgres:latest").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
            withReuse(true)
            withInitScript("init-db.sql")
        }

        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.r2dbc.url") { "r2dbc:postgresql://${postgreSQLContainer.host}:${postgreSQLContainer.firstMappedPort}/testdb" }
            registry.add("spring.r2dbc.username") { "testuser" }
            registry.add("spring.r2dbc.password") { "testpass" }
        }
    }

    @BeforeEach
    fun setup() {
        clientRepository.deleteAll().block() // Clean DB before each test
    }

    @Test
    fun `should create a client successfully`() {
        val request = CreateClientRequestDto(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phone = "123456789",
            address = "123 Street"
        )

        val response = webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.firstName").isEqualTo("John")
            .jsonPath("$.lastName").isEqualTo("Doe")
            .jsonPath("$.email").isEqualTo("john.doe@example.com")
            .jsonPath("$.phone").isEqualTo("123456789")
            .jsonPath("$.address").isEqualTo("123 Street")
            .returnResult()

        val secondRequest = CreateClientRequestDto(
            firstName = "Benjamin",
            lastName = "Sisko",
            email = "bennyrussel@starfleet.com",
            phone = "123456789",
            address = "DS9 Street"
        )
        val secondResponse = webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(secondRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isNotEmpty
            .jsonPath("$.firstName").isEqualTo("Benjamin")
            .jsonPath("$.lastName").isEqualTo("Sisko")
            .jsonPath("$.email").isEqualTo("bennyrussel@starfleet.com")
            .jsonPath("$.phone").isEqualTo("123456789")
            .jsonPath("$.address").isEqualTo("DS9 Street")
            .returnResult()


        println("Response Body: ${response.responseBody?.decodeToString()}")
        println("Response Body: ${secondResponse.responseBody?.decodeToString()}")
    }
}

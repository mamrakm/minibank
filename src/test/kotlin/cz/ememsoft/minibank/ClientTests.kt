package cz.ememsoft.minibank

import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Testcontainers
class ClientTests @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val accountRepository: AccountRepository,
    private val clientRepository: ClientRepository
) : TestBase() {

    @BeforeEach
    fun setup() {
        clientRepository.deleteAll().block() // Clean DB before each test
    }

    /*    @Test
        fun `should create a client successfully`() {
            val request = CreateClientRequest(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                phone = "123456789",
                address = "123 Street"
            )

            webTestClient.post()
                .uri("/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.id").isNotEmpty
                .jsonPath("$.firstName").isEqualTo("John")
                .jsonPath("$.email").isEqualTo("john.doe@example.com")
        }*/
}

package cz.ememsoft.minibank

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Integration tests for client-related REST operations.
 *
 * This class uses a PostgreSQL Testcontainer for an isolated test environment.
 * Tests cover creating, retrieving, updating, deleting, and searching clients.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ClientTests @Autowired constructor(
    private val webTestClient: WebTestClient,
    private val accountRepository: AccountRepository,
    private val clientRepository: ClientRepository,
) : TestBase() {

    @BeforeEach
    fun setup() {
        println("Checking existing tables...")
        clientRepository.findAll().collectList().block()?.forEach { println(it) }
        println("Deleting all clients now...")
        clientRepository.deleteAll().block()
    }

    /**
     * Tests the creation of new clients.
     */
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

    /**
     * Tests retrieving a client by its ID.
     */
    @Test
    fun `should get client by id successfully`() {
        // First, create a client.
        val request = CreateClientRequestDto(
            firstName = "Alice",
            lastName = "Smith",
            email = "alice.smith@example.com",
            phone = "987654321",
            address = "456 Avenue"
        )

        val clientResponse = webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated
            .expectBody(CreateClientResponseDto::class.java)
            .returnResult().responseBody!!

        val clientId = clientResponse.id

        // Retrieve the client by id.
        webTestClient.get()
            .uri("/clients/$clientId")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(clientId)
            .jsonPath("$.firstName").isEqualTo("Alice")
            .jsonPath("$.lastName").isEqualTo("Smith")
            .jsonPath("$.email").isEqualTo("alice.smith@example.com")
            .jsonPath("$.phone").isEqualTo("987654321")
            .jsonPath("$.address").isEqualTo("456 Avenue")
    }

    /**
     * Tests retrieving all clients.
     */
    @Test
    fun `should get all clients successfully`() {
        // Create multiple clients.
        val request1 = CreateClientRequestDto(
            firstName = "Bob",
            lastName = "Brown",
            email = "bob.brown@example.com",
            phone = "111111111",
            address = "789 Road"
        )
        val request2 = CreateClientRequestDto(
            firstName = "Charlie",
            lastName = "Davis",
            email = "charlie.davis@example.com",
            phone = "222222222",
            address = "321 Boulevard"
        )

        webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request1)
            .exchange()
            .expectStatus().isCreated

        webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request2)
            .exchange()
            .expectStatus().isCreated

        // Retrieve all clients.
        webTestClient.get()
            .uri("/clients/search-by-name/Bob")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].firstName").isEqualTo("Bob")
    }

    /**
     * Tests updating an existing client's information.
     */
    @Test
    fun `should update client successfully`() {
        // Create a client.
        val createRequest = ClientDto(
            id = 1,
            firstName = "David",
            lastName = "Evans",
            email = "david.evans@example.com",
            phone = "333333333",
            address = "654 Lane"
        )

        val createResponse = webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(CreateClientResponseDto::class.java)
            .returnResult().responseBody!!

        val clientId = createResponse.id

        // Prepare an update request with new details.
        val updateRequest = ClientDto(
            id = clientId,
            firstName = "Rich",
            lastName = "Evans",
            email = "rich.evans@example.com",
            phone = "333333333",
            address = "654 Lane Updated"
        )

        // Update the client.
        webTestClient.put()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(updateRequest)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.id").isEqualTo(clientId)
            .jsonPath("$.email").isEqualTo("rich.evans@example.com")
            .jsonPath("$.address").isEqualTo("654 Lane Updated")
    }

    /**
     * Tests deleting a client.
     */
    @Test
    fun `should delete client successfully`() {
        // Create a client to delete.
        val createRequest = CreateClientRequestDto(
            firstName = "Eve",
            lastName = "Foster",
            email = "eve.foster@example.com",
            phone = "444444444",
            address = "987 Court"
        )

        val createResponse = webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createRequest)
            .exchange()
            .expectStatus().isCreated
            .expectBody(CreateClientResponseDto::class.java)
            .returnResult().responseBody!!

        val clientId = createResponse.id

        // Delete the client.
        webTestClient.delete()
            .uri("/clients/$clientId")
            .exchange()
            .expectStatus().isNoContent

        print("Client ID: $clientId")
        // Verify deletion by attempting to retrieve the deleted client.
        webTestClient.get()
            .uri("/clients/$clientId")
            .exchange()
            .expectStatus().isNotFound
    }

    /**
     * Tests searching for clients by their first name.
     */
    @Test
    fun `should search clients by first name successfully`() {
        // Create clients with the same first name.
        val request1 = CreateClientRequestDto(
            firstName = "Frank",
            lastName = "Green",
            email = "frank.green1@example.com",
            phone = "555555555",
            address = "111 Plaza"
        )
        val request2 = CreateClientRequestDto(
            firstName = "Frank",
            lastName = "Hall",
            email = "frank.hall@example.com",
            phone = "666666666",
            address = "222 Plaza"
        )
        val request3 = CreateClientRequestDto(
            firstName = "George",
            lastName = "Ivory",
            email = "george.ivory@example.com",
            phone = "777777777",
            address = "333 Plaza"
        )

        webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request1)
            .exchange()
            .expectStatus().isCreated

        webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request2)
            .exchange()
            .expectStatus().isCreated

        webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request3)
            .exchange()
            .expectStatus().isCreated

        // Search for clients by first name "Frank".
        webTestClient.get()
            .uri("/clients/search-by-name/Frank")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
            .jsonPath("$[0].firstName").isEqualTo("Frank")
            .jsonPath("$[1].firstName").isEqualTo("Frank")
    }

    /**
     * Tests searching for a client by email.
     */
    @Test
    fun `should search client by email successfully`() {
        // Create a client.
        val createRequest = CreateClientRequestDto(
            firstName = "Helen",
            lastName = "Jones",
            email = "helen.jones@example.com",
            phone = "888888888",
            address = "444 Street"
        )

        webTestClient.post()
            .uri("/clients")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(createRequest)
            .exchange()
            .expectStatus().isCreated

        // Search for the client by email.
        webTestClient.get()
            .uri("/clients/search-by-email/helen.jones@example.com")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.firstName").isEqualTo("Helen")
            .jsonPath("$.lastName").isEqualTo("Jones")
            .jsonPath("$.email").isEqualTo("helen.jones@example.com")
            .jsonPath("$.phone").isEqualTo("888888888")
            .jsonPath("$.address").isEqualTo("444 Street")
    }
}

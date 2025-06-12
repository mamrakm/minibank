package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.service.ClientService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ClientControllerUnitTest {

    @Mock
    private lateinit var clientService: ClientService

    @InjectMocks
    private lateinit var clientController: ClientController

    private lateinit var clientDto: ClientDto
    private lateinit var createClientRequest: CreateClientRequestDto
    private lateinit var createClientResponse: CreateClientResponseDto

    @BeforeEach
    fun setUp() {
        val sampleUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        clientDto = ClientDto(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid
        )

        createClientRequest = CreateClientRequestDto(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )

        createClientResponse = CreateClientResponseDto(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid
        )
    }

    @Test
    fun `getClient should return client when found`() {
        // Arrange
        given(clientService.getClient(1L)).willReturn(Mono.just(clientDto))

        // Act & Assert
        StepVerifier.create(clientController.getClient(1L))
            .expectNext(clientDto)
            .verifyComplete()

        verify(clientService).getClient(1L)
    }

    @Test
    fun `getClient should propagate ClientNotFoundException`() {
        // Arrange
        val exception = ClientNotFoundException("Client with ID 999 not found")
        given(clientService.getClient(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.getClient(999L))
            .expectErrorMatches {
                it is ClientNotFoundException && it.message == "Client with ID 999 not found"
            }
            .verify()

        verify(clientService).getClient(999L)
    }

    @Test
    fun `getAllClients should return all clients`() {
        // Arrange
        val secondClient = clientDto.copy(id = 2L, email = "jane.doe@example.com")
        given(clientService.getAllClients()).willReturn(Flux.just(clientDto, secondClient))

        // Act & Assert
        StepVerifier.create(clientController.getAllClients())
            .expectNext(clientDto)
            .expectNext(secondClient)
            .verifyComplete()

        verify(clientService).getAllClients()
    }

    @Test
    fun `getAllClients should handle empty result`() {
        // Arrange
        given(clientService.getAllClients()).willReturn(Flux.empty())

        // Act & Assert
        StepVerifier.create(clientController.getAllClients())
            .verifyComplete()

        verify(clientService).getAllClients()
    }

    @Test
    fun `createClient should create client successfully`() {
        // Arrange
        given(clientService.createClient(createClientRequest))
            .willReturn(Mono.just(createClientResponse))

        // Act & Assert
        StepVerifier.create(clientController.createClient(createClientRequest))
            .expectNext(createClientResponse)
            .verifyComplete()

        verify(clientService).createClient(createClientRequest)
    }

    @Test
    fun `createClient should propagate DuplicateClientException`() {
        // Arrange
        val exception = DuplicateClientException("Client with email john.doe@example.com already exists")
        given(clientService.createClient(createClientRequest))
            .willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.createClient(createClientRequest))
            .expectError(DuplicateClientException::class.java)
            .verify()

        verify(clientService).createClient(createClientRequest)
    }

    @Test
    fun `updateClient should update client successfully`() {
        // Arrange
        val updatedClient = clientDto.copy(firstName = "Jane")
        given(clientService.updateClient(1L, updatedClient))
            .willReturn(Mono.just(updatedClient))

        // Act & Assert
        StepVerifier.create(clientController.updateClient(1L, updatedClient))
            .expectNext(updatedClient)
            .verifyComplete()

        verify(clientService).updateClient(1L, updatedClient)
    }

    @Test
    fun `updateClient should propagate ClientNotFoundException`() {
        // Arrange
        val exception = ClientNotFoundException("Client with ID 999 not found")
        given(clientService.updateClient(999L, clientDto))
            .willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.updateClient(999L, clientDto))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientService).updateClient(999L, clientDto)
    }

    @Test
    fun `deleteClient should delete client successfully`() {
        // Arrange
        given(clientService.deleteClient(1L)).willReturn(Mono.empty())

        // Act & Assert
        StepVerifier.create(clientController.deleteClient(1L))
            .verifyComplete()

        verify(clientService).deleteClient(1L)
    }

    @Test
    fun `deleteClient should propagate ClientNotFoundException`() {
        // Arrange
        val exception = ClientNotFoundException("Client with ID 999 not found")
        given(clientService.deleteClient(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.deleteClient(999L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientService).deleteClient(999L)
    }

    @Test
    fun `findClientsByFirstName should return matching clients`() {
        // Arrange
        given(clientService.findClientsByFirstName("John"))
            .willReturn(Flux.just(clientDto))

        // Act & Assert
        StepVerifier.create(clientController.findClientsByFirstName("John"))
            .expectNext(clientDto)
            .verifyComplete()

        verify(clientService).findClientsByFirstName("John")
    }

    @Test
    fun `findClientByEmail should return client when found`() {
        // Arrange
        given(clientService.findClientByEmail("john.doe@example.com"))
            .willReturn(Mono.just(clientDto))

        // Act & Assert
        StepVerifier.create(clientController.findClientByEmail("john.doe@example.com"))
            .expectNext(clientDto)
            .verifyComplete()

        verify(clientService).findClientByEmail("john.doe@example.com")
    }

    @Test
    fun `findClientByEmail should handle empty result`() {
        // Arrange
        given(clientService.findClientByEmail("notfound@example.com"))
            .willReturn(Mono.empty())

        // Act & Assert
        StepVerifier.create(clientController.findClientByEmail("notfound@example.com"))
            .verifyComplete()

        verify(clientService).findClientByEmail("notfound@example.com")
    }
}
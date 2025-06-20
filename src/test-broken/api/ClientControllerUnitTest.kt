package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.ClientUnauthorizedException
import cz.ememsoft.minibank.service.ClientService
import cz.ememsoft.minibank.service.SecurityService
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
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class ClientControllerUnitTest {

    @Mock
    private lateinit var clientService: ClientService

    @Mock
    private lateinit var securityService: SecurityService

    @InjectMocks
    private lateinit var clientController: ClientController

    private lateinit var clientDto: ClientDto
    private lateinit var adminClientDto: ClientDto
    private lateinit var clientEntity: ClientEntity

    @BeforeEach
    fun setUp() {
        val sampleUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val now = LocalDateTime.now()

        clientDto = ClientDto(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid,
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = now
        )

        adminClientDto = ClientDto(
            id = 2L,
            firstName = "Admin",
            lastName = "User",
            email = "admin@example.com",
            role = UserRoleEnum.ADMIN,
            phoneNumber = "+1987654321",
            address = "456 Admin St",
            dateOfBirth = LocalDate.of(1985, 5, 5),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = now
        )

        clientEntity = ClientEntity(
            id = 2L,
            firstName = "Admin",
            lastName = "User",
            email = "admin@example.com",
            passwordHash = "\$2a\$10\$dummyHashForTesting",
            role = UserRoleEnum.ADMIN,
            phoneNumber = "+1987654321",
            address = "456 Admin St",
            dateOfBirth = LocalDate.of(1985, 5, 5),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = now,
            lastLoginAt = null
        )
    }

    // ==================== Security and Access Control Tests ====================

    @Test
    fun `getClient should return client when authorized`() {
        // Arrange
        given(securityService.verifyClientAccess(1L)).willReturn(Mono.empty())
        given(clientService.getClient(1L)).willReturn(Mono.just(clientDto))

        // Act & Assert
        StepVerifier.create(clientController.getClient(1L))
            .expectNext(clientDto)
            .verifyComplete()

        verify(securityService).verifyClientAccess(1L)
        verify(clientService).getClient(1L)
    }

    @Test
    fun `getClient should reject unauthorized access`() {
        // Arrange
        val exception = ClientUnauthorizedException("Access denied: You can only access your own data")
        given(securityService.verifyClientAccess(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.getClient(999L))
            .expectError(ClientUnauthorizedException::class.java)
            .verify()

        verify(securityService).verifyClientAccess(999L)
    }

    @Test
    fun `getClient should propagate ClientNotFoundException when authorized`() {
        // Arrange
        val exception = ClientNotFoundException("Client with ID 999 not found")
        given(securityService.verifyClientAccess(999L)).willReturn(Mono.empty())
        given(clientService.getClient(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.getClient(999L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(securityService).verifyClientAccess(999L)
        verify(clientService).getClient(999L)
    }

    @Test
    fun `getAllClients should return all clients for admin user`() {
        // Arrange
        given(securityService.getAuthenticatedClient()).willReturn(Mono.just(clientEntity))
        given(clientService.getAllClients()).willReturn(Flux.just(clientDto, adminClientDto))

        // Act & Assert
        StepVerifier.create(clientController.getAllClients())
            .expectNext(clientDto)
            .expectNext(adminClientDto) 
            .verifyComplete()

        verify(securityService).getAuthenticatedClient()
        verify(clientService).getAllClients()
    }

    @Test
    fun `getAllClients should return only own client data for regular user`() {
        // Arrange
        val regularClientEntity = clientEntity.copy(id = 1L, role = UserRoleEnum.CLIENT)
        given(securityService.getAuthenticatedClient()).willReturn(Mono.just(regularClientEntity))
        given(clientService.getClient(1L)).willReturn(Mono.just(clientDto))

        // Act & Assert
        StepVerifier.create(clientController.getAllClients())
            .expectNext(clientDto)
            .verifyComplete()

        verify(securityService).getAuthenticatedClient()
        verify(clientService).getClient(1L)
    }

    @Test
    fun `updateClient should update client when authorized`() {
        // Arrange
        val updatedClient = clientDto.copy(firstName = "Jane")
        given(securityService.verifyClientAccess(1L)).willReturn(Mono.empty())
        given(clientService.updateClient(1L, updatedClient)).willReturn(Mono.just(updatedClient))

        // Act & Assert
        StepVerifier.create(clientController.updateClient(1L, updatedClient))
            .expectNext(updatedClient)
            .verifyComplete()

        verify(securityService).verifyClientAccess(1L)
        verify(clientService).updateClient(1L, updatedClient)
    }

    @Test
    fun `updateClient should reject unauthorized update`() {
        // Arrange
        val updatedClient = clientDto.copy(firstName = "Jane")
        val exception = ClientUnauthorizedException("Access denied: You can only access your own data")
        given(securityService.verifyClientAccess(1L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.updateClient(1L, updatedClient))
            .expectError(ClientUnauthorizedException::class.java)
            .verify()

        verify(securityService).verifyClientAccess(1L)
    }

    @Test
    fun `updateClient should propagate ClientNotFoundException when authorized`() {
        // Arrange
        val exception = ClientNotFoundException("Client with ID 999 not found")
        given(securityService.verifyClientAccess(999L)).willReturn(Mono.empty())
        given(clientService.updateClient(999L, clientDto)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.updateClient(999L, clientDto))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(securityService).verifyClientAccess(999L)
        verify(clientService).updateClient(999L, clientDto)
    }

    @Test
    fun `deleteClient should delete client when authorized`() {
        // Arrange
        given(securityService.verifyClientAccess(1L)).willReturn(Mono.empty())
        given(clientService.deleteClient(1L)).willReturn(Mono.empty())

        // Act & Assert
        StepVerifier.create(clientController.deleteClient(1L))
            .verifyComplete()

        verify(securityService).verifyClientAccess(1L)
        verify(clientService).deleteClient(1L)
    }

    @Test
    fun `deleteClient should reject unauthorized deletion`() {
        // Arrange
        val exception = ClientUnauthorizedException("Access denied: You can only access your own data")
        given(securityService.verifyClientAccess(1L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.deleteClient(1L))
            .expectError(ClientUnauthorizedException::class.java)
            .verify()

        verify(securityService).verifyClientAccess(1L)
    }

    @Test
    fun `deleteClient should propagate ClientNotFoundException when authorized`() {
        // Arrange
        val exception = ClientNotFoundException("Client with ID 999 not found")
        given(securityService.verifyClientAccess(999L)).willReturn(Mono.empty())
        given(clientService.deleteClient(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(clientController.deleteClient(999L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(securityService).verifyClientAccess(999L)
        verify(clientService).deleteClient(999L)
    }
}
package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import cz.ememsoft.minibank.repository.findByStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalDateTime

class ClientServiceTest {

    private val clientRepository: ClientRepository = mock()
    private val clientMapper: ClientMapper = mock()
    private lateinit var clientService: ClientService

    private val clientEntity = ClientEntity(
        id = 1L,
        keycloakUserId = "keycloak-123",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        role = UserRoleEnum.CLIENT,
        phoneNumber = "+1234567890",
        address = "123 Main St",
        dateOfBirth = LocalDate.of(1990, 1, 1),
        status = ClientStatusEnum.ACTIVE,
        createdAt = LocalDateTime.now()
    )

    private val clientDto = ClientDto(
        id = 1L,
        keycloakUserId = "keycloak-123",
        firstName = "John",
        lastName = "Doe",
        email = "john.doe@example.com",
        role = UserRoleEnum.CLIENT,
        phoneNumber = "+1234567890",
        address = "123 Main St",
        dateOfBirth = LocalDate.of(1990, 1, 1),
        status = ClientStatusEnum.ACTIVE,
        createdAt = LocalDateTime.now()
    )

    private val createProfileRequest = CreateClientProfileRequestDto(
        keycloakUserId = "keycloak-456",
        firstName = "Jane",
        lastName = "Smith",
        email = "jane.smith@example.com",
        role = UserRoleEnum.CLIENT,
        phoneNumber = "+0987654321",
        address = "456 Oak St",
        dateOfBirth = LocalDate.of(1985, 5, 15)
    )

    @BeforeEach
    fun setup() {
        clientService = ClientServiceImpl(clientRepository, clientMapper)
    }

    @Test
    fun `getClient should return client when found and active`() {
        // Given
        whenever(clientRepository.findActiveById(1L)).thenReturn(Mono.just(clientEntity))
        whenever(clientMapper.entityToDto(clientEntity)).thenReturn(clientDto)

        // When & Then
        StepVerifier.create(clientService.getClient(1L))
            .expectNext(clientDto)
            .verifyComplete()

        verify(clientRepository).findActiveById(1L)
        verify(clientMapper).entityToDto(clientEntity)
    }

    @Test
    fun `getClient should fail when client not found`() {
        // Given
        whenever(clientRepository.findActiveById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.getClient(999L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findActiveById(999L)
    }

    @Test
    fun `getClientByKeycloakId should return client when found`() {
        // Given
        val keycloakId = "keycloak-123"
        whenever(clientRepository.findByKeycloakUserId(keycloakId)).thenReturn(Mono.just(clientEntity))
        whenever(clientMapper.entityToDto(clientEntity)).thenReturn(clientDto)

        // When & Then
        StepVerifier.create(clientService.getClientByKeycloakId(keycloakId))
            .expectNext(clientDto)
            .verifyComplete()

        verify(clientRepository).findByKeycloakUserId(keycloakId)
        verify(clientMapper).entityToDto(clientEntity)
    }

    @Test
    fun `getClientByKeycloakId should fail when client not found`() {
        // Given
        val keycloakId = "non-existent"
        whenever(clientRepository.findByKeycloakUserId(keycloakId)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.getClientByKeycloakId(keycloakId))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findByKeycloakUserId(keycloakId)
    }

    @Test
    fun `getAllClients should return all active clients`() {
        // Given
        val clients = listOf(clientEntity, clientEntity.copy(id = 2L, email = "client2@example.com"))
        whenever(clientRepository.findAllActive()).thenReturn(Flux.fromIterable(clients))
        whenever(clientMapper.entityToDto(any())).thenReturn(clientDto)

        // When & Then
        StepVerifier.create(clientService.getAllClients())
            .expectNextCount(2)
            .verifyComplete()

        verify(clientRepository).findAllActive()
    }

    @Test
    fun `updateClient should update existing client`() {
        // Given
        val updatedDto = clientDto.copy(firstName = "Johnny")
        val updatedEntity = clientEntity.copy(firstName = "Johnny")
        whenever(clientRepository.findById(1L)).thenReturn(Mono.just(clientEntity))
        whenever(clientMapper.mergeForUpdate(updatedDto, clientEntity)).thenReturn(updatedEntity)
        whenever(clientRepository.save(updatedEntity)).thenReturn(Mono.just(updatedEntity))
        whenever(clientMapper.entityToDto(updatedEntity)).thenReturn(updatedDto)

        // When & Then
        StepVerifier.create(clientService.updateClient(1L, updatedDto))
            .expectNext(updatedDto)
            .verifyComplete()

        verify(clientRepository).findById(1L)
        verify(clientMapper).mergeForUpdate(updatedDto, clientEntity)
        verify(clientRepository).save(updatedEntity)
        verify(clientMapper).entityToDto(updatedEntity)
    }

    @Test
    fun `updateClient should fail when client not found`() {
        // Given
        whenever(clientRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.updateClient(999L, clientDto))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findById(999L)
    }

    @Test
    fun `deleteClient should soft delete existing client`() {
        // Given
        val inactiveClient = clientEntity.copy(status = ClientStatusEnum.INACTIVE)
        whenever(clientRepository.findById(1L)).thenReturn(Mono.just(clientEntity))
        whenever(clientRepository.save(inactiveClient)).thenReturn(Mono.just(inactiveClient))

        // When & Then
        StepVerifier.create(clientService.deleteClient(1L))
            .verifyComplete()

        verify(clientRepository).findById(1L)
        verify(clientRepository).save(inactiveClient)
    }

    @Test
    fun `deleteClient should fail when client not found`() {
        // Given
        whenever(clientRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.deleteClient(999L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findById(999L)
    }

    @Test
    fun `createClientProfile should create new client profile`() {
        // Given
        val newEntity = clientEntity.copy(id = 0L, keycloakUserId = "keycloak-456")
        val savedEntity = newEntity.copy(id = 2L)
        val resultDto = clientDto.copy(id = 2L, keycloakUserId = "keycloak-456")

        whenever(clientRepository.findByKeycloakUserId("keycloak-456")).thenReturn(Mono.empty())
        whenever(clientMapper.createProfileRequestToEntity(createProfileRequest)).thenReturn(newEntity)
        whenever(clientRepository.save(newEntity)).thenReturn(Mono.just(savedEntity))
        whenever(clientMapper.entityToDto(savedEntity)).thenReturn(resultDto)

        // When & Then
        StepVerifier.create(clientService.createClientProfile(createProfileRequest))
            .expectNext(resultDto)
            .verifyComplete()

        verify(clientRepository).findByKeycloakUserId("keycloak-456")
        verify(clientMapper).createProfileRequestToEntity(createProfileRequest)
        verify(clientRepository).save(newEntity)
        verify(clientMapper).entityToDto(savedEntity)
    }

    @Test
    fun `createClientProfile should fail when client already exists`() {
        // Given
        whenever(clientRepository.findByKeycloakUserId("keycloak-456")).thenReturn(Mono.just(clientEntity))

        // When & Then
        StepVerifier.create(clientService.createClientProfile(createProfileRequest))
            .expectError(DuplicateClientException::class.java)
            .verify()

        verify(clientRepository).findByKeycloakUserId("keycloak-456")
    }

    @Test
    fun `getClientByIdAdmin should return client regardless of status`() {
        // Given
        val inactiveClient = clientEntity.copy(status = ClientStatusEnum.INACTIVE)
        val inactiveDto = clientDto.copy(status = ClientStatusEnum.INACTIVE)
        whenever(clientRepository.findById(1L)).thenReturn(Mono.just(inactiveClient))
        whenever(clientMapper.entityToDto(inactiveClient)).thenReturn(inactiveDto)

        // When & Then
        StepVerifier.create(clientService.getClientByIdAdmin(1L))
            .expectNext(inactiveDto)
            .verifyComplete()

        verify(clientRepository).findById(1L)
        verify(clientMapper).entityToDto(inactiveClient)
    }

    @Test
    fun `getAllClientsAdmin should return all clients regardless of status`() {
        // Given
        val activeClient = clientEntity
        val inactiveClient = clientEntity.copy(id = 2L, status = ClientStatusEnum.INACTIVE)
        val clients = listOf(activeClient, inactiveClient)
        whenever(clientRepository.findAll()).thenReturn(Flux.fromIterable(clients))
        whenever(clientMapper.entityToDto(any())).thenReturn(clientDto)

        // When & Then
        StepVerifier.create(clientService.getAllClientsAdmin())
            .expectNextCount(2)
            .verifyComplete()

        verify(clientRepository).findAll()
    }

    @Test
    fun `getClientsByStatus should return clients with specific status`() {
        // Given
        val inactiveClients = listOf(clientEntity.copy(status = ClientStatusEnum.INACTIVE))
        whenever(clientRepository.findByStatus(ClientStatusEnum.INACTIVE)).thenReturn(Flux.fromIterable(inactiveClients))
        whenever(clientMapper.entityToDto(any())).thenReturn(clientDto.copy(status = ClientStatusEnum.INACTIVE))

        // When & Then
        StepVerifier.create(clientService.getClientsByStatus(ClientStatusEnum.INACTIVE))
            .expectNextCount(1)
            .verifyComplete()

        verify(clientRepository).findByStatus(ClientStatusEnum.INACTIVE)
    }

    @Test
    fun `updateClientStatus should update client status`() {
        // Given
        val updatedClient = clientEntity.copy(status = ClientStatusEnum.INACTIVE)
        val updatedDto = clientDto.copy(status = ClientStatusEnum.INACTIVE)
        whenever(clientRepository.findById(1L)).thenReturn(Mono.just(clientEntity))
        whenever(clientRepository.save(updatedClient)).thenReturn(Mono.just(updatedClient))
        whenever(clientMapper.entityToDto(updatedClient)).thenReturn(updatedDto)

        // When & Then
        StepVerifier.create(clientService.updateClientStatus(1L, ClientStatusEnum.INACTIVE))
            .expectNext(updatedDto)
            .verifyComplete()

        verify(clientRepository).findById(1L)
        verify(clientRepository).save(updatedClient)
        verify(clientMapper).entityToDto(updatedClient)
    }

    @Test
    fun `updateClientStatus should fail when client not found`() {
        // Given
        whenever(clientRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.updateClientStatus(999L, ClientStatusEnum.INACTIVE))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findById(999L)
    }
}

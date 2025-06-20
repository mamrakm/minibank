package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class
ClientServiceImplTest {

    @Mock
    private lateinit var clientRepository: ClientRepository

    @Mock
    private lateinit var clientMapper: ClientMapper

    @InjectMocks
    private lateinit var clientService: ClientServiceImpl

    private lateinit var sampleUuid: UUID
    private lateinit var sampleClientEntity: ClientEntity
    private lateinit var sampleClientDto: ClientDto
    private lateinit var sampleCreateClientRequestDto: CreateClientRequestDto
    private lateinit var sampleCreateClientResponseDto: CreateClientResponseDto

    @BeforeEach
    fun setUp() {
        sampleUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")

        sampleClientEntity = ClientEntity(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe1@example.com",
            passwordHash = "\$2a\$10\$dummyHashForTesting",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid,
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now(),
            lastLoginAt = null
        )

        sampleClientDto = ClientDto(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe2@example.com",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid,
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now()
        )

        sampleCreateClientRequestDto = CreateClientRequestDto(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe3@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )

        sampleCreateClientResponseDto = CreateClientResponseDto(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe4@example.com",
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid
        )
    }

    @Test
    fun `getClient should return client when found`() {
        // Given
        whenever(clientRepository.findActiveById(1L)).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientMapper.entityToDto(sampleClientEntity)).thenReturn(sampleClientDto)

        // When & Then
        StepVerifier.create(clientService.getClient(1L))
            .expectNext(sampleClientDto)
            .verifyComplete()

        verify(clientRepository).findActiveById(1L)
        verify(clientMapper).entityToDto(sampleClientEntity)
    }

    @Test
    fun `getClient should throw ClientNotFoundException when not found`() {
        // Given
        whenever(clientRepository.findActiveById(1L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.getClient(1L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findActiveById(1L)
    }

    @Test
    fun `getAllClients should return all clients`() {
        // Given
        whenever(clientRepository.findAllActive()).thenReturn(Flux.just(sampleClientEntity))
        whenever(clientMapper.entityToDto(sampleClientEntity)).thenReturn(sampleClientDto)

        // When & Then
        StepVerifier.create(clientService.getAllClients())
            .expectNext(sampleClientDto)
            .verifyComplete()

        verify(clientRepository).findAllActive()
        verify(clientMapper).entityToDto(sampleClientEntity)
    }

    @Test
    fun `createClient should save client when email is unique`() {
        // Given
        whenever(clientRepository.findByEmail(sampleCreateClientRequestDto.email)).thenReturn(Mono.empty())
        whenever(clientMapper.createRequestToEntity(sampleCreateClientRequestDto)).thenReturn(sampleClientEntity)
        whenever(clientRepository.saveAndReturn(
            sampleClientEntity.firstName,
            sampleClientEntity.lastName,
            sampleClientEntity.email,
            sampleClientEntity.phoneNumber,
            sampleClientEntity.address,
            sampleClientEntity.dateOfBirth
        )).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientMapper.entityToCreateResponse(sampleClientEntity)).thenReturn(sampleCreateClientResponseDto)

        // When & Then
        StepVerifier.create(clientService.createClient(sampleCreateClientRequestDto))
            .expectNext(sampleCreateClientResponseDto)
            .verifyComplete()
    }

    @Test
    fun `createClient should throw DuplicateClientException when email exists`() {
        // Given
        whenever(clientRepository.findByEmail(sampleCreateClientRequestDto.email)).thenReturn(Mono.just(sampleClientEntity))

        // When & Then
        StepVerifier.create(clientService.createClient(sampleCreateClientRequestDto))
            .expectError(DuplicateClientException::class.java)
            .verify()

        verify(clientRepository).findByEmail(sampleCreateClientRequestDto.email)
    }

    @Test
    fun `updateClient should update client when found`() {
        // Given
        val clientId = 1L
        // Ensure the DTO has the correct ID
        val clientDtoWithId = sampleClientDto.copy(id = clientId)
        
        whenever(clientRepository.findById(clientId)).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientRepository.save(any<ClientEntity>())).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientMapper.entityToDto(sampleClientEntity)).thenReturn(clientDtoWithId)

        // When & Then
        StepVerifier.create(clientService.updateClient(clientId, clientDtoWithId))
            .expectNext(clientDtoWithId)
            .verifyComplete()

        verify(clientRepository).findById(clientId)
        verify(clientRepository).save(any<ClientEntity>())
        verify(clientMapper).entityToDto(sampleClientEntity)
    }

    @Test
    fun `updateClient should throw ClientNotFoundException when not found`() {
        // Given
        whenever(clientRepository.findById(1L)).thenReturn(Mono.empty())

        // When & Then
        // Your test code - INCORRECT
        StepVerifier.create(clientService.updateClient(1L, sampleClientDto))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findById(1L)
    }

    @Test
    fun `deleteClient should soft delete client when found`() {
        // Given
        val inactiveClient = sampleClientEntity.copy(status = ClientStatusEnum.INACTIVE)
        whenever(clientRepository.findById(1L)).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientRepository.save(any<ClientEntity>())).thenReturn(Mono.just(inactiveClient))

        // When & Then
        StepVerifier.create(clientService.deleteClient(1L))
            .verifyComplete()

        verify(clientRepository).findById(1L)
        verify(clientRepository).save(any<ClientEntity>())
    }

    @Test
    fun `deleteClient should throw ClientNotFoundException when not found`() {
        // Given
        whenever(clientRepository.findById(1L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.deleteClient(1L))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(clientRepository).findById(1L)
    }

    @Test
    fun `findClientByEmail should return client when found`() {
        // Given
        whenever(clientRepository.findActiveByEmail("john.doe@example.com")).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientMapper.entityToDto(sampleClientEntity)).thenReturn(sampleClientDto)

        // When & Then
        StepVerifier.create(clientService.findClientByEmail("john.doe@example.com"))
            .expectNext(sampleClientDto)
            .verifyComplete()

        verify(clientRepository).findActiveByEmail("john.doe@example.com")
        verify(clientMapper).entityToDto(sampleClientEntity)
    }

    @Test
    fun `findClientByEmail should return empty when not found`() {
        // Given
        whenever(clientRepository.findActiveByEmail("notfound@example.com")).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(clientService.findClientByEmail("notfound@example.com"))
            .verifyComplete()

        verify(clientRepository).findActiveByEmail("notfound@example.com")
    }

    @Test
    fun `findClientsByFirstName should return clients when found`() {
        // Given
        whenever(clientRepository.findActiveByFirstNameIgnoreCase("John")).thenReturn(Flux.just(sampleClientEntity))
        whenever(clientMapper.entityToDto(sampleClientEntity)).thenReturn(sampleClientDto)

        // When & Then
        StepVerifier.create(clientService.findClientsByFirstName("John"))
            .expectNext(sampleClientDto)
            .verifyComplete()

        verify(clientRepository).findActiveByFirstNameIgnoreCase("John")
        verify(clientMapper).entityToDto(sampleClientEntity)
    }

    @Test
    fun `findClientsByFirstName should return empty when not found`() {
        // Given
        whenever(clientRepository.findActiveByFirstNameIgnoreCase("NotFound")).thenReturn(Flux.empty())

        // When & Then
        StepVerifier.create(clientService.findClientsByFirstName("NotFound"))
            .verifyComplete()

        verify(clientRepository).findActiveByFirstNameIgnoreCase("NotFound")
    }
}
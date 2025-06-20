package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
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
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class ClientServiceTest {

    @Mock
    private lateinit var clientRepository: ClientRepository

    @Mock
    private lateinit var clientMapper: ClientMapper

    @InjectMocks
    private lateinit var clientService: ClientServiceImpl

    private lateinit var sampleClientEntity: ClientEntity
    private lateinit var sampleClientDto: ClientDto

    @BeforeEach
    fun setUp() {
        sampleClientEntity = ClientEntity(
            id = 1L,
            keycloakUserId = "keycloak-123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe1@example.com",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            status = ClientStatusEnum.ACTIVE,
            createdAt = LocalDateTime.now()
        )

        sampleClientDto = ClientDto(
            id = 1L,
            keycloakUserId = "keycloak-123",
            firstName = "John",
            lastName = "Doe",
            email = "john.doe2@example.com",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            status = ClientStatusEnum.ACTIVE,
            createdAt = LocalDateTime.now()
        )
    }

    @Test
    fun `getClient should return client when found`() {
        whenever(clientRepository.findActiveById(1L)).thenReturn(Mono.just(sampleClientEntity))
        whenever(clientMapper.entityToDto(sampleClientEntity)).thenReturn(sampleClientDto)

        StepVerifier.create(clientService.getClient(1L))
            .expectNext(sampleClientDto)
            .verifyComplete()

        verify(clientRepository).findActiveById(1L)
        verify(clientMapper).entityToDto(sampleClientEntity)
    }

    @Test
    fun `createClientProfile should save and return new client`() {
        val request = CreateClientProfileRequestDto(
            keycloakUserId = "new-user-id",
            firstName = "Jane",
            lastName = "Doe",
            email = "jane.doe@example.com",
            role = UserRoleEnum.CLIENT,
            phoneNumber = null,
            address = null,
            dateOfBirth = null
        )
        val newEntity = sampleClientEntity.copy(id = 0, keycloakUserId = "new-user-id")
        val savedEntity = newEntity.copy(id = 2L)
        val resultDto = sampleClientDto.copy(id = 2L)

        whenever(clientRepository.findByKeycloakUserId(request.keycloakUserId)).thenReturn(Mono.empty())
        whenever(clientMapper.createProfileRequestToEntity(request)).thenReturn(newEntity)
        whenever(clientRepository.save(any<ClientEntity>())).thenReturn(Mono.just(savedEntity))
        whenever(clientMapper.entityToDto(savedEntity)).thenReturn(resultDto)

        StepVerifier.create(clientService.createClientProfile(request))
            .expectNext(resultDto)
            .verifyComplete()
    }
}

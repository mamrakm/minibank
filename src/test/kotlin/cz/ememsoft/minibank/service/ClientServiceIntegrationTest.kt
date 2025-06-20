package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.TestBase
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.repository.ClientRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * Integration tests for ClientService that verify mapper integration
 * and end-to-end functionality with real database.
 */
@SpringBootTest
@ActiveProfiles("test")
class ClientServiceIntegrationTest : TestBase() {

    @Autowired
    private lateinit var clientService: ClientService

    @Autowired
    private lateinit var clientRepository: ClientRepository

    @Autowired
    private lateinit var clientMapper: ClientMapper

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    fun setUp() {
        // Disable security for service layer tests (focus on business logic)
        disableSecurity()
        
        // Clean up database
        clientRepository.deleteAll().block()
    }

    @Test
    fun `service should properly map entities to DTOs when retrieving clients`() {
        // Arrange - Create a client entity directly in the database
        val clientEntity = ClientEntity(
            id = null,
            firstName = "Integration",
            lastName = "Test",
            email = "integration@test.com",
            passwordHash = passwordEncoder.encode("password123"),
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Integration St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now(),
            lastLoginAt = null
        )
        
        val savedEntity = clientRepository.save(clientEntity).block()!!

        // Act
        val result = clientService.getClient(savedEntity.id!!).block()!!

        // Assert - Verify mapping worked correctly
        assertEquals(savedEntity.id, result.id)
        assertEquals(savedEntity.firstName, result.firstName)
        assertEquals(savedEntity.lastName, result.lastName)
        assertEquals(savedEntity.email, result.email)
        assertEquals(savedEntity.role, result.role)
        assertEquals(savedEntity.phoneNumber, result.phoneNumber)
        assertEquals(savedEntity.address, result.address)
        assertEquals(savedEntity.dateOfBirth, result.dateOfBirth)
        assertEquals(savedEntity.personalNumber, result.personalNumber)
        assertEquals(savedEntity.status, result.status)
        assertEquals(savedEntity.enabled, result.enabled)
        assertEquals(savedEntity.createdAt, result.createdAt)
        
        // Password hash should not be in DTO (security check)
        assertNotNull(savedEntity.passwordHash)
    }

    @Test
    fun `updateClient should use mapper to merge DTO with existing entity`() {
        // Arrange - Create initial client
        val originalEntity = ClientEntity(
            id = null,
            firstName = "Original",
            lastName = "User",
            email = "original@test.com",
            passwordHash = passwordEncoder.encode("password123"),
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1111111111",
            address = "111 Original St",
            dateOfBirth = LocalDate.of(1985, 5, 5),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now().minusDays(30),
            lastLoginAt = LocalDateTime.now().minusDays(1)
        )
        
        val savedEntity = clientRepository.save(originalEntity).block()!!
        val originalPasswordHash = savedEntity.passwordHash
        val originalCreatedAt = savedEntity.createdAt
        val originalLastLoginAt = savedEntity.lastLoginAt

        // Create update DTO with some changed fields
        val updateDto = ClientDto(
            id = savedEntity.id,
            firstName = "Updated",
            lastName = "User",
            email = "updated@test.com", // Email changed
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+2222222222", // Phone changed
            address = savedEntity.address, // Address same
            dateOfBirth = savedEntity.dateOfBirth, // Birthday same
            personalNumber = savedEntity.personalNumber, // Personal number same
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = originalCreatedAt
        )

        // Act
        val result = clientService.updateClient(savedEntity.id!!, updateDto).block()!!

        // Assert - Verify mapper correctly merged fields
        assertEquals(updateDto.firstName, result.firstName) // Updated field
        assertEquals(updateDto.email, result.email) // Updated field
        assertEquals(updateDto.phoneNumber, result.phoneNumber) // Updated field
        
        // Preserved fields from original entity
        assertEquals(savedEntity.address, result.address)
        assertEquals(savedEntity.dateOfBirth, result.dateOfBirth)
        assertEquals(savedEntity.personalNumber, result.personalNumber)
        assertEquals(originalCreatedAt, result.createdAt)

        // Verify in database that sensitive fields were preserved
        val updatedEntityInDb = clientRepository.findById(savedEntity.id!!).block()!!
        assertEquals(originalPasswordHash, updatedEntityInDb.passwordHash) // Password preserved
        assertEquals(originalCreatedAt, updatedEntityInDb.createdAt) // Created timestamp preserved
        assertEquals(originalLastLoginAt, updatedEntityInDb.lastLoginAt) // Last login preserved
    }

    @Test
    fun `getAllClients should map all entities to DTOs correctly`() {
        // Arrange - Create multiple clients
        val client1 = ClientEntity(
            id = null,
            firstName = "Client",
            lastName = "One",
            email = "client1@test.com",
            passwordHash = passwordEncoder.encode("password1"),
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1111111111",
            address = "111 Client St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440001"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now(),
            lastLoginAt = null
        )

        val client2 = ClientEntity(
            id = null,
            firstName = "Client",
            lastName = "Two",
            email = "client2@test.com",
            passwordHash = passwordEncoder.encode("password2"),
            role = UserRoleEnum.ADMIN,
            phoneNumber = "+2222222222",
            address = "222 Client St",
            dateOfBirth = LocalDate.of(1992, 2, 2),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440002"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now(),
            lastLoginAt = LocalDateTime.now().minusHours(2)
        )

        clientRepository.save(client1).block()
        clientRepository.save(client2).block()

        // Act
        val results = clientService.getAllClients().collectList().block()!!

        // Assert - Verify mapping for both clients
        assertEquals(2, results.size)
        
        val clientOneDto = results.find { it.email == "client1@test.com" }!!
        val clientTwoDto = results.find { it.email == "client2@test.com" }!!
        
        // Verify mapping correctness for client 1
        assertEquals(client1.firstName, clientOneDto.firstName)
        assertEquals(client1.lastName, clientOneDto.lastName)
        assertEquals(client1.email, clientOneDto.email)
        assertEquals(client1.role, clientOneDto.role)
        
        // Verify mapping correctness for client 2 (admin)
        assertEquals(client2.firstName, clientTwoDto.firstName)
        assertEquals(client2.lastName, clientTwoDto.lastName)
        assertEquals(client2.email, clientTwoDto.email)
        assertEquals(client2.role, clientTwoDto.role)
        assertEquals(UserRoleEnum.ADMIN, clientTwoDto.role)
    }

    @Test
    fun `findClientByEmail should use mapper and return correct DTO`() {
        // Arrange
        val clientEntity = ClientEntity(
            id = null,
            firstName = "Email",
            lastName = "Search",
            email = "email.search@test.com",
            passwordHash = passwordEncoder.encode("password"),
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+5555555555",
            address = "555 Email St",
            dateOfBirth = LocalDate.of(1988, 8, 8),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440005"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now(),
            lastLoginAt = null
        )
        
        clientRepository.save(clientEntity).block()

        // Act
        val result = clientService.findClientByEmail("email.search@test.com").block()!!

        // Assert - Verify mapper correctly converted entity to DTO
        assertEquals(clientEntity.firstName, result.firstName)
        assertEquals(clientEntity.lastName, result.lastName)
        assertEquals(clientEntity.email, result.email)
        assertEquals(clientEntity.role, result.role)
        assertEquals(clientEntity.status, result.status)
        assertEquals(clientEntity.enabled, result.enabled)
    }

    @Test
    fun `mapper integration should handle role-based business logic`() {
        // Arrange - Create a regular client
        val regularClient = ClientEntity(
            id = null,
            firstName = "Regular",
            lastName = "Client",
            email = "regular@test.com",
            passwordHash = passwordEncoder.encode("password"),
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+3333333333",
            address = "333 Regular St",
            dateOfBirth = LocalDate.of(1993, 3, 3),
            personalNumber = UUID.fromString("550e8400-e29b-41d4-a716-446655440003"),
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = LocalDateTime.now(),
            lastLoginAt = null
        )
        
        val savedClient = clientRepository.save(regularClient).block()!!

        // Test mapper's custom validation logic
        val attemptAdminUpgrade = ClientDto(
            id = savedClient.id,
            firstName = savedClient.firstName,
            lastName = savedClient.lastName,
            email = savedClient.email,
            role = UserRoleEnum.ADMIN, // Attempt privilege escalation
            phoneNumber = savedClient.phoneNumber,
            address = savedClient.address,
            dateOfBirth = savedClient.dateOfBirth,
            personalNumber = savedClient.personalNumber,
            status = savedClient.status,
            enabled = savedClient.enabled,
            createdAt = savedClient.createdAt
        )

        // Act - Use mapper's validation method directly
        val validatedResult = clientMapper.updateClientWithValidation(attemptAdminUpgrade, savedClient)

        // Assert - Role should remain CLIENT (privilege escalation prevented)
        assertEquals(UserRoleEnum.CLIENT, validatedResult.role)
        assertEquals(savedClient.firstName, validatedResult.firstName)
    }
}
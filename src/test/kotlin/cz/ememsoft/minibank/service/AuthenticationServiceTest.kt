package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RegisterRequestDto
import cz.ememsoft.minibank.entity.ClientEntity
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.exception.InvalidClientDataException
import cz.ememsoft.minibank.repository.ClientRepository
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.test.util.ReflectionTestUtils
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Date
import java.util.UUID

/**
 * Unit tests for AuthenticationServiceImpl.
 * Tests JWT token generation, validation, registration, and login functionality.
 */
@ExtendWith(MockitoExtension::class)
class AuthenticationServiceTest {

    @Mock
    private lateinit var clientRepository: ClientRepository

    @InjectMocks
    private lateinit var authenticationService: AuthenticationServiceImpl

    private val jwtSecret = "test-secret-key-that-is-long-enough-for-jwt-signing-purposes"
    private val jwtExpiration = 3600L // 1 hour
    private val passwordEncoder = BCryptPasswordEncoder()

    private lateinit var registerRequest: RegisterRequestDto
    private lateinit var loginRequest: LoginRequestDto
    private lateinit var clientEntity: ClientEntity

    @BeforeEach
    fun setUp() {
        // Set private fields using reflection
        ReflectionTestUtils.setField(authenticationService, "jwtSecret", jwtSecret)
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", jwtExpiration)

        val sampleUuid = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val now = LocalDateTime.now()

        registerRequest = RegisterRequestDto(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            password = "password123",
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1)
        )

        loginRequest = LoginRequestDto(
            email = "john.doe@example.com",
            password = "password123"
        )

        clientEntity = ClientEntity(
            id = 1L,
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            passwordHash = passwordEncoder.encode("password123"),
            role = UserRoleEnum.CLIENT,
            phoneNumber = "+1234567890",
            address = "123 Main St",
            dateOfBirth = LocalDate.of(1990, 1, 1),
            personalNumber = sampleUuid,
            status = ClientStatusEnum.ACTIVE,
            enabled = true,
            createdAt = now,
            lastLoginAt = null
        )
    }

    // ==================== Registration Tests ====================

    @Test
    fun `register should create new client successfully`() {
        // Arrange
        given(clientRepository.findByEmail(registerRequest.email)).willReturn(Mono.empty())
        given(clientRepository.save(any<ClientEntity>())).willReturn(Mono.just(clientEntity))

        // Act & Assert
        StepVerifier.create(authenticationService.register(registerRequest))
            .assertNext { response ->
                assertNotNull(response.token)
                assertEquals(clientEntity.id, response.clientId)
                assertEquals(clientEntity.email, response.email)
                assertEquals(clientEntity.role, response.role)
                assertEquals(jwtExpiration, response.expiresIn)
            }
            .verifyComplete()

        verify(clientRepository).findByEmail(registerRequest.email)
        verify(clientRepository).save(any<ClientEntity>())
    }

    @Test
    fun `register should reject duplicate email`() {
        // Arrange
        given(clientRepository.findByEmail(registerRequest.email)).willReturn(Mono.just(clientEntity))

        // Act & Assert
        StepVerifier.create(authenticationService.register(registerRequest))
            .expectErrorMatches { 
                it is DuplicateClientException && 
                it.message == "Email ${registerRequest.email} is already registered"
            }
            .verify()

        verify(clientRepository).findByEmail(registerRequest.email)
    }

    @Test
    fun `register should handle repository errors`() {
        // Arrange
        given(clientRepository.findByEmail(registerRequest.email)).willReturn(Mono.empty())
        given(clientRepository.save(any<ClientEntity>())).willReturn(Mono.error(RuntimeException("Database error")))

        // Act & Assert
        StepVerifier.create(authenticationService.register(registerRequest))
            .expectError(RuntimeException::class.java)
            .verify()
    }

    // ==================== Login Tests ====================

    @Test
    fun `login should authenticate valid credentials successfully`() {
        // Arrange
        given(clientRepository.findByEmail(loginRequest.email)).willReturn(Mono.just(clientEntity))
        given(clientRepository.save(any<ClientEntity>())).willReturn(Mono.just(clientEntity.copy(lastLoginAt = LocalDateTime.now())))

        // Act & Assert
        StepVerifier.create(authenticationService.login(loginRequest))
            .assertNext { response ->
                assertNotNull(response.token)
                assertEquals(clientEntity.id, response.clientId)
                assertEquals(clientEntity.email, response.email)
                assertEquals(clientEntity.role, response.role)
                assertEquals(jwtExpiration, response.expiresIn)
            }
            .verifyComplete()

        verify(clientRepository).findByEmail(loginRequest.email)
        verify(clientRepository).save(any<ClientEntity>())
    }

    @Test
    fun `login should reject invalid email`() {
        // Arrange
        given(clientRepository.findByEmail(loginRequest.email)).willReturn(Mono.empty())

        // Act & Assert
        StepVerifier.create(authenticationService.login(loginRequest))
            .expectErrorMatches {
                it is ClientNotFoundException && it.message == "Invalid email or password"
            }
            .verify()

        verify(clientRepository).findByEmail(loginRequest.email)
    }

    @Test
    fun `login should reject invalid password`() {
        // Arrange
        val loginWithWrongPassword = loginRequest.copy(password = "wrongpassword")
        given(clientRepository.findByEmail(loginRequest.email)).willReturn(Mono.just(clientEntity))

        // Act & Assert
        StepVerifier.create(authenticationService.login(loginWithWrongPassword))
            .expectErrorMatches {
                it is ClientNotFoundException && it.message == "Invalid email or password"
            }
            .verify()

        verify(clientRepository).findByEmail(loginRequest.email)
    }

    @Test
    fun `login should reject disabled account`() {
        // Arrange
        val disabledClient = clientEntity.copy(enabled = false)
        given(clientRepository.findByEmail(loginRequest.email)).willReturn(Mono.just(disabledClient))

        // Act & Assert
        StepVerifier.create(authenticationService.login(loginRequest))
            .expectErrorMatches {
                it is InvalidClientDataException && it.message == "Account is disabled"
            }
            .verify()

        verify(clientRepository).findByEmail(loginRequest.email)
    }

    // ==================== JWT Token Tests ====================

    @Test
    fun `generateToken should create valid JWT token`() {
        // Act
        val token = authenticationService.generateToken(
            clientId = 1L,
            email = "test@example.com",
            role = "CLIENT"
        )

        // Assert
        assertNotNull(token)
        assertTrue(token.isNotEmpty())

        // Verify token can be parsed and contains correct claims
        val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())
        val claims = Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload

        assertEquals("1", claims.subject)
        assertEquals("test@example.com", claims["email"])
        assertEquals("CLIENT", claims["role"])
        assertNotNull(claims.issuedAt)
        assertNotNull(claims.expiration)
    }

    @Test
    fun `validateToken should validate correct JWT token`() {
        // Arrange
        val token = authenticationService.generateToken(
            clientId = 42L,
            email = "test@example.com",
            role = "ADMIN"
        )

        // Act & Assert
        StepVerifier.create(authenticationService.validateToken(token))
            .expectNext(42L)
            .verifyComplete()
    }

    @Test
    fun `validateToken should reject invalid token`() {
        // Arrange
        val invalidToken = "invalid.jwt.token"

        // Act & Assert
        StepVerifier.create(authenticationService.validateToken(invalidToken))
            .verifyComplete() // Should return empty Mono for invalid token
    }

    @Test
    fun `validateToken should reject expired token`() {
        // Arrange - Create token with short expiration
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", -1L) // Already expired
        val expiredToken = authenticationService.generateToken(1L, "test@example.com", "CLIENT")

        // Reset expiration for validation
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", jwtExpiration)

        // Act & Assert
        StepVerifier.create(authenticationService.validateToken(expiredToken))
            .verifyComplete() // Should return empty Mono for expired token
    }

    @Test
    fun `validateToken should reject token with wrong signature`() {
        // Arrange - Create token with different secret
        val wrongSecretKey = Keys.hmacShaKeyFor("wrong-secret-key-for-testing-purposes-only".toByteArray())
        val tokenWithWrongSignature = Jwts.builder()
            .setSubject("1")
            .claim("email", "test@example.com")
            .claim("role", "CLIENT")
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + 3600000))
            .signWith(wrongSecretKey)
            .compact()

        // Act & Assert
        StepVerifier.create(authenticationService.validateToken(tokenWithWrongSignature))
            .verifyComplete() // Should return empty Mono for invalid signature
    }

    // ==================== Password Tests ====================

    @Test
    fun `encodePassword should encode password correctly`() {
        // Arrange
        val plainPassword = "testPassword123"

        // Act
        val encodedPassword = authenticationService.encodePassword(plainPassword)

        // Assert
        assertNotNull(encodedPassword)
        assertNotEquals(plainPassword, encodedPassword)
        assertTrue(encodedPassword.startsWith("\$2a\$")) // BCrypt prefix
    }

    @Test
    fun `verifyPassword should verify correct password`() {
        // Arrange
        val plainPassword = "testPassword123"
        val encodedPassword = authenticationService.encodePassword(plainPassword)

        // Act & Assert
        assertTrue(authenticationService.verifyPassword(plainPassword, encodedPassword))
    }

    @Test
    fun `verifyPassword should reject incorrect password`() {
        // Arrange
        val plainPassword = "testPassword123"
        val wrongPassword = "wrongPassword"
        val encodedPassword = authenticationService.encodePassword(plainPassword)

        // Act & Assert
        assertFalse(authenticationService.verifyPassword(wrongPassword, encodedPassword))
    }

    // ==================== Admin Role Tests ====================

    @Test
    fun `register should allow admin role creation`() {
        // Arrange
        val adminRegisterRequest = registerRequest.copy(role = UserRoleEnum.ADMIN)
        val adminClientEntity = clientEntity.copy(role = UserRoleEnum.ADMIN)
        
        given(clientRepository.findByEmail(adminRegisterRequest.email)).willReturn(Mono.empty())
        given(clientRepository.save(any<ClientEntity>())).willReturn(Mono.just(adminClientEntity))

        // Act & Assert
        StepVerifier.create(authenticationService.register(adminRegisterRequest))
            .assertNext { response ->
                assertEquals(UserRoleEnum.ADMIN, response.role)
                assertNotNull(response.token)
            }
            .verifyComplete()
    }

    @Test
    fun `login should work with admin credentials`() {
        // Arrange
        val adminClient = clientEntity.copy(role = UserRoleEnum.ADMIN)
        given(clientRepository.findByEmail(loginRequest.email)).willReturn(Mono.just(adminClient))
        given(clientRepository.save(any<ClientEntity>())).willReturn(Mono.just(adminClient.copy(lastLoginAt = LocalDateTime.now())))

        // Act & Assert
        StepVerifier.create(authenticationService.login(loginRequest))
            .assertNext { response ->
                assertEquals(UserRoleEnum.ADMIN, response.role)
                assertNotNull(response.token)
            }
            .verifyComplete()
    }
}
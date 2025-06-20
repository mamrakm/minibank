package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.auth.AuthResponseDto
import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RegisterRequestDto
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.exception.ClientNotFoundException
import cz.ememsoft.minibank.exception.DuplicateClientException
import cz.ememsoft.minibank.exception.InvalidClientDataException
import cz.ememsoft.minibank.service.AuthenticationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

/**
 * Unit tests for AuthenticationController.
 * Tests registration and login endpoints with various scenarios.
 */
@ExtendWith(MockitoExtension::class)
class AuthenticationControllerTest {

    @Mock
    private lateinit var authenticationService: AuthenticationService

    @InjectMocks
    private lateinit var authenticationController: AuthenticationController

    private lateinit var registerRequest: RegisterRequestDto
    private lateinit var loginRequest: LoginRequestDto
    private lateinit var authResponse: AuthResponseDto

    @BeforeEach
    fun setUp() {
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

        authResponse = AuthResponseDto(
            token = "eyJhbGciOiJIUzUxMiJ9.test.token",
            clientId = 1L,
            email = "john.doe@example.com",
            role = UserRoleEnum.CLIENT,
            expiresIn = 3600L
        )
    }

    // ==================== Registration Tests ====================

    @Test
    fun `register should successfully register new client`() {
        // Arrange
        given(authenticationService.register(registerRequest)).willReturn(Mono.just(authResponse))

        // Act & Assert
        StepVerifier.create(authenticationController.register(registerRequest))
            .expectNext(authResponse)
            .verifyComplete()

        verify(authenticationService).register(registerRequest)
    }

    @Test
    fun `register should propagate DuplicateClientException`() {
        // Arrange
        val exception = DuplicateClientException("Email john.doe@example.com is already registered")
        given(authenticationService.register(registerRequest)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(authenticationController.register(registerRequest))
            .expectError(DuplicateClientException::class.java)
            .verify()

        verify(authenticationService).register(registerRequest)
    }

    @Test
    fun `register should handle admin role registration`() {
        // Arrange
        val adminRegisterRequest = registerRequest.copy(role = UserRoleEnum.ADMIN)
        val adminResponse = authResponse.copy(role = UserRoleEnum.ADMIN)
        given(authenticationService.register(adminRegisterRequest)).willReturn(Mono.just(adminResponse))

        // Act & Assert
        StepVerifier.create(authenticationController.register(adminRegisterRequest))
            .expectNext(adminResponse)
            .verifyComplete()

        verify(authenticationService).register(adminRegisterRequest)
    }

    // ==================== Login Tests ====================

    @Test
    fun `login should successfully authenticate valid credentials`() {
        // Arrange
        given(authenticationService.login(loginRequest)).willReturn(Mono.just(authResponse))

        // Act & Assert
        StepVerifier.create(authenticationController.login(loginRequest))
            .expectNext(authResponse)
            .verifyComplete()

        verify(authenticationService).login(loginRequest)
    }

    @Test
    fun `login should propagate ClientNotFoundException for invalid credentials`() {
        // Arrange
        val exception = ClientNotFoundException("Invalid email or password")
        given(authenticationService.login(loginRequest)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(authenticationController.login(loginRequest))
            .expectError(ClientNotFoundException::class.java)
            .verify()

        verify(authenticationService).login(loginRequest)
    }

    @Test
    fun `login should propagate InvalidClientDataException for disabled account`() {
        // Arrange
        val exception = InvalidClientDataException("Account is disabled")
        given(authenticationService.login(loginRequest)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(authenticationController.login(loginRequest))
            .expectError(InvalidClientDataException::class.java)
            .verify()

        verify(authenticationService).login(loginRequest)
    }

    @Test
    fun `login should handle admin user authentication`() {
        // Arrange
        val adminResponse = authResponse.copy(role = UserRoleEnum.ADMIN)
        given(authenticationService.login(loginRequest)).willReturn(Mono.just(adminResponse))

        // Act & Assert
        StepVerifier.create(authenticationController.login(loginRequest))
            .expectNext(adminResponse)
            .verifyComplete()

        verify(authenticationService).login(loginRequest)
    }
}
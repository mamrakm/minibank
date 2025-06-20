package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.auth.AuthResponseDto
import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RegisterRequestDto
import reactor.core.publisher.Mono

/**
 * Service interface for authentication and authorization operations.
 */
interface AuthenticationService {

    /**
     * Registers a new client with authentication credentials.
     *
     * @param registerRequest The registration request containing client data and password
     * @return A [Mono] emitting the authentication response with JWT token
     */
    fun register(registerRequest: RegisterRequestDto): Mono<AuthResponseDto>

    /**
     * Authenticates a client using email and password.
     *
     * @param loginRequest The login request containing email and password
     * @return A [Mono] emitting the authentication response with JWT token
     */
    fun login(loginRequest: LoginRequestDto): Mono<AuthResponseDto>

    /**
     * Generates a JWT token for the authenticated client.
     *
     * @param clientId The client ID
     * @param email The client email
     * @param role The client role
     * @return JWT token string
     */
    fun generateToken(clientId: Long, email: String, role: String): String

    /**
     * Validates a JWT token and extracts client information.
     *
     * @param token The JWT token to validate
     * @return A [Mono] emitting the client ID if token is valid
     */
    fun validateToken(token: String): Mono<Long>

    /**
     * Encodes a plain text password using BCrypt.
     *
     * @param password The plain text password
     * @return The BCrypt encoded password hash
     */
    fun encodePassword(password: String): String

    /**
     * Verifies a plain text password against a BCrypt hash.
     *
     * @param password The plain text password
     * @param encodedPassword The BCrypt encoded password hash
     * @return true if passwords match, false otherwise
     */
    fun verifyPassword(password: String, encodedPassword: String): Boolean
}
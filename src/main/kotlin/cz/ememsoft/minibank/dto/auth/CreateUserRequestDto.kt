package cz.ememsoft.minibank.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * Request payload for creating a Keycloak user.
 */
data class CreateUserRequestDto(
    @field:NotBlank(message = "Username must not be blank")
    val username: String,
    @field:NotBlank(message = "Password must not be blank")
    val password: String,
    @field:NotBlank(message = "First name must not be blank")
    val firstName: String,
    @field:NotBlank(message = "Last name must not be blank")
    val lastName: String,
    @field:Email(message = "Email must be valid")
    val email: String,
    val roles: Set<String>? = null,
)

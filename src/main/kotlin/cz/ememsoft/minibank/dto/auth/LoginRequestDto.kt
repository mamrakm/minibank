package cz.ememsoft.minibank.dto.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * DTO for login requests.
 */
data class LoginRequestDto(
    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotBlank(message = "Password must not be blank")
    val password: String
)
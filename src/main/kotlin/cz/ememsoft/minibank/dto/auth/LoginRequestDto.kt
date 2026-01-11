package cz.ememsoft.minibank.dto.auth

import jakarta.validation.constraints.NotBlank

/**
 * Request payload for user login.
 */
data class LoginRequestDto(
    @field:NotBlank(message = "Username must not be blank")
    val username: String,
    @field:NotBlank(message = "Password must not be blank")
    val password: String,
)

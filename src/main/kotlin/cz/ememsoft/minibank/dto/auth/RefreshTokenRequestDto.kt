package cz.ememsoft.minibank.dto.auth

import jakarta.validation.constraints.NotBlank

/**
 * Request payload for refreshing an access token.
 */
data class RefreshTokenRequestDto(
    @field:NotBlank(message = "Refresh token must not be blank")
    val refreshToken: String,
)

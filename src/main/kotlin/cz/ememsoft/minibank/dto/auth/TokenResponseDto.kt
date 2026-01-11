package cz.ememsoft.minibank.dto.auth

/**
 * Response payload for Keycloak token responses.
 */
data class TokenResponseDto(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Long,
    val refreshExpiresIn: Long?,
    val tokenType: String,
    val scope: String?,
)

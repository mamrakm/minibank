package cz.ememsoft.minibank.dto.auth

import cz.ememsoft.minibank.enumeration.UserRoleEnum

/**
 * DTO for authentication responses.
 */
data class AuthResponseDto(
    val token: String,
    val clientId: Long,
    val email: String,
    val role: UserRoleEnum,
    val expiresIn: Long // seconds
)
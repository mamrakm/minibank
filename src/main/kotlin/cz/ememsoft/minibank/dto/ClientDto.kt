package cz.ememsoft.minibank.dto

import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Data Transfer Object (DTO) for representing a client profile.
 *
 * This DTO is used to transfer client data between different layers of the application.
 * Updated for Keycloak integration - authentication is handled by Keycloak.
 */
data class ClientDto(
    val id: Long,

    @NotNull(message = "Keycloak User ID must not be null")
    val keycloakUserId: String,

    @NotNull(message = "First name must not be null")
    val firstName: String,

    @NotNull(message = "Last name must not be null")
    val lastName: String,

    @NotNull(message = "Email must not be null")
    @Email(message = "Invalid email format")
    val email: String,

    @NotNull(message = "Role must not be null")
    val role: UserRoleEnum,

    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )
    val phoneNumber: String?,

    val address: String?,

    val dateOfBirth: LocalDate?,

    @NotNull(message = "Status must not be null")
    val status: ClientStatusEnum = ClientStatusEnum.ACTIVE,

    @NotNull(message = "Created at must not be null")
    val createdAt: LocalDateTime
)
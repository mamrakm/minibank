package cz.ememsoft.minibank.dto.client

import cz.ememsoft.minibank.enumeration.UserRoleEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

/**
 * DTO for an admin to create a new client profile.
 * This profile must be linked to an existing user in Keycloak.
 */
data class CreateClientProfileRequestDto(
    @field:NotBlank(message = "Keycloak User ID ('sub' claim) must not be blank")
    val keycloakUserId: String,

    @field:NotBlank(message = "First name must not be blank")
    val firstName: String,

    @field:NotBlank(message = "Last name must not be blank")
    val lastName: String,

    @field:NotBlank(message = "Email must not be blank")
    @field:Email(message = "Email must be valid")
    val email: String,

    @field:NotNull(message = "Role must not be null")
    val role: UserRoleEnum = UserRoleEnum.CLIENT,

    val phoneNumber: String?,

    val address: String?,

    val dateOfBirth: LocalDate?
)
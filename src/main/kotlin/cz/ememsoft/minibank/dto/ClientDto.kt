package cz.ememsoft.minibank.dto

import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.enumeration.UserRoleEnum
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

/**
 * Data Transfer Object (DTO) for representing a client.
 *
 * This DTO is used to transfer client data between different layers of the application.
 * It excludes sensitive authentication data like password hash.
 *
 * @property id The unique identifier for the client.
 * @property firstName The first name of the client.
 * @property lastName The last name of the client.
 * @property email The email address of the client.
 * @property role The user role (CLIENT or ADMIN).
 * @property phoneNumber The phone number of the client.
 * @property address The address of the client.
 * @property dateOfBirth The date of birth of the client.
 * @property personalNumber The unique personal identifier (UUID) of the client.
 * @property status The status of the client (ACTIVE, INACTIVE, SUSPENDED).
 * @property enabled Whether the account is enabled for login.
 * @property createdAt Account creation timestamp.
 */
data class ClientDto(
    val id: Long,

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

    @NotNull(message = "Personal number must not be null")
    val personalNumber: UUID,

    @NotNull(message = "Status must not be null")
    val status: ClientStatusEnum = ClientStatusEnum.ACTIVE,

    @NotNull(message = "Enabled must not be null")
    val enabled: Boolean = true,

    @NotNull(message = "Created at must not be null")
    val createdAt: LocalDateTime
) {
    override fun toString(): String {
        return "ClientDto(id=$id, firstName='$firstName', lastName='$lastName', email='$email', role=$role, phoneNumber='$phoneNumber', address='$address', dateOfBirth=$dateOfBirth, personalNumber=$personalNumber, status=$status, enabled=$enabled, createdAt=$createdAt)"
    }
}
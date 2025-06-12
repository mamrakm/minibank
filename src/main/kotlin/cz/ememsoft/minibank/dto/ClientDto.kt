package cz.ememsoft.minibank.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate
import java.util.UUID

/**
 * Data Transfer Object (DTO) for representing a client.
 *
 * This DTO is used to transfer client data between different layers of the application.
 * It maps to the ClientEntity entity.
 *
 * @property id The unique identifier for the client.
 * @property firstName The first name of the client.
 * @property lastName The last name of the client.
 * @property email The email address of the client.
 * @property phoneNumber The phone number of the client.
 * @property address The address of the client.
 * @property dateOfBirth The date of birth of the client.
 * @property personalNumber The unique personal identifier (UUID) of the client.
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

    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )
    val phoneNumber: String?,

    val address: String?,

    val dateOfBirth: LocalDate?,

    @NotNull(message = "Personal number must not be null")
    val personalNumber: UUID
) {
    override fun toString(): String {
        return "ClientDto(id=$id, firstName='$firstName', lastName='$lastName', email='$email', phoneNumber='$phoneNumber', address='$address', dateOfBirth=$dateOfBirth, personalNumber=$personalNumber)"
    }
}
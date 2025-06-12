package cz.ememsoft.minibank.api.client.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import java.time.LocalDate

/**
 * Create client request DTO for client creation.
 */
data class CreateClientRequestDto(
    @NotBlank(message = "First name must not be blank")
    val firstName: String,

    @NotBlank(message = "Last name must not be blank")
    val lastName: String,

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email must be valid")
    val email: String,

    @NotBlank(message = "Phone number must not be blank")
    val phoneNumber: String,

    @NotBlank(message = "Address must not be blank")
    val address: String,

    @NotNull(message = "Date of birth must not be null")
    @Past(message = "Date of birth must be in the past")
    val dateOfBirth: LocalDate
) {
    override fun toString(): String {
        return "CreateClientRequestDto(firstName='$firstName', lastName='$lastName', email='$email', phoneNumber='$phoneNumber', address='$address', dateOfBirth=$dateOfBirth)"
    }
}
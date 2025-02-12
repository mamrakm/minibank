package cz.ememsoft.minibank.client.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateClientRequestDto(
    @NotNull
    @NotBlank
    val firstName: String,
    @NotNull
    @NotBlank
    val lastName: String,
    @Email(message = "Invalid email")
    val email: String,
    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )
    val phone: String,
    val address: String,
) {
    override fun toString(): String {
        return "ClientSaveRequest(firstName='$firstName', lastName='$lastName', email='$email', phone='$phone', address='$address')"
    }
}
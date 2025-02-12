package cz.ememsoft.minibank.dto

import jakarta.validation.constraints.NotNull

/**
 * Data Transfer Object (DTO) for representing client information.
 *
 * This DTO is used to transfer client data between different layers of the application.
 *
 * @property firstName The first name of the client.
 * @property lastName The last name of the client.
 * @property email The email address of the client.
 * @property phone The phone number of the client.
 * @property address The address of the client.
 */
data class ClientDto(
    val id: Long,
    @NotNull
    val firstName: String,
    @NotNull
    val lastName: String,
    @NotNull
    val email: String,
    @NotNull
    val phone: String,
    @NotNull
    val address: String,
) {
    /**
     * Returns a string representation of the ClientDto object.
     *
     * @return A string representation of the object in the format:
     * "CustomerDto(firstName='value', lastName='value', email='value', phone='value', address='value')".
     */
    override fun toString(): String {
        return "ClientDto(firstName='$firstName', lastName='$lastName', email='$email', phone='$phone', address='$address')"
    }
}

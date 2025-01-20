package cz.ememsoft.minibank.dto

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
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String,
) {
    /**
     * Returns a string representation of the ClientDto object.
     *
     * @return A string representation of the object in the format:
     * "CustomerDto(firstName='value', lastName='value', email='value', phone='value', address='value')".
     */
    override fun toString(): String {
        return "CustomerDto(firstName='$firstName', lastName='$lastName', email='$email', phone='$phone', address='$address')"
    }
}

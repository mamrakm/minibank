package cz.ememsoft.minibank.api.dto.response

import jakarta.validation.constraints.NotNull

data class CreateClientResponseDto(
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
        return "CustomerDto(id= '$id', firstName='$firstName', lastName='$lastName', email='$email', phone='$phone', address='$address')"
    }
}

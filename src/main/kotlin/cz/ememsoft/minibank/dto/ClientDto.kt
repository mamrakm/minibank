package cz.ememsoft.minibank.dto

data class ClientDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String,
) {
    override fun toString(): String {
        return "CustomerDto(firstName='$firstName', lastName='$lastName', email='$email', phone='$phone', address='$address')"
    }
}

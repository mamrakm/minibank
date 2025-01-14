package cz.ememsoft.minibank.dto

data class CustomerDto(
    val id: Long,
    val name: String,
    val surname: String,
    val email: String,
    val phone: String,
    val address: String
)

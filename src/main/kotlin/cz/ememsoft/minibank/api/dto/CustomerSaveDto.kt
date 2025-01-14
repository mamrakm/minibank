package cz.ememsoft.minibank.api.dto

data class CustomerSaveDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val address: String,
)

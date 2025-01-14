package cz.ememsoft.minibank.api.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive


data class CustomerSaveRequest(
    @Positive
    val id: Long,
    @NotNull
    val firstName: String,
    @NotNull
    val surname: String,
    @Email(message = "Invalid email")
    val email: String,
    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}\$",
        message = "Invalid phone number"
    )
    val phone: String,

    val address: String
)

package cz.ememsoft.minibank.api.account.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

/**
 * Create an account request DTO using MoneyUtils for financial safety.
 */
data class CreateAccountRequestDto(
    @NotBlank(message = "Account type must not be blank")
    val accountType: String,

    @NotNull(message = "Initial balance must not be null")
    @PositiveOrZero(message = "Initial balance must be zero or positive")
    @DecimalMin(value = "0.00", message = "Initial balance must be at least 0.00")
    @Digits(integer = 15, fraction = 4, message = "Balance must have at most 15 integer digits and 4 decimal places")
    val balance: BigDecimal,

    @NotBlank(message = "Currency must not be blank")
    val currency: String,

    @NotNull(message = "Client ID must not be null")
    val clientId: Long,
) {
    val normalizedCurrency: String
        get() = currency.uppercase().trim()

    val normalizedAccountType: String
        get() = accountType.uppercase().trim()

    fun getFormattedBalance(): String = balance.toPlainString()

    override fun toString(): String {
        return "CreateAccountRequestDto(accountType='$accountType', balance=$balance, currency='$currency', clientId=$clientId)"
    }
}
package cz.ememsoft.minibank.validation

import cz.ememsoft.minibank.exception.ValidationException
import java.math.BigDecimal

/**
 * Specialized validator for account creation requests.
 * Follows Single Responsibility Principle.
 */
class ValidationRequestValidator(
    private val balanceValidator: ValidationStrategy<BigDecimal>,
    private val currencyValidator: ValidationStrategy<String>,
    private val accountTypeValidator: ValidationStrategy<String>
) {

    fun validateAccountRequest(
        balance: BigDecimal,
        currency: String,
        accountType: String
    ) {
        val errors = mutableListOf<String>()

        try {
            balanceValidator.validate(balance)
        } catch (e: ValidationException) {
            errors.add("Balance: ${e.message}")
        }

        try {
            currencyValidator.validate(currency)
        } catch (e: ValidationException) {
            errors.add("Currency: ${e.message}")
        }

        try {
            accountTypeValidator.validate(accountType)
        } catch (e: ValidationException) {
            errors.add("Account Type: ${e.message}")
        }

        if (errors.isNotEmpty()) {
            throw ValidationException("Validation failed: ${errors.joinToString("; ")}")
        }
    }
}
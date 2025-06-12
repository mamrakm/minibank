package cz.ememsoft.minibank.validation

import cz.ememsoft.minibank.exception.ValidationException
import cz.ememsoft.minibank.util.MoneyUtils

/**
 * Validation strategy for currency codes.
 * Follows a Single Responsibility Principle and Strategy Pattern.
 */
class CurrencyValidationStrategy : ValidationStrategy<String> {

    override fun validate(input: String) {
        try {
            MoneyUtils.validateCurrency(input)
        } catch (e: IllegalArgumentException) {
            throw ValidationException("Invalid currency code: ${e.message}", e)
        }
    }
}
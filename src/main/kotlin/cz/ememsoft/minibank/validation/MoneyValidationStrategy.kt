package cz.ememsoft.minibank.validation

import cz.ememsoft.minibank.exception.ValidationException
import cz.ememsoft.minibank.util.MoneyUtils
import java.math.BigDecimal

/**
 * Validation strategy for monetary amounts.
 * Follows a Single Responsibility Principle and Strategy Pattern.
 */
class MoneyValidationStrategy(
    private val allowZero: Boolean = false
) : ValidationStrategy<BigDecimal> {

    override fun validate(input: BigDecimal) {
        try {
            MoneyUtils.validateAmount(input, allowZero)
        } catch (e: IllegalArgumentException) {
            throw ValidationException("Invalid monetary amount: ${e.message}", e)
        }
    }
}
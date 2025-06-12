package cz.ememsoft.minibank.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Utility class for safe monetary operations and validations in banking applications.
 *
 * This class provides standardized methods for handling monetary values with proper
 * precision, rounding, and validation to prevent common financial calculation errors.
 *
 * <p><strong>Key Features:</strong></p>
 * <ul>
 *   <li>Standardized precision (4 decimal places)</li>
 *   <li>Banker's rounding (HALF_EVEN) for fairness</li>
 *   <li>Comprehensive validation methods</li>
 *   <li>Currency-aware operations</li>
 * </ul>
 */
object MoneyUtils {

    /**
     * Standard scale for all monetary values in the banking system.
     * 4 decimal places provides sufficient precision for most currencies.
     */
    const val MONEY_SCALE = 4

    /**
     * Standard rounding mode for monetary calculations.
     * HALF_EVEN (banker's rounding) minimizes cumulative rounding errors.
     */
    val MONEY_ROUNDING_MODE = RoundingMode.HALF_EVEN

    /**
     * Minimum allowed monetary amount to prevent micro-transaction spam.
     */
    val MIN_MONEY_AMOUNT = BigDecimal("0.0001")

    /**
     * Maximum allowed monetary amount for security and practical limits.
     */
    val MAX_MONEY_AMOUNT = BigDecimal("999999999999999.9999")

    /**
     * Zero amount with proper monetary precision.
     */
    val ZERO = BigDecimal.ZERO.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)

    /**
     * Normalizes a monetary amount to the standard scale and rounding.
     *
     * @param amount The amount to normalize
     * @return The normalized amount with standard precision
     * @throws IllegalArgumentException if the amount is null
     */
    fun normalize(amount: BigDecimal?): BigDecimal {
        require(amount != null) { "Amount cannot be null" }
        return amount.setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
    }

    /**
     * Validates that a monetary amount is within acceptable bounds.
     *
     * @param amount The amount to validate
     * @param allowZero Whether zero values are acceptable
     * @return The validated and normalized amount
     * @throws IllegalArgumentException if validation fails
     */
    fun validateAmount(amount: BigDecimal?, allowZero: Boolean = false): BigDecimal {
        require(amount != null) { "Amount cannot be null" }

        val normalizedAmount = normalize(amount)

        if (!allowZero && normalizedAmount <= ZERO) {
            throw IllegalArgumentException("Amount must be positive, got: $normalizedAmount")
        }

        if (allowZero && normalizedAmount < ZERO) {
            throw IllegalArgumentException("Amount cannot be negative, got: $normalizedAmount")
        }

        if (normalizedAmount < MIN_MONEY_AMOUNT && normalizedAmount > ZERO) {
            throw IllegalArgumentException("Amount $normalizedAmount is below minimum allowed amount $MIN_MONEY_AMOUNT")
        }

        if (normalizedAmount > MAX_MONEY_AMOUNT) {
            throw IllegalArgumentException("Amount $normalizedAmount exceeds maximum allowed amount $MAX_MONEY_AMOUNT")
        }

        return normalizedAmount
    }

    /**
     * Safely adds two monetary amounts.
     *
     * @param amount1 First amount
     * @param amount2 Second amount
     * @return The sum with proper monetary precision
     */
    fun add(amount1: BigDecimal, amount2: BigDecimal): BigDecimal {
        val norm1 = normalize(amount1)
        val norm2 = normalize(amount2)
        return norm1.add(norm2).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
    }

    /**
     * Safely subtracts one monetary amount from another.
     *
     * @param amount1 Amount to subtract from
     * @param amount2 Amount to subtract
     * @return The difference with proper monetary precision
     */
    fun subtract(amount1: BigDecimal, amount2: BigDecimal): BigDecimal {
        val norm1 = normalize(amount1)
        val norm2 = normalize(amount2)
        return norm1.subtract(norm2).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
    }

    /**
     * Safely multiplies a monetary amount by a factor.
     *
     * @param amount The monetary amount
     * @param factor The multiplication factor
     * @return The product with proper monetary precision
     */
    fun multiply(amount: BigDecimal, factor: BigDecimal): BigDecimal {
        val normAmount = normalize(amount)
        return normAmount.multiply(factor).setScale(MONEY_SCALE, MONEY_ROUNDING_MODE)
    }

    /**
     * Safely divides a monetary amount by a divisor.
     *
     * @param amount The monetary amount
     * @param divisor The divisor (must not be zero)
     * @return The quotient with proper monetary precision
     * @throws IllegalArgumentException if divisor is zero
     */
    fun divide(amount: BigDecimal, divisor: BigDecimal): BigDecimal {
        require(divisor.compareTo(BigDecimal.ZERO) != 0) { "Cannot divide by zero" }

        val normAmount = normalize(amount)
        return normAmount.divide(divisor, MONEY_SCALE, MONEY_ROUNDING_MODE)
    }

    /**
     * Compares two monetary amounts safely.
     *
     * @param amount1 First amount
     * @param amount2 Second amount
     * @return -1 if amount1 < amount2, 0 if equal, 1 if amount1 > amount2
     */
    fun compare(amount1: BigDecimal, amount2: BigDecimal): Int {
        val norm1 = normalize(amount1)
        val norm2 = normalize(amount2)
        return norm1.compareTo(norm2)
    }

    /**
     * Checks if an amount is sufficient for a required amount.
     *
     * @param available The available amount
     * @param required The required amount
     * @return true if available >= required
     */
    fun hasSufficientFunds(available: BigDecimal, required: BigDecimal): Boolean {
        return compare(available, required) >= 0
    }

    /**
     * Formats a monetary amount for display with a specific currency.
     *
     * @param amount The amount to format
     * @param currency The currency code
     * @param displayScale The number of decimal places to show (default 2)
     * @return Formatted string representation
     */
    fun formatAmount(amount: BigDecimal, currency: String, displayScale: Int = 2): String {
        val normalizedAmount = normalize(amount)
        val displayAmount = normalizedAmount.setScale(displayScale, MONEY_ROUNDING_MODE)
        return "$displayAmount $currency"
    }

    /**
     * Validates that a currency code is properly formatted.
     *
     * @param currency The currency code to validate
     * @return The normalized currency code (uppercase, trimmed)
     * @throws IllegalArgumentException if currency is invalid
     */
    fun validateCurrency(currency: String?): String {
        require(!currency.isNullOrBlank()) { "Currency cannot be null or blank" }

        val normalizedCurrency = currency.uppercase().trim()

        require(normalizedCurrency.length == 3) {
            "Currency code must be exactly 3 characters, got: '$normalizedCurrency'"
        }

        require(normalizedCurrency.all { it.isLetter() }) {
            "Currency code must contain only letters, got: '$normalizedCurrency'"
        }

        return normalizedCurrency
    }

    /**
     * Creates a monetary amount from a string representation.
     *
     * @param amountStr The string representation of the amount
     * @return The parsed and normalized monetary amount
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    fun fromString(amountStr: String?): BigDecimal {
        require(!amountStr.isNullOrBlank()) { "Amount string cannot be null or blank" }

        return try {
            val amount = BigDecimal(amountStr.trim())
            normalize(amount)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Invalid amount format: '$amountStr'", e)
        }
    }

    /**
     * Calculates a percentage of a monetary amount.
     *
     * @param amount The base amount
     * @param percentage The percentage (e.g., 5.5 for 5.5%)
     * @return The calculated percentage amount
     */
    fun calculatePercentage(amount: BigDecimal, percentage: BigDecimal): BigDecimal {
        val normAmount = normalize(amount)
        val factor = percentage.divide(BigDecimal("100"), 6, MONEY_ROUNDING_MODE)
        return multiply(normAmount, factor)
    }
}
package cz.ememsoft.minibank.api.transaction.request

import cz.ememsoft.minibank.util.MoneyUtils
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

/**
 * Data Transfer Object for requesting a money transaction operation.
 *
 * This DTO contains all the necessary information required to perform a transaction
 * between two accounts with proper validation.
 *
 * @property sourceAccountId The ID of the account to transfer from.
 * @property targetAccountId The ID of the account to transfer to.
 * @property amount The amount to transfer (must be positive).
 * @property currency The currency of the transaction.
 * @property reference Optional description or reference for the transaction.
 */
data class TransactionRequestDto(
    @NotNull(message = "Source account ID must not be null")
    val sourceAccountId: Long,

    @NotNull(message = "Target account ID must not be null")
    val targetAccountId: Long,

    @NotNull(message = "Amount must not be null")
    @Positive(message = "Amount must be greater than zero")
    @DecimalMin(value = "0.0001", message = "Amount must be at least 0.0001")
    @Digits(integer = 15, fraction = 4, message = "Amount must have at most 15 integer digits and 4 decimal places")
    val amount: BigDecimal,

    @NotBlank(message = "Currency must not be blank")
    val currency: String,

    val reference: String = ""
) {
    init {
        require(sourceAccountId != targetAccountId) {
            "Source and target account IDs must be different"
        }

        // Validate using MoneyUtils
        MoneyUtils.validateAmount(amount)
        MoneyUtils.validateCurrency(currency)
    }

    /**
     * Returns normalized currency using MoneyUtils.
     */
    val normalizedCurrency: String
        get() = MoneyUtils.validateCurrency(currency)

    override fun toString(): String {
        return "TransactionRequest(sourceAccountId=$sourceAccountId, targetAccountId=$targetAccountId, " +
                "amount=$amount, currency='$currency', reference='$reference')"
    }
}
package cz.ememsoft.minibank.api.transaction.response

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.math.BigDecimal

/**
 * Data Transfer Object for requesting a money transfer between accounts.
 *
 * This DTO contains all necessary information to perform a transaction
 * from one account to another.
 *
 * @property sourceAccountId The ID of the account from which money will be transferred.
 * @property targetAccountId The ID of the account to which money will be transferred.
 * @property amount The amount of money to transfer. Must be positive.
 * @property reference A description or reference for the transaction.
 */
data class TransferResponse(
    @NotNull(message = "Source account ID must not be null")
    val sourceAccountId: Long,

    @NotNull(message = "Target account ID must not be null")
    val targetAccountId: Long,

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Positive(message = "Amount must be positive")
    val amount: BigDecimal,

    val reference: String = "",
) {
    override fun toString(): String {
        return "TransferRequest(sourceAccountId=$sourceAccountId, targetAccountId=$targetAccountId, amount=$amount, reference='$reference')"
    }
}
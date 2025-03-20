package cz.ememsoft.minibank.transaction.request

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
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
 * @property currency The currency of the transaction.
 * @property reference A description or reference for the transaction.
 */
data class TransferRequest(
    @field:NotNull(message = "Source account ID must not be null")
    val sourceAccountId: Long,

    @field:NotNull(message = "Target account ID must not be null")
    val targetAccountId: Long,

    @field:NotNull(message = "Amount must not be null")
    @field:DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @field:Positive(message = "Amount must be positive")
    val amount: BigDecimal,

    @field:NotBlank(message = "Currency must not be blank")
    val currency: String,

    val reference: String = ""
) {
    override fun toString(): String {
        return "TransferRequest(sourceAccountId=$sourceAccountId, targetAccountId=$targetAccountId, amount=$amount, currency='$currency', reference='$reference')"
    }
}
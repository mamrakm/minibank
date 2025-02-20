package cz.ememsoft.minibank.transaction.request

import cz.ememsoft.minibank.enumeration.CurrencyEnum
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * Data Transfer Object for creating a new transaction.
 *
 * @property fromAccountId The ID of the sender's account.
 * @property toAccountId The ID of the recipient's account.
 * @property amount The amount to transfer (must be greater than zero).
 * @property currency The currency of the transaction, enforced by [CurrencyEnum].
 */
data class CreateTransactionRequestDto(
    @field:NotNull val fromAccountId: Long,
    @field:NotNull val toAccountId: Long,
    @field:Min(value = 1, message = "Amount must be greater than zero")
    val amount: BigDecimal,
    @field:NotNull val currency: CurrencyEnum
)

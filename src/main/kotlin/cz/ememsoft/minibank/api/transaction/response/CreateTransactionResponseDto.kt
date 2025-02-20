package cz.ememsoft.minibank.transaction.response

import cz.ememsoft.minibank.enumeration.CurrencyEnum
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Data Transfer Object for returning details of a processed transaction.
 *
 * @property transactionId The unique identifier of the transaction.
 * @property fromAccountId The sender's account ID.
 * @property toAccountId The recipient's account ID.
 * @property amount The transferred amount.
 * @property currency The currency of the transaction, enforced by [CurrencyEnum].
 * @property timestamp The timestamp when the transaction occurred.
 */
data class CreateTransactionResponseDto(
    val transactionId: Long,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal,
    val currency: CurrencyEnum,
    val timestamp: LocalDateTime
)

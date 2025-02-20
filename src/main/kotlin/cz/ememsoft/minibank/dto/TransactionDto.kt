package cz.ememsoft.minibank.dto

import java.math.BigDecimal

/**
 * Data Transfer Object (DTO) for representing a financial transaction.
 *
 * @property id Unique identifier of the transaction.
 * @property fromAccountId The ID of the sender's account.
 * @property toAccountId The ID of the recipient's account.
 * @property amount The amount of money being transferred.
 * @property currency The currency in which the transaction is executed.
 * @property status The current status of the transaction.
 */
data class TransactionDto(
    val id: Long? = null,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: BigDecimal,
    val currency: String,
    val status: String
)
package cz.ememsoft.minibank.api.transaction.response

import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Data Transfer Object for the response after a money transaction operation.
 *
 * This DTO provides information about the completed transaction.
 *
 * @property id The unique identifier of the transaction.
 * @property sourceAccountId The ID of the account from which money was transferred.
 * @property targetAccountId The ID of the account to which money was transferred.
 * @property amount The amount of money transferred.
 * @property currency The currency of the transaction.
 * @property timestamp The date and time when the transaction occurred.
 * @property status The status of the transaction (e.g., completed, failed).
 * @property reference A description or reference for the transaction.
 */
data class TransactionResponseDto(
    val id: Long,
    val sourceAccountId: Long,
    val targetAccountId: Long,
    val amount: BigDecimal,
    val currency: String,
    val timestamp: LocalDateTime,
    val status: TransactionStatusEnum,
    val reference: String
) {
    override fun toString(): String {
        return "TransactionResponse(id=$id, sourceAccountId=$sourceAccountId, targetAccountId=$targetAccountId, " +
                "amount=$amount, currency='$currency', timestamp=$timestamp, status=$status, reference='$reference')"
    }
}
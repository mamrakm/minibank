package cz.ememsoft.minibank.transaction.entity

import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.enumeration.TransactionStatusEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents a financial transaction entity.
 *
 * Mapped to the `transaction` table in the `bank` schema.
 *
 * @property id The unique identifier for the transaction.
 * @property fromAccountId The sender's account ID.
 * @property toAccountId The recipient's account ID.
 * @property amount The amount transferred.
 * @property currency The currency used in the transaction, enforced by [CurrencyEnum].
 * @property status The current status of the transaction, represented by [TransactionStatusEnum].
 * @property timestamp The date and time when the transaction was recorded.
 */
@Table(name = "transaction", schema = "bank")
data class TransactionEntity(
    @Id
    @Column("id")
    val id: Long? = null,
    @Column("from_account_id")
    val fromAccountId: Long,
    @Column("to_account_id")
    val toAccountId: Long,
    @Column("amount")
    val amount: BigDecimal,
    @Column("currency")
    val currency: CurrencyEnum,
    @Column("status")
    val status: TransactionStatusEnum,
    @Column("timestamp")
    val timestamp: LocalDateTime = LocalDateTime.now()
)

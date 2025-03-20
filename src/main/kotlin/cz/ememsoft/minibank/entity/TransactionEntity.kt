package cz.ememsoft.minibank.entity

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Represents a transaction entity within the system.
 *
 * This entity is mapped to the `transaction` table in the `bank` schema.
 * Each transaction records a movement of funds between two accounts.
 *
 * <p><strong>Database Mapping:</strong></p>
 * <ul>
 *   <li>Table: `bank.transaction`</li>
 *   <li>Primary Key: `id`</li>
 *   <li>Foreign Keys:
 *     <ul>
 *       <li>`source_account_id` references the source account in the `account` table</li>
 *       <li>`target_account_id` references the target account in the `account` table</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>{@code id} - The unique identifier for the transaction.</li>
 *   <li>{@code sourceAccountId} - The identifier of the account from which the funds are transferred.</li>
 *   <li>{@code targetAccountId} - The identifier of the account to which the funds are transferred.</li>
 *   <li>{@code amount} - The amount of money transferred.</li>
 *   <li>{@code currency} - The currency of the transaction.</li>
 *   <li>{@code timestamp} - The date and time when the transaction occurred.</li>
 *   <li>{@code status} - The status of the transaction (e.g., completed, failed).</li>
 *   <li>{@code reference} - A reference or description for the transaction.</li>
 * </ul>
 */
@Table(name = "transaction", schema = "bank")
data class TransactionEntity(
    /** Unique identifier for the transaction */
    @Id
    @Column("id")
    val id: Long,

    /** Identifier of the source account */
    @Column("source_account_id")
    val sourceAccountId: Long,

    /** Identifier of the target account */
    @Column("target_account_id")
    val targetAccountId: Long,

    /** Amount transferred */
    @Column("amount")
    val amount: BigDecimal,

    /** Currency of the transaction */
    @Column("currency")
    val currency: String,

    /** Timestamp of when the transaction occurred */
    @Column("timestamp")
    val timestamp: LocalDateTime,

    /** Status of the transaction */
    @Column("status")
    val status: TransactionStatus,

    /** Reference or description for the transaction */
    @Column("reference")
    val reference: String
)
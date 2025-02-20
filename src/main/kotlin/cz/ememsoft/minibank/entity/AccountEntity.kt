package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

/**
 * Represents a bank account entity within the system.
 *
 * This entity is mapped to the `account` table in the `bank` schema.
 * Each account is associated with a specific client through a Many-to-One relationship.
 *
 * <p><strong>Database Mapping:</strong></p>
 * <ul>
 *   <li>Table: `bank.account`</li>
 *   <li>Primary Key: `id`</li>
 *   <li>Foreign Key: `client_id` references the client record in the `client` table</li>
 * </ul>
 *
 * <p><strong>Fields:</strong></p>
 * <ul>
 *   <li>{@code id} - The unique identifier for the account.</li>
 *   <li>{@code name} - The name assigned to the account.</li>
 *   <li>{@code clientId} - The identifier of the client who owns this account.</li>
 *   <li>{@code balance} - The current balance of the account.</li>
 *   <li>{@code accountType} - The type of the account (e.g., CLASSIC, SAVINGS, INVESTMENT).</li>
 *   <li>{@code currency} - The currency of the account, enforced by [CurrencyEnum] for type safety.</li>
 * </ul>
 *
 * @see cz.ememsoft.minibank.enumeration.AccountTypeEnum
 * @see cz.ememsoft.minibank.enumeration.CurrencyEnum
 */
@Table(name = "account", schema = "bank")
data class AccountEntity(
    @Id
    @Column("id")
    val id: Long,
    @Column("account_name")
    val name: String,
    @Column("client_id")
    val clientId: Long,
    @Column("balance")
    val balance: BigDecimal,
    @Column("account_type")
    val accountType: AccountTypeEnum,
    @Column("currency")
    val currency: CurrencyEnum
)

package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.AccountTypeEnum
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
 *   <li>{@code id} - The unique identifier for the account, mapped to column `id`.</li>
 *   <li>{@code name} - The name assigned to the account, mapped to column `account_name`. This field cannot be null.</li>
 *   <li>{@code clientId} - The identifier of the client owning this account, mapped to column `client_id`. This field cannot be null.</li>
 *   <li>{@code balance} - The current balance of the account, mapped to column `balance`. This field cannot be null.</li>
 *   <li>{@code accountType} - The type of the account, mapped to column `account_type` and represented by [AccountTypeEnum].</li>
 * </ul>
 *
 * @see cz.ememsoft.minibank.enumeration.AccountTypeEnum
 */
@Table(name = "account", schema = "bank")
data class AccountEntity(
    /** Unique identifier for the account */
    @Id
    @Column("id")
    val id: Long,

    /** Name of the account */
    @Column("account_name")
    val name: String,

    /** Identifier of the client owning this account */
    @Column("client_id")
    val clientId: Long,

    /** Current balance of the account */
    @Column("balance")
    val balance: BigDecimal,

    /** Type of the account (e.g., checking, savings) */
    @Column("account_type")
    val accountType: AccountTypeEnum,
)

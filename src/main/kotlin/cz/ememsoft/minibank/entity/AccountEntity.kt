package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.math.BigDecimal

/**
 * Represents a bank account entity in the system.
 *
 * This entity is mapped to the `account` table within the `bank` schema.
 * Each account is linked to a specific client through a Many-to-One relationship.
 *
 * ## Database Mapping:
 * - Table: `bank.account`
 * - Primary Key: `account_id`
 * - Foreign Key: `client_id` references `client`
 *
 * ## Fields:
 * @property id The unique identifier for the account, mapped to `account_id`.
 * @property name The name assigned to the account, mapped to `account_name`. Cannot be null.
 * @property clientId The ID of the client associated with this account, mapped to `client_id`. Cannot be null.
 * @property balance The current balance of the account, mapped to `balance`. Cannot be null.
 * @property accountType The type of the account, mapped to `account_type`. Uses [AccountTypeEnum].
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

    /** Foreign key referencing the client owning this account */
    @Column("client_id")
    val clientId: Long,

    /** Account balance */
    @Column("balance")
    val balance: BigDecimal,

    /** Type of account (e.g., checking, savings) */
    @Column("account_type")
    val accountType: AccountTypeEnum,
)

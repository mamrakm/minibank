package cz.ememsoft.minibank.entity

import cz.ememsoft.minibank.enum.AccountTypeEnum
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * Entity representing a bank account in the system.
 *
 * This entity is mapped to the `account` table within the `bank` schema.
 * Each account is associated with a specific client through a `@ManyToOne` relationship.
 *
 * @property id The unique identifier for the account, generated automatically.
 * @property name The name assigned to the account. Cannot be null.
 * @property clientEntity The client associated with this account. Cannot be null.
 * @property balance The current balance of the account. Cannot be null.
 * @property accountType The type of the account, represented as an [AccountTypeEnum]. Cannot be null.
 */
@Entity
@Table(name = "account", schema = "bank")
class AccountEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    val id: Long,

    @Column(name = "account_name", nullable = false)
    @NotNull
    val name: String,

    @NotNull(message = "Client must be provided")
    @ManyToOne(cascade = [CascadeType.REFRESH], optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    val clientEntity: ClientEntity,

    @Column(name = "balance", nullable = false)
    @NotNull
    val balance: BigDecimal,

    @Column(name = "account_type", nullable = false)
    @NotNull
    val accountType: AccountTypeEnum,
) {

    /**
     * Compares this [AccountEntity] with another object for equality.
     *
     * Two [AccountEntity] objects are considered equal if their `id` and associated
     * [ClientEntity] instances are identical.
     *
     * @param other The object to compare with this entity.
     * @return `true` if the objects are equal, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AccountEntity) return false

        if (id != other.id) return false
        if (clientEntity != other.clientEntity) return false

        return true
    }

    /**
     * Computes the hash code for this [AccountEntity].
     *
     * The hash code is derived from the `id` and `clientEntity` properties,
     * ensuring that two equal entities produce the same hash code.
     *
     * @return The computed hash code of this entity.
     */
    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + clientEntity.hashCode()
        return result
    }
}

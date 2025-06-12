package cz.ememsoft.minibank.enumeration

import java.io.Serializable

/**
 * Represents the type of a bank account.
 */
enum class AccountTypeEnum : Serializable {
    /**
     * A standard account for daily transactions, often called a checking or current account.
     */
    CHECKING,

    /**
     * A standard account for daily transactions, often used in Europe.
     */
    CURRENT,

    /**
     * An account for saving money, which may have different interest rates or rules.
     */
    SAVINGS,

    /**
     * An account for holding and trading investments.
     */
    INVESTMENT,

    /**
     * An account specifically for business or corporate clients.
     */
    BUSINESS,

    /**
     * A specialized account for students, often with lower fees.
     */
    STUDENT,

    /**
     * An account held by two or more individuals.
     */
    JOINT,

    /**
     * An account representing a loan owed by the client.
     */
    LOAN,

    /**
     * A basic, standard bank account.
     */
    CLASSIC;

    /**
     * Provides a more user-friendly string representation.
     */
    override fun toString(): String {
        return name.lowercase().replaceFirstChar { it.uppercase() }
    }

    /**
     * User-friendly display name
     */
    val displayName: String
        get() = when (this) {
            CHECKING -> "Checking Account"
            CURRENT -> "Current Account"
            SAVINGS -> "Savings Account"
            INVESTMENT -> "Investment Account"
            BUSINESS -> "Business Account"
            STUDENT -> "Student Account"
            JOINT -> "Joint Account"
            LOAN -> "Loan Account"
            CLASSIC -> "Classic Account"
        }
}
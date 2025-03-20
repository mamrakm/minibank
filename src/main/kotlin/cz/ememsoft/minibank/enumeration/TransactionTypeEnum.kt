package cz.ememsoft.minibank.enumeration

import java.io.Serializable

/**
 * Enumeration for the different types of transactions supported by the system.
 */
enum class TransactionTypeEnum : Serializable {
    /** Transfer between two accounts within the system */
    TRANSFER,

    /** Money added to an account from an external source */
    DEPOSIT,

    /** Money withdrawn from an account to an external destination */
    WITHDRAWAL,

    /** Interest paid into an account */
    INTEREST,

    /** Fee charged for a service */
    FEE
}
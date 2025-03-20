package cz.ememsoft.minibank.entity

import java.io.Serializable

/**
 * Enum representing the possible states of a transaction.
 *
 * Transactions can be in one of three states:
 * - PENDING: The transaction has been initiated but not yet completed
 * - COMPLETED: The transaction has been successfully completed
 * - FAILED: The transaction failed to complete
 */
enum class TransactionStatus : Serializable {
    PENDING,
    COMPLETED,
    FAILED
}
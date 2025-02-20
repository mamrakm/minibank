package cz.ememsoft.minibank.enumeration

/**
 * Enum representing the status of a financial transaction.
 *
 * @property SUCCESS Indicates that the transaction was processed successfully.
 * @property PENDING Indicates that the transaction is currently pending.
 * @property FAILED Indicates that the transaction has failed.
 * @property CANCELLED Indicates that the transaction was cancelled.
 */
enum class TransactionStatusEnum {
    SUCCESS,
    PENDING,
    FAILED,
    CANCELLED
}

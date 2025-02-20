package cz.ememsoft.minibank.exception

/**
 * Base exception for all transaction-related errors in the application.
 *
 * @param message The detail message about the exception.
 */
sealed class TransactionException(message: String) : RuntimeException(message)

/**
 * Exception thrown when the sender account does not have sufficient funds.
 *
 * @param message The detail message about the exception.
 */
class InsufficientFundsException(message: String) : TransactionException(message)

/**
 * Exception thrown when there is a currency mismatch between the involved accounts.
 *
 * @param message The detail message about the exception.
 */
class CurrencyMismatchException(message: String) : TransactionException(message)

/**
 * Exception thrown when processing the transaction fails due to an unexpected error.
 *
 * @param message The detail message about the exception.
 */
class TransactionProcessingException(message: String) : TransactionException(message)

package cz.ememsoft.minibank.exception

/**
 * Base exception for all transaction-related errors in the application.
 *
 * This class serves as a parent for specific transaction exceptions,
 * allowing centralized handling of all transaction-related issues.
 *
 * @param message The detail message about the exception.
 */
sealed class TransactionException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a transaction fails due to insufficient funds.
 *
 * This exception is used when the source account does not have enough balance
 * to complete the requested transaction.
 *
 * @param message The detail message about the exception.
 */
class InsufficientFundsException(message: String) : TransactionException(message)

/**
 * Exception thrown when an attempt is made to transfer money to the same account.
 *
 * This exception is used when the source and target accounts are the same,
 * which is not allowed in a transfer operation.
 *
 * @param message The detail message about the exception.
 */
class SameAccountTransferException(message: String) : TransactionException(message)

/**
 * Exception thrown when a transaction fails due to currency mismatch.
 *
 * This exception is used when the source and target accounts have different
 * currencies, and the system does not support cross-currency transfers.
 *
 * @param message The detail message about the exception.
 */
class CurrencyMismatchException(message: String) : TransactionException(message)

/**
 * Exception thrown when a transaction operation fails.
 *
 * This is a general exception for transaction failures that don't fit
 * into the more specific categories.
 *
 * @param message The detail message about the exception.
 */
class TransactionFailedException(message: String) : TransactionException(message)

/**
 * Exception thrown when a transaction is not found.
 *
 * This exception is used when trying to retrieve a transaction by ID
 * and no corresponding record is found.
 *
 * @param message The detail message about the exception.
 */
class TransactionNotFoundException(message: String) : TransactionException(message)

/**
 * Exception thrown when a negative or zero amount is provided for a transaction.
 *
 * @param message The detail message about the exception.
 */
class InvalidTransactionAmountException(message: String) : TransactionException(message)
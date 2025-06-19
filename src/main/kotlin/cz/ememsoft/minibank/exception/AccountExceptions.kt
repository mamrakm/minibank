package cz.ememsoft.minibank.exception

/**
 * Base exception for all account-related errors in the application.
 */
sealed class AccountException(message: String) : RuntimeException(message)

/**
 * Exception thrown when an account is not found.
 */
class AccountNotFoundException(message: String) : AccountException(message)

/**
 * Exception thrown when attempting to operate on a frozen account.
 */
class AccountFrozenException(message: String) : AccountException(message)

/**
 * Exception thrown when attempting to operate on a closed account.
 */
class AccountClosedException(message: String) : AccountException(message)

/**
 * Exception thrown when attempting to create an account with invalid data.
 */
class InvalidAccountDataException(message: String) : AccountException(message)

/**
 * Exception thrown when an account operation violates balance constraints.
 */
class InsufficientBalanceException(message: String) : AccountException(message)

/**
 * Exception thrown when attempting to access an account not owned by the client.
 */
class UnauthorizedAccountAccessException(message: String) : AccountException(message)
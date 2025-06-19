package cz.ememsoft.minibank.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * Global exception handler for the application.
 *
 * This class handles exceptions thrown by controllers and provides consistent
 * responses for specific types of errors. It uses Spring's `@RestControllerAdvice`
 * to apply these handlers globally.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * Handles [ClientNotFoundException].
     *
     * Returns a response with HTTP status `404 Not Found` and an error message
     * indicating that the client could not be found.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(ClientNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleClientNotFoundException(exception: ClientNotFoundException): Map<String, String> {
        return mapOf(
            "error" to "Client Not Found",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [DuplicateClientException].
     *
     * Returns a response with HTTP status `409 Conflict` and an error message
     * indicating that a duplicate client entry exists.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(DuplicateClientException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDuplicateClientException(exception: DuplicateClientException): Map<String, String> {
        return mapOf(
            "error" to "Duplicate Client",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [InvalidClientDataException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that the client data provided is invalid.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(InvalidClientDataException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidClientDataException(exception: InvalidClientDataException): Map<String, String> {
        return mapOf(
            "error" to "Invalid Client Data",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [ClientDeletionException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that the client could not be deleted due to associated data.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(ClientDeletionException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleClientDeletionException(exception: ClientDeletionException): Map<String, String> {
        return mapOf(
            "error" to "Client Deletion Failed",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [AccountNotFoundException].
     *
     * Returns a response with HTTP status `404 Not Found` and an error message
     * indicating that the account could not be found.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(AccountNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleAccountNotFoundException(exception: AccountNotFoundException): Map<String, String> {
        return mapOf(
            "error" to "Account Not Found",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [AccountFrozenException].
     *
     * Returns a response with HTTP status `423 Locked` and an error message
     * indicating that the account is frozen.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(AccountFrozenException::class)
    @ResponseStatus(HttpStatus.LOCKED)
    fun handleAccountFrozenException(exception: AccountFrozenException): Map<String, String> {
        return mapOf(
            "error" to "Account Frozen",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [AccountClosedException].
     *
     * Returns a response with HTTP status `410 Gone` and an error message
     * indicating that the account is closed.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(AccountClosedException::class)
    @ResponseStatus(HttpStatus.GONE)
    fun handleAccountClosedException(exception: AccountClosedException): Map<String, String> {
        return mapOf(
            "error" to "Account Closed",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [InvalidAccountDataException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that the account data is invalid.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(InvalidAccountDataException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidAccountDataException(exception: InvalidAccountDataException): Map<String, String> {
        return mapOf(
            "error" to "Invalid Account Data",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [InsufficientBalanceException].
     *
     * Returns a response with HTTP status `402 Payment Required` and an error message
     * indicating insufficient balance for the operation.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(InsufficientBalanceException::class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    fun handleInsufficientBalanceException(exception: InsufficientBalanceException): Map<String, String> {
        return mapOf(
            "error" to "Insufficient Balance",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [UnauthorizedAccountAccessException].
     *
     * Returns a response with HTTP status `403 Forbidden` and an error message
     * indicating unauthorized access to the account.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(UnauthorizedAccountAccessException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleUnauthorizedAccountAccessException(exception: UnauthorizedAccountAccessException): Map<String, String> {
        return mapOf(
            "error" to "Unauthorized Account Access",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [ClientSuspendedException].
     *
     * Returns a response with HTTP status `423 Locked` and an error message
     * indicating that the client is suspended.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(ClientSuspendedException::class)
    @ResponseStatus(HttpStatus.LOCKED)
    fun handleClientSuspendedException(exception: ClientSuspendedException): Map<String, String> {
        return mapOf(
            "error" to "Client Suspended",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [ClientInactiveException].
     *
     * Returns a response with HTTP status `410 Gone` and an error message
     * indicating that the client is inactive.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(ClientInactiveException::class)
    @ResponseStatus(HttpStatus.GONE)
    fun handleClientInactiveException(exception: ClientInactiveException): Map<String, String> {
        return mapOf(
            "error" to "Client Inactive",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [ClientUnauthorizedException].
     *
     * Returns a response with HTTP status `403 Forbidden` and an error message
     * indicating that the client is unauthorized for administrative operations.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(ClientUnauthorizedException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleClientUnauthorizedException(exception: ClientUnauthorizedException): Map<String, String> {
        return mapOf(
            "error" to "Client Unauthorized",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [TransactionNotFoundException].
     *
     * Returns a response with HTTP status `404 Not Found` and an error message
     * indicating that the transaction could not be found.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(TransactionNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleTransactionNotFoundException(exception: TransactionNotFoundException): Map<String, String> {
        return mapOf(
            "error" to "Transaction Not Found",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [InsufficientFundsException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that the source account has insufficient funds.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(InsufficientFundsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInsufficientFundsException(exception: InsufficientFundsException): Map<String, String> {
        return mapOf(
            "error" to "Insufficient Funds",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [SameAccountTransferException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that transfer to the same account is not allowed.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(SameAccountTransferException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleSameAccountTransferException(exception: SameAccountTransferException): Map<String, String> {
        return mapOf(
            "error" to "Invalid Transfer",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [CurrencyMismatchException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that the currencies of the accounts do not match.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(CurrencyMismatchException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCurrencyMismatchException(exception: CurrencyMismatchException): Map<String, String> {
        return mapOf(
            "error" to "Currency Mismatch",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [InvalidTransactionAmountException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that the transaction amount is invalid.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(InvalidTransactionAmountException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleInvalidTransactionAmountException(exception: InvalidTransactionAmountException): Map<String, String> {
        return mapOf(
            "error" to "Invalid Transaction Amount",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [TransactionFailedException].
     *
     * Returns a response with HTTP status `500 Internal Server Error` and an error message
     * indicating that the transaction failed.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(TransactionFailedException::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleTransactionFailedException(exception: TransactionFailedException): Map<String, String> {
        return mapOf(
            "error" to "Transaction Failed",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles [IllegalArgumentException].
     *
     * Returns a response with HTTP status `400 Bad Request` and an error message
     * indicating that an invalid argument was provided.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): Map<String, String> {
        return mapOf(
            "error" to "Invalid Argument",
            "message" to exception.message.orEmpty()
        )
    }

    /**
     * Handles generic exceptions.
     *
     * Returns a response with HTTP status `500 Internal Server Error` and an error
     * message indicating that an unexpected error occurred.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGenericException(exception: Exception): Map<String, String> {
        return mapOf(
            "error" to "Internal Server Error",
            "message" to exception.message.orEmpty()
        )
    }
}
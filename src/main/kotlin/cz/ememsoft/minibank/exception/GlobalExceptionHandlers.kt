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

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
     * Handles [CustomerNotFoundException].
     *
     * Returns a response with HTTP status `404 Not Found` and an error message
     * indicating that the customer could not be found.
     *
     * @param exception The exception instance.
     * @return A map containing the error type and the exception message.
     */
    @ExceptionHandler(CustomerNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCustomerNotFoundException(exception: CustomerNotFoundException): Map<String, String> {
        return mapOf(
            "error" to "Customer Not Found",
            "message" to exception.message!!
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
            "message" to exception.message!!
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
            "message" to exception.message!!
        )
    }
}

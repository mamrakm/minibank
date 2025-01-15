package cz.ememsoft.minibank.helper

import cz.ememsoft.minibank.exception.CustomerNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(CustomerNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleCustomerNotFoundException(exception: CustomerNotFoundException): Map<String, String> {
        return mapOf(
            "error" to "Customer Not Found",
            "message" to exception.message!!
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(exception: IllegalArgumentException): Map<String, String> {
        return mapOf(
            "error" to "Invalid Argument",
            "message" to exception.message!!
        )
    }

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGenericException(exception: Exception): Map<String, String> {
        return mapOf(
            "error" to "Internal Server Error",
            "message" to exception.message!!
        )
    }
}

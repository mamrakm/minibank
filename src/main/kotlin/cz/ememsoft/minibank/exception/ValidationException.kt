package cz.ememsoft.minibank.exception

/**
 * Exception thrown when validation fails.
 * Follows Single Responsibility Principle.
 */
class ValidationException(
    message: String,
    cause: Throwable? = null
) : IllegalArgumentException(message, cause)
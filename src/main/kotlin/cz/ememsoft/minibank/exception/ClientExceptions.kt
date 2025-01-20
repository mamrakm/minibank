package cz.ememsoft.minibank.exception

/**
 * Base exception for all client-related errors in the application.
 *
 * This class serves as a parent for specific client exceptions,
 * allowing centralised handling of all client-related issues.
 *
 * @param message The detail message about the exception.
 */
open class ClientException(message: String) : RuntimeException(message)

/**
 * Exception thrown when a client is not found.
 *
 * This is used in scenarios where a client lookup by ID, email, or
 * other unique identifiers fails to find a matching record.
 *
 * @param message The detail message about the exception.
 */
class ClientNotFoundException(message: String) : ClientException(message)

/**
 * Exception thrown when attempting to save a duplicate client.
 *
 * This is used in scenarios where a new client record conflicts with
 * an existing one (e.g., duplicate email).
 *
 * @param message The detail message about the exception.
 */
class DuplicateClientException(message: String) : ClientException(message)

/**
 * Exception thrown when client data is invalid.
 *
 * This is used in scenarios where the provided client information
 * does not meet required validation or business rules.
 *
 * @param message The detail message about the exception.
 */
class InvalidClientDataException(message: String) : ClientException(message)

/**
 * Exception thrown when a client cannot be deleted.
 *
 * This is used in scenarios where a client deletion fails due to
 * associated data (e.g., foreign key constraints or dependent records).
 *
 * @param message The detail message about the exception.
 */
class ClientDeletionException(message: String) : ClientException(message)

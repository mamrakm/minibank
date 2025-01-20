package cz.ememsoft.minibank.exception

/**
 * Exception thrown when a requested client is not found in the system.
 *
 * This is a custom exception that extends [RuntimeException] and is typically
 * used to indicate that a client lookup by ID or other criteria has failed.
 */
class ClientNotFoundException : RuntimeException()

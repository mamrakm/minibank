package cz.ememsoft.minibank.api.dto.request

/**
 * Request object for deleting a client.
 *
 * This DTO is used to encapsulate the ID of the client to be deleted.
 *
 * @property id The unique identifier of the client to delete.
 */
data class ClientDeleteRequest(val id: Long)

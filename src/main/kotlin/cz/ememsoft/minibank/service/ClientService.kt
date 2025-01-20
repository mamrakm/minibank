package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto

/**
 * Service interface for managing clients in the application.
 */
interface ClientService {
    /**
     * Retrieves a client by their ID.
     *
     * @param id The ID of the client to retrieve.
     * @return The corresponding [ClientDto] for the given ID.
     */
    fun getclient(id: Long): ClientDto

    /**
     * Retrieves a list of all clients, mapped to [ClientDto] objects.
     *
     * @return A list of all clients as [ClientDto].
     */
    fun getclientAccounts()

    /**
     * Retrieves transactions associated with clients.
     *
     * **Note:** This method is intended to fetch client transactions but has no detailed implementation here.
     */
    fun getclientTransactions()

    /**
     * Retrieves a list of all clients.
     *
     * @return A list of all clients as [ClientDto].
     */
    fun getAllclients(): List<ClientDto>

    /**
     * Saves a new client to the database.
     *
     * @param clientDto The DTO containing client information to save.
     * @return The ID of the saved client.
     */
    fun saveclient(clientDto: ClientDto): Long

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     */
    fun deleteclient(id: Long)

    /**
     * Updates an existing client's information.
     *
     * **Note:** This method is intended for updating client details but has no detailed implementation here.
     */
    fun updateclient()

}

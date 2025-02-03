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
    suspend fun getClient(id: Long): ClientDto

    /**
     * Retrieves all clients in the system.
     *
     * @return A list of all clients as [ClientDto].
     */
    suspend fun getAllClients(): List<ClientDto>

    /**
     * Saves a new client to the database.
     *
     * @param clientDto The DTO containing client information to save.
     * @return The ID of the saved client.
     */
    suspend fun saveClient(clientDto: ClientDto): Long

    /**
     * Updates an existing client's information.
     *
     * @param id The ID of the client to update.
     * @param updatedClientDto The updated client data.
     * @return The updated [ClientDto].
     */
    suspend fun updateClient(id: Long, updatedClientDto: ClientDto): ClientDto

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     */
    suspend fun deleteClient(id: Long)

    /**
     * Searches for a client by their email.
     *
     * @param email The email address to search for.
     * @return The corresponding [ClientDto] if found, or null otherwise.
     */
    suspend fun findClientByEmail(email: String): ClientDto?

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A list of clients matching the first name as [ClientDto].
     */
    suspend fun findClientsByFirstName(firstName: String): List<ClientDto>
}

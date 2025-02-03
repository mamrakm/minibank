package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.ClientDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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
    fun getClient(id: Long): Mono<ClientDto>

    /**
     * Retrieves all clients in the system.
     *
     * @return A list of all clients as [ClientDto].
     */
    fun getAllClients(): Flux<ClientDto>

    /**
     * Saves a new client to the database.
     *
     * @param clientDto The DTO containing client information to save.
     * @return The ID of the saved client.
     */
    fun saveClient(clientDto: ClientDto): Mono<Long>

    /**
     * Updates an existing client's information.
     *
     * @param id The ID of the client to update.
     * @param updatedClientDto The updated client data.
     * @return The updated [ClientDto].
     */
    fun updateClient(id: Long, updatedClientDto: ClientDto): Mono<ClientDto>

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     */
    fun deleteClient(id: Long): Mono<Void>

    /**
     * Searches for a client by their email.
     *
     * @param email The email address to search for.
     * @return The corresponding [ClientDto] if found, or null otherwise.
     */
    fun findClientByEmail(email: String): Mono<ClientDto>

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A list of clients matching the first name as [ClientDto].
     */
    fun findClientsByFirstName(firstName: String): Flux<ClientDto>
}

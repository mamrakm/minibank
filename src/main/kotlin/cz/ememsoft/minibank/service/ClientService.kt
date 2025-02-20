package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service interface for managing clients in the application.
 *
 * This interface defines methods for retrieving, saving, updating, and deleting
 * client information in a reactive manner using Project Reactor.
 */
interface ClientService {

    /**
     * Retrieves a client by their ID.
     *
     * @param id The unique identifier of the client to retrieve.
     * @return A [Mono] emitting the corresponding [ClientDto], or an error if the client is not found.
     */
    fun getClient(id: Long): Mono<ClientDto>

    /**
     * Retrieves all clients in the system.
     *
     * @return A [Flux] emitting all clients as [ClientDto].
     */
    fun getAllClients(): Flux<ClientDto>

    /**
     * Saves a new client to the database.
     *
     * This method checks for duplicate email addresses before saving the client.
     *
     * @param clientDto The DTO containing client information to save.
     * @return A [Mono] emitting the ID of the saved client, or an error if a duplicate exists.
     */
    fun createClient(clientDto: CreateClientRequestDto): Mono<CreateClientResponseDto>

    /**
     * Updates an existing client's information.
     *
     * @param id The ID of the client to update.
     * @param updatedClientDto The updated client data.
     * @return A [Mono] emitting the updated [ClientDto], or an error if the client is not found.
     */
    fun updateClient(updatedClientDto: ClientDto): Mono<ClientDto>

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     * @return A [Mono] signaling completion of the deletion process, or an error if the client is not found.
     */
    fun deleteClient(id: Long): Mono<Void>

    /**
     * Searches for a client by their email.
     *
     * @param email The email address to search for.
     * @return A [Mono] emitting the corresponding [ClientDto] if found, or an empty Mono if not found.
     */
    fun findClientByEmail(email: String): Mono<ClientDto>

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A [Flux] emitting clients matching the first name as [ClientDto].
     */
    fun findClientsByFirstName(firstName: String): Flux<ClientDto>
}

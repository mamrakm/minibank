package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.service.ClientService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.jpa.domain.AbstractPersistable_.id
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for managing clients in the application.
 *
 * Provides endpoints for CRUD operations and search functionality related to clients.
 *
 * @property clientService The service layer for client-related operations.
 * @property clientRequestMapper The mapper for converting request DTOs to internal DTOs.
 */
@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientService: ClientService,
    private val clientMapper: ClientMapper,
) {

    /**
     * Retrieves a client by their ID.
     *
     * @param id The ID of the client to retrieve.
     * @return The corresponding [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClient(@PathVariable id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return clientService.getClient(id)
            .doOnSuccess { logger.debug { "Successfully fetched client: $it" } }
            .doOnError { logger.error(it) { "Error fetching client with ID: $id" } }
    }

    /**
     * Retrieves all clients in the system.
     *
     * @return A list of all clients as [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching all clients" }
        return clientService.getAllClients()
            .doOnComplete { logger.debug { "Successfully fetched all clients" } }
            .doOnError { logger.error(it) { "Error fetching all clients" } }
    }

    /**
     * Creates a new client.
     *
     * @param createClientRequestDto The request body containing client data.
     * @return The ID of the created client.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun saveClient(@RequestBody createClientRequestDto: CreateClientRequestDto): Mono<CreateClientResponseDto> {
        logger.info { "Creating new client: $createClientRequestDto" }
        return clientService.createClient(createClientRequestDto)
            .doOnSuccess { logger.info { "Successfully created client with ID: $it" } }
            .doOnError { logger.error(it) { "Error creating client" } }
    }

    /**
     * Updates an existing client's information.
     *
     * @param id The ID of the client to update.
     * @param createClientRequestDto The request body containing updated client data.
     * @return The updated [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateClient(
        @RequestBody updatedClientDto: ClientDto,
    ): Mono<ClientDto> {
        val id = updatedClientDto.id
        logger.info { "Updating client with ID: $id" }
        return clientService.updateClient(updatedClientDto)
            .doOnSuccess { logger.info { "Successfully updated client with ID: $id" } }
            .doOnError { logger.error(it) { "Error updating client with ID: $id" } }
    }

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteClient(@PathVariable id: Long) {
        logger.info { "Deleting client with ID: $id" }
        clientService.deleteClient(id)
            .doOnSuccess { logger.info { "Successfully deleted client with ID: $id" } }
            .doOnError { logger.error(it) { "Error deleting client with ID: $id" } }
    }

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A list of clients matching the first name as [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search-by-name/{firstName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findClientsByFirstName(@PathVariable firstName: String): Flux<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientService.findClientsByFirstName(firstName)
            .doOnComplete { logger.info { "Successfully completed search for clients with first name: $firstName" } }
            .doOnError { logger.error(it) { "Error searching for clients with first name: $firstName" } }
    }

    /**
     * Searches for a client by their email.
     *
     * @param email The email address of the client to search for.
     * @return The corresponding [ClientDto] or null if not found.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search-by-email/{email}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findClientByEmail(@PathVariable email: String): Mono<ClientDto> {
        logger.info { "Searching for client with email: $email" }
        return clientService.findClientByEmail(email)
            .doOnSuccess { logger.info { "Successfully found client with email: $email" } }
            .doOnError { logger.error(it) { "Error searching for client with email: $email" } }
    }
}

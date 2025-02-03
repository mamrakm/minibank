package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.dto.request.CreateClientRequest
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.mapper.ClientMapper
import cz.ememsoft.minibank.service.ClientService
import io.github.oshai.kotlinlogging.KotlinLogging
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
    suspend fun getClient(@PathVariable id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return clientService.getClient(id)
    }

    /**
     * Retrieves all clients in the system.
     *
     * @return A list of all clients as [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching all clients" }
        return clientService.getAllClients()
    }

    /**
     * Creates a new client.
     *
     * @param createClientRequest The request body containing client data.
     * @return The ID of the created client.
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun saveClient(@RequestBody createClientRequest: CreateClientRequest): Mono<Long> {
        logger.info { "Creating new client: $createClientRequest" }
        val clientDto = clientMapper.requestToDto(createClientRequest)
        return clientService.saveClient(clientDto)
    }

    /**
     * Updates an existing client's information.
     *
     * @param id The ID of the client to update.
     * @param createClientRequest The request body containing updated client data.
     * @return The updated [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun updateClient(
        @PathVariable id: Long,
        @RequestBody createClientRequest: CreateClientRequest,
    ): Mono<ClientDto> {
        logger.info { "Updating client with ID: $id" }
        val updatedClientDto = clientMapper.requestToDto(createClientRequest)
        return clientService.updateClient(id, updatedClientDto)
    }

    /**
     * Deletes a client by their ID.
     *
     * @param id The ID of the client to delete.
     */
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    suspend fun deleteClient(@PathVariable id: Long) {
        logger.info { "Deleting client with ID: $id" }
        clientService.deleteClient(id)
    }

    /**
     * Searches for clients by their first name.
     *
     * @param firstName The first name of the client(s) to search for.
     * @return A list of clients matching the first name as [ClientDto].
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search-by-name/{firstName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun findClientsByFirstName(@PathVariable firstName: String): Flux<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientService.findClientsByFirstName(firstName)
    }

    /**
     * Searches for a client by their email.
     *
     * @param email The email address of the client to search for.
     * @return The corresponding [ClientDto] or null if not found.
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search-by-email/{email}", produces = [MediaType.APPLICATION_JSON_VALUE])
    suspend fun findClientByEmail(@PathVariable email: String): Mono<ClientDto> {
        logger.info { "Searching for client with email: $email" }
        return clientService.findClientByEmail(email)
    }
}

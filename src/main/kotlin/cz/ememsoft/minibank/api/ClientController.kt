package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.client.request.CreateClientRequestDto
import cz.ememsoft.minibank.api.client.response.CreateClientResponseDto
import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.service.ClientService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
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
 * REST controller for managing clients with validation enabled.
 */
@RestController
@RequestMapping("/clients")
@Validated
class ClientController(
    private val clientService: ClientService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClient(@PathVariable id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return clientService.getClient(id)
            .doOnSuccess { logger.debug { "Successfully fetched client: $it" } }
            .doOnError { logger.error(it) { "Error fetching client with ID: $id" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching all clients" }
        return clientService.getAllClients()
            .doOnComplete { logger.debug { "Successfully fetched all clients" } }
            .doOnError { logger.error(it) { "Error fetching all clients" } }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createClient(@Valid @RequestBody createClientRequestDto: CreateClientRequestDto): Mono<CreateClientResponseDto> {
        logger.info { "Creating new client: $createClientRequestDto" }
        return clientService.createClient(createClientRequestDto)
            .doOnSuccess { logger.info { "Successfully created client with ID: ${it.id}" } }
            .doOnError { logger.error(it) { "Error creating client" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateClient(@PathVariable id: Long, @Valid @RequestBody updatedClientDto: ClientDto): Mono<ClientDto> {
        logger.info { "Updating client with ID: $id" }
        return clientService.updateClient(id, updatedClientDto)
            .doOnSuccess { logger.info { "Successfully updated client with ID: $id" } }
            .doOnError { logger.error(it) { "Error updating client with ID: $id" } }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteClient(@PathVariable id: Long): Mono<Void> {
        logger.info { "Deleting client with ID: $id" }
        return clientService.deleteClient(id)
            .doOnSuccess { logger.info { "Successfully deleted client with ID: $id" } }
            .doOnError { logger.error(it) { "Error deleting client with ID: $id" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search-by-name/{firstName}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findClientsByFirstName(@PathVariable firstName: String): Flux<ClientDto> {
        logger.info { "Searching for clients with first name: $firstName" }
        return clientService.findClientsByFirstName(firstName)
            .doOnComplete { logger.info { "Successfully completed search for clients with first name: $firstName" } }
            .doOnError { logger.error(it) { "Error searching for clients with first name: $firstName" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search-by-email/{email}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findClientByEmail(@PathVariable email: String): Mono<ClientDto> {
        logger.info { "Searching for client with email: $email" }
        return clientService.findClientByEmail(email)
            .doOnSuccess { logger.info { "Successfully found client with email: $email" } }
            .doOnError { logger.error(it) { "Error searching for client with email: $email" } }
    }
}

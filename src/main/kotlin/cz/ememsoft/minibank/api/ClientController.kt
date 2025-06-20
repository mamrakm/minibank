package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.service.ClientService
import cz.ememsoft.minibank.service.SecurityService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for managing clients with security and validation.
 * Clients can only access their own data unless they have admin role.
 */
@RestController
@RequestMapping("/clients")
@Validated
class ClientController(
    private val clientService: ClientService,
    private val securityService: SecurityService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClient(@PathVariable id: Long): Mono<ClientDto> {
        logger.info { "Fetching client with ID: $id" }
        return securityService.verifyClientAccess(id)
            .then(clientService.getClient(id))
            .doOnSuccess { logger.debug { "Successfully fetched client: $it" } }
            .doOnError { logger.error(it) { "Error fetching client with ID: $id" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllClients(): Flux<ClientDto> {
        logger.info { "Fetching clients for authenticated user" }
        return securityService.getAuthenticatedClient()
            .flatMapMany { authenticatedClient ->
                if (authenticatedClient.role == cz.ememsoft.minibank.enumeration.UserRoleEnum.ADMIN) {
                    logger.debug { "Admin user fetching all clients" }
                    clientService.getAllClients()
                } else {
                    logger.debug { "Regular user fetching own client data" }
                    clientService.getClient(authenticatedClient.id).flux()
                }
            }
            .doOnComplete { logger.debug { "Successfully fetched clients" } }
            .doOnError { logger.error(it) { "Error fetching clients" } }
    }

    // Client creation is handled by AuthenticationController for registration

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateClient(@PathVariable id: Long, @Valid @RequestBody updatedClientDto: ClientDto): Mono<ClientDto> {
        logger.info { "Updating client with ID: $id" }
        return securityService.verifyClientAccess(id)
            .then(clientService.updateClient(id, updatedClientDto))
            .doOnSuccess { logger.info { "Successfully updated client with ID: $id" } }
            .doOnError { logger.error(it) { "Error updating client with ID: $id" } }
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    fun deleteClient(@PathVariable id: Long): Mono<Void> {
        logger.info { "Deleting client with ID: $id" }
        return securityService.verifyClientAccess(id)
            .then(clientService.deleteClient(id))
            .doOnSuccess { logger.info { "Successfully deleted client with ID: $id" } }
            .doOnError { logger.error(it) { "Error deleting client with ID: $id" } }
    }

    // Search endpoints removed for security - clients can only access their own data
    // Admin users can use admin endpoints for client search functionality
}

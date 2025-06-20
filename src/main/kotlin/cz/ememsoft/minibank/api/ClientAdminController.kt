package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.service.ClientService
import cz.ememsoft.minibank.service.SecurityService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for administrative client operations.
 * These endpoints work with clients in any status and require ADMIN role.
 */
@RestController
@RequestMapping("/admin/clients")
@Validated
class ClientAdminController(
    private val clientService: ClientService,
    private val securityService: SecurityService
) {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClientAdmin(@PathVariable id: Long): Mono<ClientDto> {
        logger.info { "Admin: Fetching client with ID: $id (any status)" }
        return securityService.requireAdminRole()
            .then(clientService.getClientByIdAdmin(id))
            .doOnSuccess { logger.debug { "Admin: Successfully fetched client: $it" } }
            .doOnError { logger.error(it) { "Admin: Error fetching client with ID: $id" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAllClientsAdmin(): Flux<ClientDto> {
        logger.info { "Admin: Fetching all clients (any status)" }
        return securityService.requireAdminRole()
            .thenMany(clientService.getAllClientsAdmin())
            .doOnComplete { logger.debug { "Admin: Successfully fetched all clients" } }
            .doOnError { logger.error(it) { "Admin: Error fetching all clients" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/by-status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getClientsByStatus(@RequestParam status: ClientStatusEnum): Flux<ClientDto> {
        logger.info { "Admin: Fetching clients with status: $status" }
        return securityService.requireAdminRole()
            .thenMany(clientService.getClientsByStatus(status))
            .doOnComplete { logger.debug { "Admin: Successfully fetched clients with status: $status" } }
            .doOnError { logger.error(it) { "Admin: Error fetching clients with status: $status" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/status", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateClientStatus(@PathVariable id: Long, @RequestBody statusRequest: ClientStatusUpdateRequest): Mono<ClientDto> {
        logger.info { "Admin: Updating client status for ID: $id to ${statusRequest.status}" }
        return securityService.requireAdminRole()
            .then(clientService.updateClientStatus(id, statusRequest.status))
            .doOnSuccess { logger.info { "Admin: Successfully updated client status for ID: $id" } }
            .doOnError { logger.error(it) { "Admin: Error updating client status for ID: $id" } }
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateClientAdmin(@PathVariable id: Long, @Valid @RequestBody updatedClientDto: ClientDto): Mono<ClientDto> {
        logger.info { "Admin: Updating client with ID: $id (any status)" }
        return securityService.requireAdminRole()
            .then(clientService.updateClient(id, updatedClientDto))
            .doOnSuccess { logger.info { "Admin: Successfully updated client with ID: $id" } }
            .doOnError { logger.error(it) { "Admin: Error updating client with ID: $id" } }
    }
}

data class ClientStatusUpdateRequest(
    val status: ClientStatusEnum
)
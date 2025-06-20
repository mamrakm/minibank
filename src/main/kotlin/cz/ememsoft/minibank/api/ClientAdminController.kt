package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.dto.client.ClientStatusUpdateRequest
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import cz.ememsoft.minibank.enumeration.ClientStatusEnum
import cz.ememsoft.minibank.service.ClientService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
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
 * REST controller for administrative client operations.
 * Requires ADMIN role for all endpoints.
 */
@RestController
@RequestMapping("/admin/clients")
@Validated
class ClientAdminController(
    private val clientService: ClientService
) {

    /**
     * Creates a new client profile linked to a Keycloak user. Admin only.
     */
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    fun createClientProfile(@Valid @RequestBody request: CreateClientProfileRequestDto): Mono<ClientDto> {
        logger.info { "Admin request to create client profile for Keycloak ID: ${request.keycloakUserId}" }
        return clientService.createClientProfile(request)
            .doOnError { logger.error(it) { "Admin failed to create client profile for ${request.keycloakUserId}" } }
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    fun getClientAdmin(@PathVariable id: Long): Mono<ClientDto> {
        logger.info { "Admin: Fetching client with ID: $id (any status)" }
        return clientService.getClientByIdAdmin(id)
    }

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllClientsAdmin(): Flux<ClientDto> {
        logger.info { "Admin: Fetching all clients (any status)" }
        return clientService.getAllClientsAdmin()
    }

    @GetMapping("/status/{status}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    fun getClientsByStatus(@PathVariable status: ClientStatusEnum): Flux<ClientDto> {
        logger.info { "Admin: Fetching clients with status: $status" }
        return clientService.getClientsByStatus(status)
    }

    @PutMapping("/{id}/status", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasRole('ADMIN')")
    fun updateClientStatus(@PathVariable id: Long, @Valid @RequestBody request: ClientStatusUpdateRequest): Mono<ClientDto> {
        logger.info { "Admin: Updating client status for ID: $id to ${request.status}" }
        return clientService.updateClientStatus(id, request.status)
    }
}

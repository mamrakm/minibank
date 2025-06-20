package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.ClientDto
import cz.ememsoft.minibank.service.ClientService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/clients")
@Validated
class ClientController(
    private val clientService: ClientService
) {

    @GetMapping("/me", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    fun getMyProfile(@AuthenticationPrincipal jwt: Jwt): Mono<ClientDto> {
        val keycloakUserId = jwt.subject
        logger.info { "Fetching client profile for Keycloak user: $keycloakUserId" }
        return clientService.getClientByKeycloakId(keycloakUserId)
    }

    @PutMapping("/me", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    fun updateMyProfile(@AuthenticationPrincipal jwt: Jwt, @Valid @RequestBody updatedClientDto: ClientDto): Mono<ClientDto> {
        val keycloakUserId = jwt.subject
        logger.info { "Updating client profile for Keycloak user: $keycloakUserId" }

        return clientService.getClientByKeycloakId(keycloakUserId)
            .flatMap { currentProfile ->
                clientService.updateClient(currentProfile.id, updatedClientDto)
            }
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("isAuthenticated()")
    fun deleteMyProfile(@AuthenticationPrincipal jwt: Jwt): Mono<Void> {
        val keycloakUserId = jwt.subject
        logger.info { "Deleting client profile for Keycloak user: $keycloakUserId" }

        return clientService.getClientByKeycloakId(keycloakUserId)
            .flatMap { currentProfile ->
                clientService.deleteClient(currentProfile.id)
            }
    }
}

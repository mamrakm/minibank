package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.enumeration.UserRoleEnum
import cz.ememsoft.minibank.exception.ClientUnauthorizedException
import cz.ememsoft.minibank.repository.ClientRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * Service for handling security and authorization logic.
 */
@Service
class SecurityService(
    private val clientRepository: ClientRepository
) {

    /**
     * Gets the authenticated client ID from the security context.
     */
    fun getAuthenticatedClientId(): Mono<Long> {
        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication?.principal as? Long }
            .filter { it != null }
            .cast(Long::class.java)
            .switchIfEmpty(Mono.error(ClientUnauthorizedException("No authenticated user found")))
    }

    /**
     * Verifies that the authenticated client can access the specified client ID.
     * Admins can access any client, regular clients can only access their own data.
     */
    fun verifyClientAccess(clientId: Long): Mono<Void> {
        return getAuthenticatedClientId()
            .flatMap { authenticatedClientId ->
                if (authenticatedClientId == clientId) {
                    // Client accessing their own data - allowed
                    Mono.empty()
                } else {
                    // Check if the authenticated client is an admin
                    clientRepository.findById(authenticatedClientId)
                        .flatMap { client ->
                            if (client.role == UserRoleEnum.ADMIN) {
                                logger.debug { "Admin client $authenticatedClientId accessing client $clientId" }
                                Mono.empty()
                            } else {
                                logger.warn { "Client $authenticatedClientId attempted to access client $clientId without permission" }
                                Mono.error(ClientUnauthorizedException("Access denied: You can only access your own data"))
                            }
                        }
                }
            }
    }

    /**
     * Verifies that the authenticated client can access accounts belonging to the specified client ID.
     */
    fun verifyAccountAccess(accountOwnerId: Long): Mono<Void> {
        return verifyClientAccess(accountOwnerId)
    }

    /**
     * Checks if the authenticated client has admin role.
     */
    fun requireAdminRole(): Mono<Void> {
        return getAuthenticatedClientId()
            .flatMap { clientId ->
                clientRepository.findById(clientId)
                    .flatMap { client ->
                        if (client.role == UserRoleEnum.ADMIN) {
                            Mono.empty()
                        } else {
                            logger.warn { "Non-admin client $clientId attempted to access admin endpoint" }
                            Mono.error(ClientUnauthorizedException("Access denied: Admin role required"))
                        }
                    }
            }
    }

    /**
     * Gets the authenticated client entity.
     */
    fun getAuthenticatedClient(): Mono<cz.ememsoft.minibank.entity.ClientEntity> {
        return getAuthenticatedClientId()
            .flatMap { clientId ->
                clientRepository.findById(clientId)
                    .switchIfEmpty(Mono.error(ClientUnauthorizedException("Authenticated client not found")))
            }
    }
}
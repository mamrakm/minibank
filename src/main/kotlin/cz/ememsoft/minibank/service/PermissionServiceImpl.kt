package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.ClientRepository
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Implementation of the PermissionService.
 * Encapsulates the logic for checking resource ownership.
 */
@Service("permissionService")
class PermissionServiceImpl(
    private val clientRepository: ClientRepository,
    private val accountRepository: AccountRepository
) : PermissionService {

    override fun isClientOwner(authentication: Authentication, clientId: Long): Mono<Boolean> {
        val principal = authentication.principal as Jwt
        val keycloakUserId = principal.subject

        return clientRepository.findByKeycloakUserId(keycloakUserId)
            .map { it.id == clientId }
            .defaultIfEmpty(false)
    }

    override fun isAccountOwner(authentication: Authentication, accountId: Long): Mono<Boolean> {
        val principal = authentication.principal as Jwt
        val keycloakUserId = principal.subject

        val clientIdMono = clientRepository.findByKeycloakUserId(keycloakUserId)
            .map { it.id }

        return clientIdMono.flatMap { clientId ->
            accountRepository.findById(accountId)
                .map { account -> account.clientId == clientId }
                .defaultIfEmpty(false)
        }.defaultIfEmpty(false)
    }
}

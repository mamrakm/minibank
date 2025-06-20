package cz.ememsoft.minibank.service

import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

/**
 * Service interface for handling complex, reusable authorization checks.
 * These methods are intended to be used within Spring Security's @PreAuthorize annotations.
 */
interface PermissionService {

    /**
     * Checks if the authenticated user is the owner of the specified client profile.
     *
     * @param authentication The current user's authentication object.
     * @param clientId The ID of the client profile being accessed.
     * @return A Mono emitting true if the user is the owner, false otherwise.
     */
    fun isClientOwner(authentication: Authentication, clientId: Long): Mono<Boolean>

    /**
     * Checks if the authenticated user is the owner of the specified bank account.
     *
     * @param authentication The current user's authentication object.
     * @param accountId The ID of the account being accessed.
     * @return A Mono emitting true if the user owns the account, false otherwise.
     */
    fun isAccountOwner(authentication: Authentication, accountId: Long): Mono<Boolean>
}

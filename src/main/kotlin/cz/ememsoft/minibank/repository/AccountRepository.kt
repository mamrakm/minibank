package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.AccountEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux

/**
 * Repository interface for managing [AccountEntity] objects.
 *
 * This repository provides CRUD operations and custom query methods for handling
 * account-related data in a reactive manner.
 */
interface AccountRepository : ReactiveCrudRepository<AccountEntity, Long> {

    /**
     * Retrieves all accounts associated with the specified client ID.
     *
     * @param clientId The ID of the client whose accounts are to be retrieved.
     * @return A [Flux] emitting all [AccountEntity] objects associated with the given client ID.
     */
    fun findByClientId(clientId: Long): Flux<AccountEntity>
}

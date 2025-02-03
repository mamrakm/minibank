package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.AccountEntity
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

/**
 * Repository interface for managing [AccountEntity] objects.
 *
 * This repository provides CRUD operations and custom query methods for handling
 * account-related data in a reactive manner.
 */
@Repository
interface AccountRepository : ReactiveCrudRepository<AccountEntity, Long> {

}

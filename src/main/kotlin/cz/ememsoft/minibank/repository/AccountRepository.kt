package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.AccountEntity
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

/**
 * Repository interface for managing [AccountEntity] objects.
 *
 * This repository provides CRUD operations and custom query methods for handling
 * account-related data in a reactive manner.
 */
@Repository
interface AccountRepository : R2dbcRepository<AccountEntity, Long> {
    fun findByClientId(clientId: Long): Flux<AccountEntity>
}

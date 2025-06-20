package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.ClientEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository interface for managing [ClientEntity] persistence.
 */
@Repository
interface ClientRepository : R2dbcRepository<ClientEntity, Long> {

    /**
     * Finds a client by their Keycloak user ID.
     */
    fun findByKeycloakUserId(keycloakUserId: String): Mono<ClientEntity>

    /**
     * Finds a client by their email address.
     */
    fun findByEmail(email: String): Mono<ClientEntity>

    /**
     * Finds an active client by their email address.
     */
    @Query("SELECT * FROM bank.client WHERE email = :email AND status = 0")
    fun findActiveByEmail(email: String): Mono<ClientEntity>

    /**
     * Finds an active client by their ID.
     */
    @Query("SELECT * FROM bank.client WHERE id = :id AND status = 0")
    fun findActiveById(id: Long): Mono<ClientEntity>

    /**
     * Finds all active clients.
     */
    @Query("SELECT * FROM bank.client WHERE status = 0")
    fun findAllActive(): Flux<ClientEntity>
    
    /**
     * Finds clients by status.
     */
    @Query("SELECT * FROM bank.client WHERE status = :statusOrdinal")
    fun findByStatusOrdinal(statusOrdinal: Int): Flux<ClientEntity>
}

// Extension function for enum-based status search
fun ClientRepository.findByStatus(status: cz.ememsoft.minibank.enumeration.ClientStatusEnum): Flux<ClientEntity> =
    findByStatusOrdinal(status.ordinal)

package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.ClientEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

/**
 * Repository interface for managing [ClientEntity] persistence.
 *
 * This interface provides CRUD operations and custom query methods for the client entity.
 * It extends [JpaRepository] to leverage Spring Data JPA functionalities.
 */
@Repository
interface ClientRepository : ReactiveCrudRepository<ClientEntity, Long> {

    /**
     * Finds a client by their email address.
     *
     * @param email The email address of the client to retrieve.
     * @return The [ClientEntity] with the specified email, or `null` if no client is found.
     */
    fun findByEmail(email: String): Mono<ClientEntity> // Ensure return type is non-nullable
}

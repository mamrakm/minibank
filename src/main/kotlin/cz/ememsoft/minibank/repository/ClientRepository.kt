package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.ClientEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Repository interface for managing [ClientEntity] persistence.
 *
 * This interface provides CRUD operations and custom query methods for the client entity.
 * It extends [JpaRepository] to leverage Spring Data JPA functionalities.
 */
@Repository
interface ClientRepository : R2dbcRepository<ClientEntity, Long> {

    /**
     * Finds a client by their email address.
     *
     * @param email The email address of the client to retrieve.
     * @return The [ClientEntity] with the specified email, or `null` if no client is found.
     */
    fun findByEmail(email: String): Mono<ClientEntity> // Ensure return type is non-nullable
    fun findByFirstNameIgnoreCase(firstName: String): Flux<ClientEntity>

    @Query("INSERT INTO bank.client (first_name, last_name, email, phone, address) VALUES (:firstName, :lastName, :email, :phone, :address) RETURNING id")
    fun saveAndReturnId(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        address: String
    ): Mono<Long>
}

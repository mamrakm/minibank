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

    /**
     * Inserts a new client into the database and returns the saved entity.
     *
     * This method performs an **INSERT** operation in the `bank.client` table,
     * inserting the provided client details and returning the newly created client entity.
     * The `RETURNING` clause ensures that the full entity, including the generated ID,
     * is retrieved after the insertion.
     *
     * ## SQL Behavior:
     * - **Inserts** a new client with the provided attributes.
     * - **Returns** the newly created `ClientEntity`, including the generated `id`.
     *
     * ## Usage:
     * This method is useful when needing to retrieve the full entity immediately after insertion.
     * It is particularly beneficial in **reactive applications** where database interactions are non-blocking.
     *
     * @param firstName The first name of the client.
     * @param lastName The last name of the client.
     * @param email The email address of the client.
     * @param phone The phone number of the client.
     * @param address The address of the client.
     * @return A [Mono] emitting the saved [ClientEntity] with all populated fields, including the generated `id`.
     */
    @Query(
        "INSERT INTO bank.client " +
                "(first_name, last_name, email, phone, address) " +
                "VALUES (:firstName, :lastName, :email, :phone, :address) " +
                "RETURNING id, first_name, last_name, email, phone, address"
    )
    fun saveAndReturnId(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        address: String
    ): Mono<ClientEntity>
}

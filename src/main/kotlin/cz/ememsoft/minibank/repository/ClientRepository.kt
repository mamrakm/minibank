package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.ClientEntity
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDate

/**
 * Repository interface for managing [ClientEntity] persistence.
 */
@Repository
interface ClientRepository : R2dbcRepository<ClientEntity, Long> {

    /**
     * Finds a client by their email address.
     */
    fun findByEmail(email: String): Mono<ClientEntity>

    /**
     * Finds clients by their first name (case-insensitive).
     */
    fun findByFirstNameIgnoreCase(firstName: String): Flux<ClientEntity>

    /**
     * Inserts a new client into the database and returns the saved entity.
     * Fixed column name mismatch: phone_number instead of phone
     */
    @Query(
        "INSERT INTO bank.client " +
                "(first_name, last_name, email, phone_number, address, date_of_birth, personal_number) " +
                "VALUES (:firstName, :lastName, :email, :phoneNumber, :address, :dateOfBirth, gen_random_uuid()) " +
                "RETURNING id, first_name, last_name, email, phone_number, address, date_of_birth, personal_number"
    )
    fun saveAndReturn(
        firstName: String,
        lastName: String,
        email: String,
        phoneNumber: String?,
        address: String?,
        dateOfBirth: LocalDate?
    ): Mono<ClientEntity>
}
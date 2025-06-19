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
     * Finds an active client by their email address.
     */
    @Query("SELECT * FROM bank.client WHERE email = :email AND status = 0")
    fun findActiveByEmail(email: String): Mono<ClientEntity>

    /**
     * Finds clients by their first name (case-insensitive).
     */
    fun findByFirstNameIgnoreCase(firstName: String): Flux<ClientEntity>

    /**
     * Finds active clients by their first name (case-insensitive).
     */
    @Query("SELECT * FROM bank.client WHERE UPPER(first_name) = UPPER(:firstName) AND status = 0")
    fun findActiveByFirstNameIgnoreCase(firstName: String): Flux<ClientEntity>

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

    @Query("SELECT * FROM bank.client WHERE id = :id AND personal_number = :personalNumber AND last_name = :lastName")
    fun findByIdAndPersonalNumberAndLastName(
        id: Long,
        personalNumber: String,
        lastName: String
    ): Mono<ClientEntity>

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
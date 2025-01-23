package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.entity.AccountEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service interface for managing account-related operations.
 * Provides reactive methods for performing CRUD operations on accounts.
 */
interface AccountService {

    /**
     * Retrieves all accounts.
     *
     * @return A [Flux] emitting all [AccountEntity] objects.
     */
    fun getAllAccounts(): Flux<AccountEntity>

    /**
     * Retrieves an account by its ID.
     *
     * @param id The ID of the account to retrieve.
     * @return A [Mono] emitting the [AccountEntity] if found, or empty if not.
     */
    fun getAccountById(id: Long): Mono<AccountEntity>

    /**
     * Creates a new account.
     *
     * @param accountEntity The account details to create.
     * @return A [Mono] emitting the created [AccountEntity].
     */
    fun createAccount(accountEntity: AccountEntity): Mono<AccountEntity>

    /**
     * Updates an existing account.
     *
     * @param id The ID of the account to update.
     * @param accountEntity The updated account details.
     * @return A [Mono] emitting the updated [AccountEntity] if found, or empty if not.
     */
    fun updateAccount(id: Long, accountEntity: AccountEntity): Mono<AccountEntity>

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete.
     * @return A [Mono] indicating the completion of the operation.
     */
    fun deleteAccount(id: Long): Mono<Void>

    /**
     * Retrieves all accounts for a specific client ID.
     *
     * @param clientId The ID of the client whose accounts are to be retrieved.
     * @return A [Flux] emitting all [AccountEntity] objects associated with the client.
     */
    fun getAccountsByClientId(clientId: Long): Flux<AccountEntity>
}

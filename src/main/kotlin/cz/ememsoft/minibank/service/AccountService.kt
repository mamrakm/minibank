package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service interface for managing bank accounts.
 *
 * This interface provides reactive CRUD operations for accounts,
 * ensuring non-blocking interactions with the database.
 */
interface AccountService {

    /**
     * Retrieves all accounts in the system.
     *
     * @return A [Flux] emitting all accounts as [AccountDto].
     */
    fun getAllAccounts(): Flux<AccountDto>

    /**
     * Retrieves an account by its ID.
     *
     * @param id The ID of the account to retrieve.
     * @return A [Mono] emitting the corresponding [AccountDto], or an error if the account is not found.
     */
    fun getAccountById(id: Long): Mono<AccountDto>

    /**
     * Creates a new account.
     *
     * @param accountRequest The request object containing the account details.
     * @return A [Mono] emitting the created [AccountDto].
     */
    fun createAccount(accountRequest: CreateAccountRequestDto): Mono<AccountDto>

    /**
     * Updates an existing account by ID.
     *
     * @param id The ID of the account to update.
     * @param accountEntity The updated account entity.
     * @return A [Mono] emitting the updated [AccountDto], or an error if the account is not found.
     */
    fun updateAccount(id: Long, accountEntity: AccountEntity): Mono<AccountDto>

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete.
     * @return A [Mono] signaling completion, or an error if the account is not found.
     */
    fun deleteAccount(id: Long): Mono<Void>

    /**
     * Retrieves accounts associated with a specific client ID.
     *
     * @param clientId The ID of the client whose accounts are being retrieved.
     * @return A [Flux] emitting all corresponding [AccountDto] objects.
     */
    fun getAccountsByClientId(clientId: Long): Flux<AccountDto>
    
    // Administrative methods for managing accounts in any status
    
    /**
     * Retrieves an account by its ID regardless of status (for administrative purposes).
     *
     * @param id The ID of the account to retrieve.
     * @return A [Mono] emitting the corresponding [AccountDto], or an error if the account is not found.
     */
    fun getAccountByIdAdmin(id: Long): Mono<AccountDto>
    
    /**
     * Retrieves all accounts in the system regardless of status (for administrative purposes).
     *
     * @return A [Flux] emitting all accounts as [AccountDto].
     */
    fun getAllAccountsAdmin(): Flux<AccountDto>
    
    /**
     * Retrieves accounts by status (for administrative purposes).
     *
     * @param status The status to filter by.
     * @return A [Flux] emitting accounts with the specified status as [AccountDto].
     */
    fun getAccountsByStatus(status: AccountStatusEnum): Flux<AccountDto>
    
    /**
     * Retrieves accounts associated with a specific client ID regardless of status (for administrative purposes).
     *
     * @param clientId The ID of the client whose accounts are being retrieved.
     * @return A [Flux] emitting all corresponding [AccountDto] objects.
     */
    fun getAccountsByClientIdAdmin(clientId: Long): Flux<AccountDto>
    
    /**
     * Updates an account's status (for administrative purposes).
     *
     * @param id The ID of the account to update.
     * @param newStatus The new status to set.
     * @return A [Mono] emitting the updated [AccountDto], or an error if the account is not found.
     */
    fun updateAccountStatus(id: Long, newStatus: AccountStatusEnum): Mono<AccountDto>
}
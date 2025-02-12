package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service implementation for managing bank accounts.
 *
 * This service provides reactive CRUD operations for accounts,
 * ensuring non-blocking interactions with the database.
 *
 * @property accountRepository The repository handling account data operations.
 * @property accountMapper The mapper responsible for converting between entity and DTO representations.
 */
@Service
@Transactional
class AccountServiceImpl(private val accountRepository: AccountRepository, private val accountMapper: AccountMapper) :
    AccountService {

    private val logger = LoggerFactory.getLogger(AccountServiceImpl::class.java)

    /**
     * Retrieves all accounts in the system.
     *
     * @return A [Flux] emitting all accounts as [AccountDto].
     */
    override fun getAllAccounts(): Flux<AccountDto> {
        logger.info("Fetching all accounts")
        return accountRepository.findAll().map { accountMapper.entityToDto(it) }
            .doOnError { logger.error("Error fetching all accounts", it) }
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param id The ID of the account to retrieve.
     * @return A [Mono] emitting the corresponding [AccountDto], or an error if the account is not found.
     */
    override fun getAccountById(id: Long): Mono<AccountDto> {
        logger.info("Fetching account with ID: $id")
        return accountRepository.findById(id).map { accountMapper.entityToDto(it) }
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .doOnSuccess { logger.info("Account found by id: $id") }
            .doOnError { logger.error("Error fetching account with ID: $id", it) }
    }

    /**
     * Creates a new account.
     *
     * @param accountRequest The request object containing the account details.
     * @return A [Mono] emitting the created [AccountDto].
     */
    override fun createAccount(accountRequest: CreateAccountRequestDto): Mono<AccountDto> {
        logger.info("Creating new account")
        return accountRepository.save(accountMapper.requestToEntity(accountRequest))
            .map { accountMapper.entityToDto(it) }.doOnSuccess { logger.info("Account created: $it") }
            .doOnError { logger.error("Error creating account", it) }
    }

    /**
     * Updates an existing account by ID.
     *
     * @param id The ID of the account to update.
     * @param accountEntity The updated account entity.
     * @return A [Mono] emitting the updated [AccountDto], or an error if the account is not found.
     */

    override fun updateAccount(id: Long, accountEntity: AccountEntity): Mono<AccountDto> {
        logger.info("Updating account with ID: \$id")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID \$id not found")))
            .flatMap { existingAccount ->
                val updatedAccount = AccountEntity(
                    id = existingAccount.id,
                    name = accountEntity.name,
                    balance = accountEntity.balance,
                    clientId = existingAccount.clientId,
                    accountType = accountEntity.accountType
                )
                accountRepository.save(updatedAccount).map { accountMapper.entityToDto(it) }
            }
            .doOnSuccess { logger.info("Account updated: \$id") }
            .doOnError { logger.error("Error updating account with ID: \$id", it) }
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete.
     * @return A [Mono] signaling completion, or an error if the account is not found.
     */
    override fun deleteAccount(id: Long): Mono<Void> {
        logger.info("Deleting account with ID: $id")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .flatMap { accountRepository.delete(it) }
            .doOnError { logger.error("Error deleting account with ID: $id", it) }
    }

    /**
     * Retrieves accounts associated with a specific client ID.
     *
     * @param clientId The ID of the client whose accounts are being retrieved.
     * @return A [Mono] emitting the corresponding [AccountDto].
     */
    override fun getAccountsByClientId(clientId: Long): Mono<AccountDto> {
        logger.info("Fetching accounts for client ID: $clientId")
        return accountRepository.findById(clientId).map { accountMapper.entityToDto(it) }
            .doOnError { logger.error("Error fetching accounts for client ID: $clientId", it) }
    }
}

package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.findByStatus
import cz.ememsoft.minibank.validation.RequestValidatorFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AccountServiceImpl(
    private val accountRepository: AccountRepository,
    private val accountMapper: AccountMapper
) : AccountService {

    private val logger = LoggerFactory.getLogger(AccountServiceImpl::class.java)

    override fun getAllAccounts(): Flux<AccountDto> {
        logger.info("Fetching all active accounts")
        return accountRepository.findAllActive()
            .map { accountMapper.entityToDto(it) }
            .doOnError { logger.error("Error fetching all active accounts", it) }
    }

    override fun getAccountById(id: Long): Mono<AccountDto> {
        logger.info("Fetching active account with ID: $id")
        return accountRepository.findActiveById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Active account with ID $id not found")))
            .map { accountMapper.entityToDto(it) }
            .doOnSuccess { logger.info("Active account found by id: $id") }
            .doOnError { logger.error("Error fetching active account with ID: $id", it) }
    }

    @Transactional
    override fun createAccount(accountRequest: CreateAccountRequestDto): Mono<AccountDto> {
        logger.info("Creating new account for client ${accountRequest.clientId}")

        return Mono.fromCallable {
            // Use custom validation in addition to Bean Validation
            val validator = RequestValidatorFactory.createAccountRequestValidator()
            validator.validateAccountRequest(
                balance = accountRequest.balance,
                currency = accountRequest.currency,
                accountType = accountRequest.accountType
            )

            accountMapper.createRequestToEntity(accountRequest)
        }
            .flatMap { accountEntity -> accountRepository.save(accountEntity) }
            .map { accountMapper.entityToDto(it) }
            .doOnSuccess { logger.info("Account created successfully: ${it.id}") }
            .doOnError { logger.error("Error creating account for client ${accountRequest.clientId}", it) }
    }

    @Transactional
    override fun updateAccount(id: Long, accountEntity: AccountEntity): Mono<AccountDto> {
        logger.info("Updating account with ID: $id")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .flatMap { existingAccount ->
                val updatedAccount = AccountEntity(
                    id = existingAccount.id,
                    name = accountEntity.name,
                    balance = accountEntity.balance,
                    clientId = existingAccount.clientId,
                    accountTypeOrdinal = accountEntity.accountType.ordinal,
                    currencyOrdinal = accountEntity.currency.ordinal,
                    statusOrdinal = existingAccount.status.ordinal
                )
                accountRepository.save(updatedAccount)
            }
            .map { accountMapper.entityToDto(it) }
            .doOnSuccess { logger.info("Account updated successfully: $id") }
            .doOnError { logger.error("Error updating account with ID: $id", it) }
    }

    @Transactional
    override fun deleteAccount(id: Long): Mono<Void> {
        logger.info("Soft deleting account with ID: $id")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .flatMap { account ->
                val closedAccount = AccountEntity(
                    id = account.id,
                    name = account.name,
                    clientId = account.clientId,
                    balance = account.balance,
                    accountTypeOrdinal = account.accountType.ordinal,
                    currencyOrdinal = account.currency.ordinal,
                    statusOrdinal = AccountStatusEnum.CLOSED.ordinal
                )
                accountRepository.save(closedAccount)
            }
            .then()
            .doOnSuccess { logger.info("Account soft deleted successfully: $id") }
            .doOnError { logger.error("Error soft deleting account with ID: $id", it) }
    }

    override fun getAccountsByClientId(clientId: Long): Flux<AccountDto> {
        logger.info("Fetching active accounts for client ID: $clientId")
        return accountRepository.findActiveByClientId(clientId)
            .map { accountMapper.entityToDto(it) }
            .doOnComplete { logger.info("Successfully retrieved active accounts for client ID: $clientId") }
            .doOnError { logger.error("Error fetching active accounts for client ID: $clientId", it) }
    }

    // Administrative methods for managing accounts in any status

    override fun getAccountByIdAdmin(id: Long): Mono<AccountDto> {
        logger.info("Admin: Fetching account with ID: $id (any status)")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .map { accountMapper.entityToDto(it) }
            .doOnSuccess { logger.info("Admin: Account found by id: $id") }
            .doOnError { logger.error("Admin: Error fetching account with ID: $id", it) }
    }

    override fun getAllAccountsAdmin(): Flux<AccountDto> {
        logger.info("Admin: Fetching all accounts (any status)")
        return accountRepository.findAll()
            .map { accountMapper.entityToDto(it) }
            .doOnError { logger.error("Admin: Error fetching all accounts", it) }
    }

    override fun getAccountsByStatus(status: AccountStatusEnum): Flux<AccountDto> {
        logger.info("Admin: Fetching accounts with status: $status")
        return accountRepository.findByStatus(status)
            .map { accountMapper.entityToDto(it) }
            .doOnComplete { logger.info("Admin: Successfully retrieved accounts with status: $status") }
            .doOnError { logger.error("Admin: Error fetching accounts with status: $status", it) }
    }

    override fun getAccountsByClientIdAdmin(clientId: Long): Flux<AccountDto> {
        logger.info("Admin: Fetching all accounts for client ID: $clientId (any status)")
        return accountRepository.findAllByClientId(clientId)
            .map { accountMapper.entityToDto(it) }
            .doOnComplete { logger.info("Admin: Successfully retrieved all accounts for client ID: $clientId") }
            .doOnError { logger.error("Admin: Error fetching all accounts for client ID: $clientId", it) }
    }

    override fun updateAccountStatus(id: Long, newStatus: AccountStatusEnum): Mono<AccountDto> {
        logger.info("Admin: Updating account status for ID: $id to $newStatus")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .flatMap { existingAccount ->
                val updatedAccount = AccountEntity(
                    id = existingAccount.id,
                    name = existingAccount.name,
                    clientId = existingAccount.clientId,
                    balance = existingAccount.balance,
                    accountTypeOrdinal = existingAccount.accountType.ordinal,
                    currencyOrdinal = existingAccount.currency.ordinal,
                    statusOrdinal = newStatus.ordinal
                )
                accountRepository.save(updatedAccount)
            }
            .map { accountMapper.entityToDto(it) }
            .doOnSuccess { logger.info("Admin: Account status updated successfully for ID: $id to $newStatus") }
            .doOnError { logger.error("Admin: Error updating account status for ID: $id", it) }
    }
}
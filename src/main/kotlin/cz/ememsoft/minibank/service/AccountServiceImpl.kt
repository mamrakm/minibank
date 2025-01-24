package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.dto.request.CreateAccountRequest
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AccountServiceImpl(private val accountRepository: AccountRepository, private val accountMapper: AccountMapper) :
    AccountService {

    private val logger = LoggerFactory.getLogger(AccountServiceImpl::class.java)

    override fun getAllAccounts(): Flux<AccountDto> {
        logger.info("Fetching all accounts")
        return accountRepository.findAll()
            .map { accountMapper.entityToDto(it) }
            .doOnError { logger.error("Error fetching all accounts", it) }
    }

    override fun getAccountById(id: Long): Mono<AccountDto> {
        logger.info("Fetching account with ID: $id")
        return accountRepository.findById(id)
            .map { accountMapper.entityToDto(it) }
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .doOnSuccess { logger.info("Account found by id: ") }
            .doOnError { logger.error("Error fetching account with ID: $id", it) }
    }

    override fun createAccount(accountRequest: CreateAccountRequest): Mono<AccountDto> {
        logger.info("Creating new account")
        return accountRepository.save(accountMapper.requestToEntity(accountRequest))
            .map { accountMapper.entityToDto(it) }
            .doOnSuccess { logger.info("Account created: $it") }
            .doOnError { logger.error("Error creating account", it) }
    }

    override fun updateAccount(id: Long, accountEntity: AccountEntity): Mono<AccountDto> {
        throw NotImplementedError()
//        logger.info("Updating account with ID: $id")
//        return accountRepository.findById(id)
//            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
//            .flatMap { existingAccount ->
//                val updatedAccount = existingAccount.copy(
//                    name = accountEntity.name ?: existingAccount.name,
//                    balance = accountEntity.balance ?: existingAccount.balance
//                )
//                accountRepository.save(updatedAccount)
//            }
//            .doOnSuccess { logger.info("Account updated: $it") }
//            .doOnError { logger.error("Error updating account with ID: $id", it) }
    }

    override fun deleteAccount(id: Long): Mono<Void> {
        logger.info("Deleting account with ID: $id")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .flatMap { accountRepository.delete(it) }
            .doOnError { logger.error("Error deleting account with ID: $id", it) }
    }

    override fun getAccountsByClientId(clientId: Long): Flux<AccountDto> {
        logger.info("Fetching accounts for client ID: $clientId")
        return accountRepository.findByClientId(clientId)
            .map { accountMapper.entityToDto(it) }
            .doOnError { logger.error("Error fetching accounts for client ID: $clientId", it) }
    }
}

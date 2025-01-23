package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.repository.AccountRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class AccountServiceImpl(private val accountRepository: AccountRepository) : AccountService {

    private val logger = LoggerFactory.getLogger(AccountServiceImpl::class.java)

    override fun getAllAccounts(): Flux<AccountEntity> {
        logger.info("Fetching all accounts")
        return accountRepository.findAll()
            .doOnError { logger.error("Error fetching all accounts", it) }
    }

    override fun getAccountById(id: Long): Mono<AccountEntity> {
        logger.info("Fetching account with ID: $id")
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(AccountNotFoundException("Account with ID $id not found")))
            .doOnError { logger.error("Error fetching account with ID: $id", it) }
    }

    override fun createAccount(accountEntity: AccountEntity): Mono<AccountEntity> {
        logger.info("Creating new account")
        return accountRepository.save(accountEntity)
            .doOnSuccess { logger.info("Account created: $it") }
            .doOnError { logger.error("Error creating account", it) }
    }

    override fun updateAccount(id: Long, accountEntity: AccountEntity): Mono<AccountEntity> {
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

    override fun getAccountsByClientId(clientId: Long): Flux<AccountEntity> {
        logger.info("Fetching accounts for client ID: $clientId")
        return accountRepository.findByClientId(clientId)
            .doOnError { logger.error("Error fetching accounts for client ID: $clientId", it) }
    }
}

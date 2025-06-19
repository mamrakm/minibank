package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface AccountRepository : R2dbcRepository<AccountEntity, Long> {

    fun findByClientId(clientId: Long): Flux<AccountEntity>

    /**
     * Finds active accounts by client ID.
     */
    @Query("SELECT * FROM bank.account WHERE client_id = :clientId AND status = 0")
    fun findActiveByClientId(clientId: Long): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal")
    fun findByAccountTypeOrdinal(accountTypeOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal AND status = 0")
    fun findActiveByAccountTypeOrdinal(accountTypeOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE currency = :currencyOrdinal")
    fun findByCurrencyOrdinal(currencyOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE currency = :currencyOrdinal AND status = 0")
    fun findActiveByCurrencyOrdinal(currencyOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal AND currency = :currencyOrdinal")
    fun findByAccountTypeOrdinalAndCurrencyOrdinal(accountTypeOrdinal: Int, currencyOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal AND currency = :currencyOrdinal AND status = 0")
    fun findActiveByAccountTypeOrdinalAndCurrencyOrdinal(accountTypeOrdinal: Int, currencyOrdinal: Int): Flux<AccountEntity>

    /**
     * Finds an active account by ID.
     */
    @Query("SELECT * FROM bank.account WHERE id = :id AND status = 0")
    fun findActiveById(id: Long): Mono<AccountEntity>

    /**
     * Finds all active accounts.
     */
    @Query("SELECT * FROM bank.account WHERE status = 0")
    fun findAllActive(): Flux<AccountEntity>
    
    /**
     * Finds accounts by status.
     */
    @Query("SELECT * FROM bank.account WHERE status = :statusOrdinal")
    fun findByStatusOrdinal(statusOrdinal: Int): Flux<AccountEntity>
    
    /**
     * Finds all accounts by client ID regardless of status.
     */
    @Query("SELECT * FROM bank.account WHERE client_id = :clientId")
    fun findAllByClientId(clientId: Long): Flux<AccountEntity>
}

// Extension functions for enum-based convenience methods
fun AccountRepository.findByAccountType(accountType: AccountTypeEnum): Flux<AccountEntity> =
    findByAccountTypeOrdinal(accountType.ordinal)

fun AccountRepository.findByCurrency(currency: CurrencyEnum): Flux<AccountEntity> =
    findByCurrencyOrdinal(currency.ordinal)

fun AccountRepository.findByAccountTypeAndCurrency(accountType: AccountTypeEnum, currency: CurrencyEnum): Flux<AccountEntity> =
    findByAccountTypeOrdinalAndCurrencyOrdinal(accountType.ordinal, currency.ordinal)

// Extension functions for active-only queries
fun AccountRepository.findActiveByAccountType(accountType: AccountTypeEnum): Flux<AccountEntity> =
    findActiveByAccountTypeOrdinal(accountType.ordinal)

fun AccountRepository.findActiveByCurrency(currency: CurrencyEnum): Flux<AccountEntity> =
    findActiveByCurrencyOrdinal(currency.ordinal)

fun AccountRepository.findActiveByAccountTypeAndCurrency(accountType: AccountTypeEnum, currency: CurrencyEnum): Flux<AccountEntity> =
    findActiveByAccountTypeOrdinalAndCurrencyOrdinal(accountType.ordinal, currency.ordinal)

// Extension function for enum-based status search
fun AccountRepository.findByStatus(status: cz.ememsoft.minibank.enumeration.AccountStatusEnum): Flux<AccountEntity> =
    findByStatusOrdinal(status.ordinal)
package cz.ememsoft.minibank.repository

import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface AccountRepository : R2dbcRepository<AccountEntity, Long> {

    fun findByClientId(clientId: Long): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal")
    fun findByAccountTypeOrdinal(accountTypeOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE currency = :currencyOrdinal")
    fun findByCurrencyOrdinal(currencyOrdinal: Int): Flux<AccountEntity>

    @Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal AND currency = :currencyOrdinal")
    fun findByAccountTypeOrdinalAndCurrencyOrdinal(accountTypeOrdinal: Int, currencyOrdinal: Int): Flux<AccountEntity>
}

// Extension functions for enum-based convenience methods
fun AccountRepository.findByAccountType(accountType: AccountTypeEnum): Flux<AccountEntity> =
    findByAccountTypeOrdinal(accountType.ordinal)

fun AccountRepository.findByCurrency(currency: CurrencyEnum): Flux<AccountEntity> =
    findByCurrencyOrdinal(currency.ordinal)

fun AccountRepository.findByAccountTypeAndCurrency(accountType: AccountTypeEnum, currency: CurrencyEnum): Flux<AccountEntity> =
    findByAccountTypeOrdinalAndCurrencyOrdinal(accountType.ordinal, currency.ordinal)
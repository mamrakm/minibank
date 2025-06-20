package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.findByStatus
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

class AccountServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val accountMapper: AccountMapper = mock()
    private lateinit var accountService: AccountService

    private val accountEntity = AccountEntity(
        id = 1L,
        name = "Test Checking Account",
        clientId = 100L,
        balance = BigDecimal("1000.0000"),
        accountTypeOrdinal = AccountTypeEnum.CHECKING.ordinal,
        currencyOrdinal = CurrencyEnum.USD.ordinal,
        statusOrdinal = AccountStatusEnum.ACTIVE.ordinal
    )

    private val accountDto = AccountDto(
        id = 1L,
        name = "Test Checking Account",
        clientId = 100L,
        balance = BigDecimal("1000.0000"),
        accountType = AccountTypeEnum.CHECKING,
        currency = CurrencyEnum.USD,
        status = AccountStatusEnum.ACTIVE
    )

    private val createAccountRequest = CreateAccountRequestDto(
        accountType = "SAVINGS",
        balance = BigDecimal("500.00"),
        currency = "EUR",
        clientId = 200L
    )

    @BeforeEach
    fun setup() {
        accountService = AccountServiceImpl(accountRepository, accountMapper)
    }

    @Test
    fun `getAllAccounts should return all active accounts`() {
        // Given
        val accounts = listOf(accountEntity, accountEntity.copy(id = 2L, name = "Another Account"))
        whenever(accountRepository.findAllActive()).thenReturn(Flux.fromIterable(accounts))
        whenever(accountMapper.entityToDto(any())).thenReturn(accountDto)

        // When & Then
        StepVerifier.create(accountService.getAllAccounts())
            .expectNextCount(2)
            .verifyComplete()

        verify(accountRepository).findAllActive()
    }

    @Test
    fun `getAccountById should return account when found and active`() {
        // Given
        whenever(accountRepository.findActiveById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountMapper.entityToDto(accountEntity)).thenReturn(accountDto)

        // When & Then
        StepVerifier.create(accountService.getAccountById(1L))
            .expectNext(accountDto)
            .verifyComplete()

        verify(accountRepository).findActiveById(1L)
        verify(accountMapper).entityToDto(accountEntity)
    }

    @Test
    fun `getAccountById should fail when account not found`() {
        // Given
        whenever(accountRepository.findActiveById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(accountService.getAccountById(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findActiveById(999L)
    }

    @Test
    fun `createAccount should create new account successfully`() {
        // Given
        val newEntity = AccountEntity(
            id = 0L,
            name = "New Savings Account",
            clientId = 200L,
            balance = BigDecimal("500.0000"),
            accountTypeOrdinal = AccountTypeEnum.SAVINGS.ordinal,
            currencyOrdinal = CurrencyEnum.EUR.ordinal,
            statusOrdinal = AccountStatusEnum.ACTIVE.ordinal
        )
        val savedEntity = newEntity.copy(id = 2L)
        val resultDto = accountDto.copy(id = 2L, name = "New Savings Account", accountType = AccountTypeEnum.SAVINGS, currency = CurrencyEnum.EUR)

        whenever(accountMapper.createRequestToEntity(createAccountRequest)).thenReturn(newEntity)
        whenever(accountRepository.save(newEntity)).thenReturn(Mono.just(savedEntity))
        whenever(accountMapper.entityToDto(savedEntity)).thenReturn(resultDto)

        // When & Then
        StepVerifier.create(accountService.createAccount(createAccountRequest))
            .expectNext(resultDto)
            .verifyComplete()

        verify(accountMapper).createRequestToEntity(createAccountRequest)
        verify(accountRepository).save(newEntity)
        verify(accountMapper).entityToDto(savedEntity)
    }

    @Test
    fun `updateAccount should update existing account`() {
        // Given
        val updateEntity = accountEntity.copy(name = "Updated Account Name")
        val updatedEntity = accountEntity.copy(name = "Updated Account Name")
        val updatedDto = accountDto.copy(name = "Updated Account Name")

        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountRepository.save(any<AccountEntity>())).thenReturn(Mono.just(updatedEntity))
        whenever(accountMapper.entityToDto(updatedEntity)).thenReturn(updatedDto)

        // When & Then
        StepVerifier.create(accountService.updateAccount(1L, updateEntity))
            .expectNext(updatedDto)
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(accountRepository).save(any<AccountEntity>())
        verify(accountMapper).entityToDto(updatedEntity)
    }

    @Test
    fun `updateAccount should fail when account not found`() {
        // Given
        whenever(accountRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(accountService.updateAccount(999L, accountEntity))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findById(999L)
    }

    @Test
    fun `deleteAccount should soft delete existing account`() {
        // Given
        val closedAccount = AccountEntity(
            id = accountEntity.id,
            name = accountEntity.name,
            clientId = accountEntity.clientId,
            balance = accountEntity.balance,
            accountTypeOrdinal = accountEntity.accountType.ordinal,
            currencyOrdinal = accountEntity.currency.ordinal,
            statusOrdinal = AccountStatusEnum.CLOSED.ordinal
        )
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountRepository.save(closedAccount)).thenReturn(Mono.just(closedAccount))

        // When & Then
        StepVerifier.create(accountService.deleteAccount(1L))
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(accountRepository).save(closedAccount)
    }

    @Test
    fun `deleteAccount should fail when account not found`() {
        // Given
        whenever(accountRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(accountService.deleteAccount(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findById(999L)
    }

    @Test
    fun `getAccountsByClientId should return active accounts for client`() {
        // Given
        val clientAccounts = listOf(accountEntity, accountEntity.copy(id = 2L, name = "Client's Second Account"))
        whenever(accountRepository.findActiveByClientId(100L)).thenReturn(Flux.fromIterable(clientAccounts))
        whenever(accountMapper.entityToDto(any())).thenReturn(accountDto)

        // When & Then
        StepVerifier.create(accountService.getAccountsByClientId(100L))
            .expectNextCount(2)
            .verifyComplete()

        verify(accountRepository).findActiveByClientId(100L)
    }

    @Test
    fun `getAccountByIdAdmin should return account regardless of status`() {
        // Given
        val closedAccount = accountEntity.copy(statusOrdinal = AccountStatusEnum.CLOSED.ordinal)
        val closedDto = accountDto.copy(status = AccountStatusEnum.CLOSED)
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(closedAccount))
        whenever(accountMapper.entityToDto(closedAccount)).thenReturn(closedDto)

        // When & Then
        StepVerifier.create(accountService.getAccountByIdAdmin(1L))
            .expectNext(closedDto)
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(accountMapper).entityToDto(closedAccount)
    }

    @Test
    fun `getAllAccountsAdmin should return all accounts regardless of status`() {
        // Given
        val activeAccount = accountEntity
        val closedAccount = accountEntity.copy(id = 2L, statusOrdinal = AccountStatusEnum.CLOSED.ordinal)
        val accounts = listOf(activeAccount, closedAccount)
        whenever(accountRepository.findAll()).thenReturn(Flux.fromIterable(accounts))
        whenever(accountMapper.entityToDto(any())).thenReturn(accountDto)

        // When & Then
        StepVerifier.create(accountService.getAllAccountsAdmin())
            .expectNextCount(2)
            .verifyComplete()

        verify(accountRepository).findAll()
    }

    @Test
    fun `getAccountsByStatus should return accounts with specific status`() {
        // Given
        val closedAccounts = listOf(accountEntity.copy(statusOrdinal = AccountStatusEnum.CLOSED.ordinal))
        whenever(accountRepository.findByStatus(AccountStatusEnum.CLOSED)).thenReturn(Flux.fromIterable(closedAccounts))
        whenever(accountMapper.entityToDto(any())).thenReturn(accountDto.copy(status = AccountStatusEnum.CLOSED))

        // When & Then
        StepVerifier.create(accountService.getAccountsByStatus(AccountStatusEnum.CLOSED))
            .expectNextCount(1)
            .verifyComplete()

        verify(accountRepository).findByStatus(AccountStatusEnum.CLOSED)
    }

    @Test
    fun `getAccountsByClientIdAdmin should return all accounts for client regardless of status`() {
        // Given
        val activeAccount = accountEntity
        val closedAccount = accountEntity.copy(id = 2L, statusOrdinal = AccountStatusEnum.CLOSED.ordinal)
        val clientAccounts = listOf(activeAccount, closedAccount)
        whenever(accountRepository.findAllByClientId(100L)).thenReturn(Flux.fromIterable(clientAccounts))
        whenever(accountMapper.entityToDto(any())).thenReturn(accountDto)

        // When & Then
        StepVerifier.create(accountService.getAccountsByClientIdAdmin(100L))
            .expectNextCount(2)
            .verifyComplete()

        verify(accountRepository).findAllByClientId(100L)
    }

    @Test
    fun `updateAccountStatus should update account status`() {
        // Given
        val updatedAccount = AccountEntity(
            id = accountEntity.id,
            name = accountEntity.name,
            clientId = accountEntity.clientId,
            balance = accountEntity.balance,
            accountTypeOrdinal = accountEntity.accountType.ordinal,
            currencyOrdinal = accountEntity.currency.ordinal,
            statusOrdinal = AccountStatusEnum.SUSPENDED.ordinal
        )
        val updatedDto = accountDto.copy(status = AccountStatusEnum.SUSPENDED)
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountRepository.save(updatedAccount)).thenReturn(Mono.just(updatedAccount))
        whenever(accountMapper.entityToDto(updatedAccount)).thenReturn(updatedDto)

        // When & Then
        StepVerifier.create(accountService.updateAccountStatus(1L, AccountStatusEnum.SUSPENDED))
            .expectNext(updatedDto)
            .verifyComplete()

        verify(accountRepository).findById(1L)
        verify(accountRepository).save(updatedAccount)
        verify(accountMapper).entityToDto(updatedAccount)
    }

    @Test
    fun `updateAccountStatus should fail when account not found`() {
        // Given
        whenever(accountRepository.findById(999L)).thenReturn(Mono.empty())

        // When & Then
        StepVerifier.create(accountService.updateAccountStatus(999L, AccountStatusEnum.SUSPENDED))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountRepository).findById(999L)
    }

    @Test
    fun `getAllAccounts should handle empty result gracefully`() {
        // Given
        whenever(accountRepository.findAllActive()).thenReturn(Flux.empty())

        // When & Then
        StepVerifier.create(accountService.getAllAccounts())
            .verifyComplete()

        verify(accountRepository).findAllActive()
    }

    @Test
    fun `getAccountsByClientId should handle empty result gracefully`() {
        // Given
        whenever(accountRepository.findActiveByClientId(999L)).thenReturn(Flux.empty())

        // When & Then
        StepVerifier.create(accountService.getAccountsByClientId(999L))
            .verifyComplete()

        verify(accountRepository).findActiveByClientId(999L)
    }
}
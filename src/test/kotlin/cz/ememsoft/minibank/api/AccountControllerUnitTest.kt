package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.service.AccountService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class AccountControllerUnitTest {

    @Mock
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var accountMapper: AccountMapper

    @InjectMocks
    private lateinit var accountController: AccountController

    private lateinit var accountDto: AccountDto
    private lateinit var accountEntity: AccountEntity
    private lateinit var createAccountRequest: CreateAccountRequestDto

    @BeforeEach
    fun setUp() {
        accountDto = AccountDto(
            id = 1L,
            name = "Test Checking Account",
            clientId = 100L,
            balance = BigDecimal("1000.0000"),
            accountType = AccountTypeEnum.CHECKING,
            currency = CurrencyEnum.USD
        )

        accountEntity = AccountEntity.create(
            id = 1L,
            name = "Test Checking Account",
            clientId = 100L,
            balance = BigDecimal("1000.0000"),
            accountType = AccountTypeEnum.CHECKING,
            currency = CurrencyEnum.USD
        )

        createAccountRequest = CreateAccountRequestDto(
            accountType = "CHECKING",
            balance = BigDecimal("1000.0000"),
            currency = "USD",
            clientId = 100L
        )
    }

    @Test
    fun `getAllAccounts should return all accounts`() {
        // Arrange
        val secondAccount = accountDto.copy(id = 2L, name = "Savings Account")
        given(accountService.getAllAccounts()).willReturn(Flux.just(accountDto, secondAccount))

        // Act & Assert
        StepVerifier.create(accountController.getAllAccounts())
            .expectNext(accountDto)
            .expectNext(secondAccount)
            .verifyComplete()

        verify(accountService).getAllAccounts()
    }

    @Test
    fun `getAllAccounts should handle empty result`() {
        // Arrange
        given(accountService.getAllAccounts()).willReturn(Flux.empty())

        // Act & Assert
        StepVerifier.create(accountController.getAllAccounts())
            .verifyComplete()

        verify(accountService).getAllAccounts()
    }

    @Test
    fun `getAccountById should return account when found`() {
        // Arrange
        given(accountService.getAccountById(1L)).willReturn(Mono.just(accountDto))

        // Act & Assert
        StepVerifier.create(accountController.getAccountById(1L))
            .expectNext(accountDto)
            .verifyComplete()

        verify(accountService).getAccountById(1L)
    }

    @Test
    fun `getAccountById should propagate AccountNotFoundException`() {
        // Arrange
        val exception = AccountNotFoundException("Account with ID 999 not found")
        given(accountService.getAccountById(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(accountController.getAccountById(999L))
            .expectErrorMatches { it is AccountNotFoundException && it.message == "Account with ID 999 not found" }
            .verify()

        verify(accountService).getAccountById(999L)
    }

    @Test
    fun `createAccount should create account successfully`() {
        // Arrange
        given(accountService.createAccount(createAccountRequest)).willReturn(Mono.just(accountDto))

        // Act & Assert
        StepVerifier.create(accountController.createAccount(createAccountRequest))
            .expectNext(accountDto)
            .verifyComplete()

        verify(accountService).createAccount(createAccountRequest)
    }

    @Test
    fun `updateAccount should update account successfully`() {
        // Arrange
        val updatedDto = accountDto.copy(name = "Updated Account")
        given(accountMapper.dtoToEntity(accountDto)).willReturn(accountEntity)
        given(accountService.updateAccount(1L, accountEntity)).willReturn(Mono.just(updatedDto))

        // Act & Assert
        StepVerifier.create(accountController.updateAccount(1L, accountDto))
            .expectNext(updatedDto)
            .verifyComplete()

        verify(accountMapper).dtoToEntity(accountDto)
        verify(accountService).updateAccount(1L, accountEntity)
    }

    @Test
    fun `deleteAccount should delete account successfully`() {
        // Arrange
        given(accountService.deleteAccount(1L)).willReturn(Mono.empty())

        // Act & Assert
        StepVerifier.create(accountController.deleteAccount(1L))
            .verifyComplete()

        verify(accountService).deleteAccount(1L)
    }

    @Test
    fun `getAccountsByClientId should return client accounts`() {
        // Arrange
        val secondAccount = accountDto.copy(id = 2L, name = "Second Account")
        given(accountService.getAccountsByClientId(100L)).willReturn(Flux.just(accountDto, secondAccount))

        // Act & Assert
        StepVerifier.create(accountController.getAccountsByClientId(100L))
            .expectNext(accountDto)
            .expectNext(secondAccount)
            .verifyComplete()

        verify(accountService).getAccountsByClientId(100L)
    }

    @Test
    fun `updateAccount should propagate AccountNotFoundException`() {
        // Arrange
        val exception = AccountNotFoundException("Account with ID 999 not found")
        given(accountMapper.dtoToEntity(any())).willReturn(accountEntity)
        given(accountService.updateAccount(999L, accountEntity)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(accountController.updateAccount(999L, accountDto))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountService).updateAccount(999L, accountEntity)
    }

    @Test
    fun `deleteAccount should propagate AccountNotFoundException`() {
        // Arrange
        val exception = AccountNotFoundException("Account with ID 999 not found")
        given(accountService.deleteAccount(999L)).willReturn(Mono.error(exception))

        // Act & Assert
        StepVerifier.create(accountController.deleteAccount(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()

        verify(accountService).deleteAccount(999L)
    }
}
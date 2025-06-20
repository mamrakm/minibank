package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import cz.ememsoft.minibank.enumeration.AccountTypeEnum
import cz.ememsoft.minibank.enumeration.CurrencyEnum
import cz.ememsoft.minibank.exception.AccountNotFoundException
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.repository.AccountRepository
import cz.ememsoft.minibank.repository.findByStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

@ExtendWith(MockitoExtension::class)
class AccountServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository
    @Mock
    private lateinit var accountMapper: AccountMapper
    @InjectMocks
    private lateinit var accountService: AccountServiceImpl

    @Captor
    private lateinit var accountEntityCaptor: ArgumentCaptor<AccountEntity>

    private lateinit var accountEntity: AccountEntity
    private lateinit var accountDto: AccountDto

    @BeforeEach
    fun setup() {
        accountEntity = AccountEntity.create(1L, "Test Account", 100L, BigDecimal("1000.00"), AccountTypeEnum.CHECKING, CurrencyEnum.USD)
        accountDto = AccountDto(1L, "Test Account", 100L, BigDecimal("1000.00"), AccountTypeEnum.CHECKING, CurrencyEnum.USD, AccountStatusEnum.ACTIVE)
    }

    @Test
    fun `getAccountById should return account when found and active`() {
        whenever(accountRepository.findActiveById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountMapper.entityToDto(accountEntity)).thenReturn(accountDto)

        StepVerifier.create(accountService.getAccountById(1L))
            .expectNext(accountDto)
            .verifyComplete()
    }

    @Test
    fun `getAccountById should return error when not found`() {
        whenever(accountRepository.findActiveById(999L)).thenReturn(Mono.empty())
        StepVerifier.create(accountService.getAccountById(999L))
            .expectError(AccountNotFoundException::class.java)
            .verify()
    }

    @Test
    fun `deleteAccount should change status to CLOSED`() {
        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountRepository.save(accountEntityCaptor.capture())).thenReturn(Mono.just(accountEntity))

        StepVerifier.create(accountService.deleteAccount(1L))
            .verifyComplete()

        verify(accountRepository).save(any())
        val capturedAccount = accountEntityCaptor.value
        assertEquals(AccountStatusEnum.CLOSED, capturedAccount.status)
        assertEquals(accountEntity.id, capturedAccount.id)
    }

    @Test
    fun `updateAccountStatus should change status correctly`() {
        val suspendedAccount = accountEntity.copy(statusOrdinal = AccountStatusEnum.SUSPENDED.ordinal)
        val suspendedDto = accountDto.copy(status = AccountStatusEnum.SUSPENDED)

        whenever(accountRepository.findById(1L)).thenReturn(Mono.just(accountEntity))
        whenever(accountRepository.save(any())).thenReturn(Mono.just(suspendedAccount))
        whenever(accountMapper.entityToDto(suspendedAccount)).thenReturn(suspendedDto)

        StepVerifier.create(accountService.updateAccountStatus(1L, AccountStatusEnum.SUSPENDED))
            .expectNext(suspendedDto)
            .verifyComplete()

        verify(accountRepository).save(accountEntityCaptor.capture())
        assertEquals(AccountStatusEnum.SUSPENDED.ordinal, accountEntityCaptor.value.status.ordinal)
    }

    @Test
    fun `getAccountsByStatus should query repository with correct status`() {
        whenever(accountRepository.findByStatus(AccountStatusEnum.FROZEN)).thenReturn(Flux.just(accountEntity))
        whenever(accountMapper.entityToDto(accountEntity)).thenReturn(accountDto)

        StepVerifier.create(accountService.getAccountsByStatus(AccountStatusEnum.FROZEN))
            .expectNextCount(1)
            .verifyComplete()

        verify(accountRepository).findByStatus(AccountStatusEnum.FROZEN)
    }
}

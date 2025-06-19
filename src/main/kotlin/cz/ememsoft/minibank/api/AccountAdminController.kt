package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.enumeration.AccountStatusEnum
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.service.AccountService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

/**
 * REST controller for administrative account operations.
 * These endpoints work with accounts in any status.
 */
@RestController
@RequestMapping("/admin/accounts")
class AccountAdminController(
    private val accountService: AccountService,
    private val accountMapper: AccountMapper
) {

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllAccountsAdmin(): Flux<AccountDto> {
        logger.info { "Admin: Fetching all accounts (any status)" }
        return accountService.getAllAccountsAdmin()
            .doOnError { logger.error(it) { "Admin: Error fetching all accounts" } }
    }

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccountByIdAdmin(@PathVariable id: Long): Mono<AccountDto> {
        logger.info { "Admin: Fetching account with ID: $id (any status)" }
        return accountService.getAccountByIdAdmin(id)
            .doOnError { logger.error(it) { "Admin: Error fetching account with ID: $id" } }
    }

    @GetMapping("/by-status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccountsByStatus(@RequestParam status: AccountStatusEnum): Flux<AccountDto> {
        logger.info { "Admin: Fetching accounts with status: $status" }
        return accountService.getAccountsByStatus(status)
            .doOnComplete { logger.info { "Admin: Successfully retrieved accounts with status: $status" } }
            .doOnError { logger.error(it) { "Admin: Error fetching accounts with status: $status" } }
    }

    @GetMapping("/client/{clientId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccountsByClientIdAdmin(@PathVariable clientId: Long): Flux<AccountDto> {
        logger.info { "Admin: Fetching all accounts for client ID: $clientId (any status)" }
        return accountService.getAccountsByClientIdAdmin(clientId)
            .doOnComplete { logger.info { "Admin: Accounts for client ID: $clientId fetched successfully" } }
            .doOnError { logger.error(it) { "Admin: Error fetching accounts for client ID: $clientId" } }
    }

    @PutMapping("/{id}/status", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.OK)
    fun updateAccountStatus(@PathVariable id: Long, @RequestBody statusRequest: AccountStatusUpdateRequest): Mono<AccountDto> {
        logger.info { "Admin: Updating account status for ID: $id to ${statusRequest.status}" }
        return accountService.updateAccountStatus(id, statusRequest.status)
            .doOnSuccess { logger.info { "Admin: Account status updated successfully for ID: $id" } }
            .doOnError { logger.error(it) { "Admin: Error updating account status for ID: $id" } }
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateAccountAdmin(
        @PathVariable id: Long,
        @RequestBody accountDto: AccountDto,
    ): Mono<AccountDto> {
        logger.info { "Admin: Updating account with ID: $id (any status)" }
        return accountService.updateAccount(id, accountMapper.dtoToEntity(accountDto))
            .doOnSuccess { logger.info { "Admin: Account with ID: $id updated successfully" } }
            .doOnError { logger.error(it) { "Admin: Error updating account with ID: $id" } }
    }
}

data class AccountStatusUpdateRequest(
    val status: AccountStatusEnum
)
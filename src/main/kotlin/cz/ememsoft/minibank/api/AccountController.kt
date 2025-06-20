package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.service.AccountService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private val logger = KotlinLogging.logger {}

@RestController
@RequestMapping("/accounts")
class AccountController(
    private val accountService: AccountService,
    private val accountMapper: AccountMapper
) {

    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #id)")
    fun getAccountById(@PathVariable id: Long): Mono<AccountDto> {
        logger.info { "Fetching account with ID: $id" }
        return accountService.getAccountById(id)
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isClientOwner(authentication, #accountRequest.clientId)")
    fun createAccount(@RequestBody accountRequest: CreateAccountRequestDto): Mono<AccountDto> {
        logger.info { "Creating new account for client ${accountRequest.clientId}" }
        return accountService.createAccount(accountRequest)
    }

    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #id)")
    fun updateAccount(
        @PathVariable id: Long,
        @RequestBody accountDto: AccountDto,
    ): Mono<AccountDto> {
        logger.info { "Updating account with ID: $id" }
        return accountService.updateAccount(id, accountMapper.dtoToEntity(accountDto))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #id)")
    fun deleteAccount(@PathVariable id: Long): Mono<Void> {
        logger.info { "Deleting account with ID: $id" }
        return accountService.deleteAccount(id)
    }

    @GetMapping("/client/{clientId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @PreAuthorize("hasRole('ADMIN') or @permissionService.isClientOwner(authentication, #clientId)")
    fun getAccountsByClientId(@PathVariable clientId: Long): Flux<AccountDto> {
        logger.info { "Fetching accounts for client ID: $clientId" }
        return accountService.getAccountsByClientId(clientId)
    }
}

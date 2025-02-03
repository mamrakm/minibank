package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.dto.request.CreateAccountRequest
import cz.ememsoft.minibank.dto.AccountDto
import cz.ememsoft.minibank.mapper.AccountMapper
import cz.ememsoft.minibank.service.AccountService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
    /**
     * Retrieves all accounts.
     *
     * @return A [Flux] emitting all [AccountDto] objects.
     */
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllAccounts(): Flux<AccountDto> {
        logger.info { "Fetching all accounts" }
        return accountService.getAllAccounts()
            .doOnError { logger.error(it) { "Error fetching all accounts" } }
            .onErrorResume { Flux.empty() }
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param id The ID of the account to retrieve.
     * @return A [Mono] emitting the [AccountDto] if found, or a not found response.
     */
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccountById(@PathVariable id: Long): Mono<AccountDto> {
        logger.info { "Fetching account with ID: $id" }
        return accountService.getAccountById(id)
            .doOnError { logger.error(it) { "Error fetching account with ID: $id" } }
    }

    /**
     * Creates a new account.
     *Ò
     * @param accountEntity The account details to create.
     * @return A [Mono] emitting the created [AccountDto].
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createAccount(@RequestBody accountRequest: CreateAccountRequest): Mono<AccountDto> {
        logger.info { "Creating new account" }
        return accountService.createAccount(accountRequest)
            .doOnSuccess { logger.info { "Account created: $it" } }
            .doOnError { logger.error(it) { "Error creating account" } }
    }

    /**
     * Updates an existing account.
     *
     * @param id The ID of the account to update.
     * @param accountDto The updated account details.
     * @return A [Mono] emitting the updated [AccountDto] if found, or a not found response.
     */
    @PutMapping("/{id}", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun updateAccount(
        @PathVariable id: Long,
        @RequestBody accountDto: AccountDto,
    ): Mono<AccountDto> {
        logger.info { "Updating account with ID: $id" }
        return accountService.updateAccount(id, accountMapper.dtoToEntity(accountDto))
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete.
     * @return A [Mono] indicating completion of the operation.
     */
    @DeleteMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun deleteAccount(@PathVariable id: Long): Mono<Void> {
        logger.info { "Deleting account with ID: $id" }
        return accountService.deleteAccount(id)
            .doOnError { logger.error(it) { "Error deleting account with ID: $id" } }
    }

    /**
     * Retrieves all accounts for a specific client ID.
     *
     * @param clientId The ID of the client whose accounts are to be retrieved.
     * @return A [Flux] emitting all [AccountDto] objects associated with the client.
     */
    @GetMapping("/client/{clientId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getAccountsByClientId(@PathVariable clientId: Long): Mono<AccountDto> {
        logger.info { "Fetching accounts for client ID: $clientId" }
        return accountService.getAccountsByClientId(clientId)
            .doOnError { logger.error(it) { "Error fetching accounts for client ID: $clientId" } }
    }
}

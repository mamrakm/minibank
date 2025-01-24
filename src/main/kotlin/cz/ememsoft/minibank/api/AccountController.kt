package cz.ememsoft.minibank.api

import cz.ememsoft.minibank.api.dto.request.CreateAccountRequest
import cz.ememsoft.minibank.entity.AccountEntity
import cz.ememsoft.minibank.mapper.AccountRequestToDtoMapper
import cz.ememsoft.minibank.service.AccountService
import org.mapstruct.factory.Mappers
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/accounts")
class AccountController(private val accountService: AccountService) {

    private val logger = LoggerFactory.getLogger(AccountController::class.java)

    /**
     * Retrieves all accounts.
     *
     * @return A [Flux] emitting all [AccountEntity] objects.
     */
    @GetMapping
    fun getAllAccounts(): Flux<ResponseEntity<AccountEntity>> {
        logger.info("Fetching all accounts")
        return accountService.getAllAccounts()
            .map { ResponseEntity.ok(it) }
            .doOnError { logger.error("Error fetching all accounts", it) }
    }

    /**
     * Retrieves an account by its ID.
     *
     * @param id The ID of the account to retrieve.
     * @return A [Mono] emitting the [AccountEntity] if found, or a not found response.
     */
    @GetMapping("/{id}")
    fun getAccountById(@PathVariable id: Long): Mono<ResponseEntity<AccountEntity>> {
        logger.info("Fetching account with ID: $id")
        return accountService.getAccountById(id)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .doOnError { logger.error("Error fetching account with ID: $id", it) }
    }

    /**
     * Creates a new account.
     *
     * @param accountEntity The account details to create.
     * @return A [Mono] emitting the created [AccountEntity].
     */
    @PostMapping
    fun createAccount(@RequestBody accountRequest: CreateAccountRequest): Mono<ResponseEntity<AccountEntity>> {
        logger.info("Creating new account")
        val mapper = Mappers.getMapper(AccountRequestToDtoMapper::class.java)
        return accountService.createAccount(mapper.toDto(accountRequest))
            .map { ResponseEntity.ok(it) }
            .doOnSuccess { logger.info("Account created: $it") }
            .doOnError { logger.error("Error creating account", it) }
    }

    /**
     * Updates an existing account.
     *
     * @param id The ID of the account to update.
     * @param accountEntity The updated account details.
     * @return A [Mono] emitting the updated [AccountEntity] if found, or a not found response.
     */
    @PutMapping("/{id}")
    fun updateAccount(
        @PathVariable id: Long,
        @RequestBody accountEntity: AccountEntity,
    ): Mono<ResponseEntity<AccountEntity>> {
        logger.info("Updating account with ID: $id")
        return accountService.updateAccount(id, accountEntity)
            .map { ResponseEntity.ok(it) }
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .doOnError { logger.error("Error updating account with ID: $id", it) }
    }

    /**
     * Deletes an account by its ID.
     *
     * @param id The ID of the account to delete.
     * @return A [Mono] indicating completion of the operation.
     */
    @DeleteMapping("/{id}")
    fun deleteAccount(@PathVariable id: Long): Mono<ResponseEntity<Void>> {
        logger.info("Deleting account with ID: $id")
        return accountService.deleteAccount(id)
            .then(Mono.fromCallable { ResponseEntity.noContent().build<Void>() })
            .defaultIfEmpty(ResponseEntity.notFound().build())
            .doOnError { logger.error("Error deleting account with ID: $id", it) }
    }

    /**
     * Retrieves all accounts for a specific client ID.
     *
     * @param clientId The ID of the client whose accounts are to be retrieved.
     * @return A [Flux] emitting all [AccountEntity] objects associated with the client.
     */
    @GetMapping("/client/{clientId}")
    fun getAccountsByClientId(@PathVariable clientId: Long): Flux<ResponseEntity<AccountEntity>> {
        logger.info("Fetching accounts for client ID: $clientId")
        return accountService.getAccountsByClientId(clientId)
            .map { ResponseEntity.ok(it) }
            .doOnError { logger.error("Error fetching accounts for client ID: $clientId", it) }
    }
}

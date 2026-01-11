# Minibank AI Coding Agent Instructions

## Architecture Overview

**Reactive Banking System** built with Spring Boot 3.5.0 WebFlux, Kotlin 2.1.21, R2DBC for PostgreSQL 17.

**Three-tier architecture:**
- **Controllers** ([api/](src/main/kotlin/cz/ememsoft/minibank/api/)) - REST endpoints with Keycloak OAuth2 JWT authentication
- **Services** ([service/](src/main/kotlin/cz/ememsoft/minibank/service/)) - Business logic with `@Transactional` boundaries
- **Repositories** ([repository/](src/main/kotlin/cz/ememsoft/minibank/repository/)) - R2DBC reactive data access with custom enum extension functions

**Key separation:** `TransactionProcessor` handles atomic money operations, `TransactionService` orchestrates the flow. Controllers route to services, services coordinate processors/repositories.

## Critical Developer Workflows

**Build & Run:**
```bash
docker compose up -d postgres           # Start DB first
./mvnw liquibase:update                 # Apply schema migrations
./mvnw spring-boot:run                  # Run dev mode
./test_minibank_api_with_auth.sh        # Test with Keycloak auth
```

**Testing:**
- All integration tests extend [TestBase.kt](src/test/kotlin/cz/ememsoft/minibank/TestBase.kt) which starts PostgreSQL + Keycloak Testcontainers
- Run: `./mvnw test` (unit) or `./mvnw test -Dtest=*IntegrationTest`
- Containers use `.withReuse(true)` for speed

**Schema Management:**
- Liquibase master changelog: [db.changelog-master.yaml](src/main/resources/db/changelog/db.changelog-master.yaml)
- All tables in `bank` schema
- Liquibase uses JDBC, app uses R2DBC (configured separately in application.yml)

## Money Safety - Critical Pattern

**ALWAYS use `MoneyUtils` for financial operations:**

```kotlin
// In services/processors
val normalized = MoneyUtils.normalize(amount)      // Set scale=4, HALF_EVEN rounding
val validated = MoneyUtils.validateAmount(amount)  // Check MIN/MAX bounds
val result = MoneyUtils.add(a, b)                  // Safe arithmetic
```

- All `BigDecimal` amounts: scale=4, `DECIMAL(19,4)` in DB
- Banker's rounding (`HALF_EVEN`) prevents bias
- Range: `0.0001` to `999,999,999,999,999.9999`
- Account entities have helper methods: `debitBalance(amount)`, `creditBalance(amount)`, `withBalance(amount)`

## Enum Storage Pattern

**Enums stored as integer ordinals** for performance. Never persist enum names as strings.

**Entity Pattern:**
```kotlin
@Table("account")
data class AccountEntity(
    @Column("account_type") private val accountTypeOrdinal: Int,  // DB column
    // Transient computed property
    @get:Transient
    val accountType: AccountTypeEnum get() = AccountTypeEnum.entries[accountTypeOrdinal]
)
```

**Repository Pattern with Extension Functions:**
```kotlin
// In AccountRepository.kt - ordinal-based query
@Query("SELECT * FROM bank.account WHERE account_type = :accountTypeOrdinal")
fun findByAccountTypeOrdinal(accountTypeOrdinal: Int): Flux<AccountEntity>

// Extension function for type-safe enum access
fun AccountRepository.findByAccountType(accountType: AccountTypeEnum): Flux<AccountEntity> =
    findByAccountTypeOrdinal(accountType.ordinal)
```

Always provide both ordinal methods + enum extension functions for new queries.

## Soft Delete Pattern

**Never hard delete** - use status enums instead:

```kotlin
// Service layer
override fun deleteAccount(id: Long): Mono<Void> {
    return accountRepository.findById(id)
        .flatMap { account ->
            val closedAccount = account.copy(statusOrdinal = AccountStatusEnum.CLOSED.ordinal)
            accountRepository.save(closedAccount)
        }
        .then()
}
```

- Use `findAllActive()`, `findActiveById()` for queries (status = 0)
- Status enum ordinals: `ACTIVE=0`, `CLOSED/DELETED=1`

## Security & Authorization

**OAuth2 JWT with Keycloak** (realm: minibank, port 8090 via docker compose).

**Controller Authorization Pattern:**
```kotlin
@PreAuthorize("hasRole('ADMIN') or @permissionService.isAccountOwner(authentication, #id)")
fun getAccount(@PathVariable id: Long): Mono<AccountDto>
```

- Public endpoints: `/api/auth/**`, `/actuator/health`, `/swagger-ui/**`
- Role extraction: `SecurityConfig.realmRoleAuthoritiesConverter()` extracts roles from JWT `realm_access.roles`
- [PermissionService](src/main/kotlin/cz/ememsoft/minibank/service/PermissionService.kt) validates ownership (client owns account, etc.)

## Validation Strategy Pattern

**Two-layer validation:**
1. Bean Validation (`@Valid`, `@NotNull`, etc.) on DTOs
2. Custom validators via Strategy Pattern ([validation/](src/main/kotlin/cz/ememsoft/minibank/validation/))

```kotlin
// In services before save
val validator = RequestValidatorFactory.createAccountRequestValidator()
validator.validateAccountRequest(balance, currency, accountType)
```

Strategies: `MoneyValidationStrategy`, `CurrencyValidationStrategy`, `AccountTypeValidationStrategy` composed via `CompositeValidator`.

## MapStruct Mapping Conventions

**Explicit field mappings** even for same-named fields (project convention):

```kotlin
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
interface AccountMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "balance", target = "balance")
    @Mapping(target = "accountTypeOrdinal", expression = "java(dto.getAccountType().ordinal())")
    @Mapping(target = "withBalance", ignore = true)  // Ignore helper methods
    fun dtoToEntity(dto: AccountDto): AccountEntity
}
```

Always ignore transient helper methods (`withBalance`, `debitBalance`, `creditBalance`).

## Transaction Flow Pattern

**Three-phase money transfer:**

1. **Validate** - Check balances, currency match, account status
2. **Create Pending** - Insert transaction with `PENDING` status
3. **Execute & Complete** - Update balances atomically, mark `COMPLETED` or `FAILED`

```kotlin
@Transactional
override fun processTransaction(request: TransactionRequestDto): Mono<TransactionDto> {
    return sourceAccount.zipWith(targetAccount)
        .flatMap { (source, target) -> processor.validateTransaction(request, source, target) }
        .flatMap { processor.createPendingTransaction(...) }
        .flatMap { pending -> processor.updateAccountBalances(source, target, amount)
            .then(Mono.just(pending)) }
        .flatMap { processor.completeTransaction(it) }
}
```

Services marked `@Transactional`, never repositories (R2DBC handles via reactive operators).

## Database Conventions

- **Schema:** All tables in `bank` schema
- **Primary Keys:** `bigint` auto-increment
- **Timestamps:** `timestamp` (LocalDateTime in Kotlin)
- **Indexing:** Foreign keys always indexed
- **Cascades:** Client deletion cascades to accounts (FK constraint)

## Integration Points

**External Dependencies:**
- **Keycloak:** Auth server at `http://localhost:8090`, imported realm from [config/keycloak/minibank-realm.json](config/keycloak/minibank-realm.json)
- **PostgreSQL 17:** Two databases (minibank-database, keycloak-database) initialized by [init-multiple-dbs.sh](db/init-multiple-dbs.sh)
- **Liquibase:** JDBC-based schema migrations (separate from R2DBC runtime connection)

## Common Gotchas

1. **Reactor Types:** Return `Mono<T>` (0-1 item) or `Flux<T>` (0-N items), never block
2. **MapStruct:** Must include `imports = [EnumClass::class]` for expression-based mappings
3. **Enum Queries:** Always create ordinal-based repository method + extension function
4. **Balance Operations:** Use entity helper methods, never direct `balance + amount`
5. **Error Handling:** Use `switchIfEmpty(Mono.error(...))` for 404s, custom exceptions in [exception/](src/main/kotlin/cz/ememsoft/minibank/exception/)

package cz.ememsoft.minibank.service

import com.fasterxml.jackson.annotation.JsonProperty
import cz.ememsoft.minibank.config.KeycloakProperties
import cz.ememsoft.minibank.dto.auth.CreateUserRequestDto
import cz.ememsoft.minibank.dto.auth.CreateUserResponseDto
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class KeycloakAdminService(
    private val keycloakProperties: KeycloakProperties,
    private val webClientBuilder: WebClient.Builder,
) {

    fun createUser(request: CreateUserRequestDto, defaultRoles: Set<String> = emptySet()): Mono<CreateUserResponseDto> {
        val rolesToAssign = request.roles?.takeIf { it.isNotEmpty() } ?: defaultRoles
        return adminToken()
            .flatMap { token ->
                createUserWithToken(token, request)
                    .flatMap { userId ->
                        if (rolesToAssign.isEmpty()) {
                            Mono.just(userId)
                        } else {
                            assignRealmRoles(token, userId, rolesToAssign).thenReturn(userId)
                        }
                    }
            }
            .map { CreateUserResponseDto(it) }
    }

    private fun createUserWithToken(token: String, request: CreateUserRequestDto): Mono<String> {
        val payload = KeycloakUserRepresentation(
            username = request.username,
            enabled = true,
            firstName = request.firstName,
            lastName = request.lastName,
            email = request.email,
            emailVerified = true,
            credentials = listOf(KeycloakCredentialRepresentation(request.password)),
        )

        return webClientBuilder.build()
            .post()
            .uri("${keycloakProperties.baseUrl}/admin/realms/${keycloakProperties.realm}/users")
            .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(payload)
            .exchangeToMono { response -> handleUserCreateResponse(response) }
    }

    private fun handleUserCreateResponse(response: ClientResponse): Mono<String> {
        return if (response.statusCode().is2xxSuccessful) {
            val location = response.headers().asHttpHeaders().location
            val userId = location?.path?.substringAfterLast('/')
            if (userId.isNullOrBlank()) {
                response.bodyToMono(String::class.java)
                    .defaultIfEmpty("No body")
                    .flatMap { Mono.error(IllegalStateException("Missing user id in Keycloak response: $it")) }
            } else {
                Mono.just(userId)
            }
        } else {
            response.bodyToMono(String::class.java)
                .defaultIfEmpty("No body")
                .flatMap { Mono.error(IllegalStateException("Failed to create Keycloak user: $it")) }
        }
    }

    private fun assignRealmRoles(token: String, userId: String, roles: Set<String>): Mono<Void> {
        return Flux.fromIterable(roles)
            .flatMap { roleName ->
                webClientBuilder.build()
                    .get()
                    .uri("${keycloakProperties.baseUrl}/admin/realms/${keycloakProperties.realm}/roles/$roleName")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .retrieve()
                    .bodyToMono(KeycloakRoleRepresentation::class.java)
            }
            .collectList()
            .flatMap { roleRepresentations ->
                webClientBuilder.build()
                    .post()
                    .uri("${keycloakProperties.baseUrl}/admin/realms/${keycloakProperties.realm}/users/$userId/role-mappings/realm")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(roleRepresentations)
                    .retrieve()
                    .bodyToMono(Void::class.java)
            }
    }

    private fun adminToken(): Mono<String> {
        return webClientBuilder.build()
            .post()
            .uri("${keycloakProperties.baseUrl}/realms/${keycloakProperties.adminRealm}/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "password")
                    .with("client_id", "admin-cli")
                    .with("username", ADMIN_USERNAME)
                    .with("password", ADMIN_PASSWORD),
            )
            .retrieve()
            .bodyToMono(KeycloakTokenResponse::class.java)
            .map { it.accessToken }
    }

    private data class KeycloakTokenResponse(
        @JsonProperty("access_token")
        val accessToken: String,
    )

    private data class KeycloakUserRepresentation(
        val username: String,
        val enabled: Boolean,
        val firstName: String,
        val lastName: String,
        val email: String,
        val emailVerified: Boolean,
        val credentials: List<KeycloakCredentialRepresentation>,
    )

    private data class KeycloakCredentialRepresentation(
        val value: String,
        val type: String = "password",
        val temporary: Boolean = false,
    )

    private data class KeycloakRoleRepresentation(
        val id: String,
        val name: String,
    )

    companion object {
        const val ADMIN_USERNAME = "admin"
        const val ADMIN_PASSWORD = "admin"
    }
}

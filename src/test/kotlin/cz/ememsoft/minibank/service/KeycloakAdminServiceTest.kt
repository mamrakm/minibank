package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.config.KeycloakProperties
import cz.ememsoft.minibank.dto.auth.CreateUserRequestDto
import org.junit.jupiter.api.Test
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFunction
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.concurrent.CopyOnWriteArrayList

class KeycloakAdminServiceTest {

    private val keycloakProperties = KeycloakProperties(
        baseUrl = "http://localhost:8080",
        realm = "minibank",
        clientId = "minibank-api",
        clientSecret = "minibank-secret",
    )

    @Test
    fun `createUser should return id and assign roles`() {
        val requests = CopyOnWriteArrayList<ClientRequest>()
        val exchangeFunction = ExchangeFunction { request ->
            requests.add(request)
            val path = request.url().path
            val method = request.method()
            when {
                path.endsWith("/protocol/openid-connect/token") -> {
                    Mono.just(jsonResponse(HttpStatus.OK, "{\"access_token\":\"admin-token\"}"))
                }
                path.endsWith("/admin/realms/minibank/users") && method.name() == "POST" -> {
                    Mono.just(
                        ClientResponse.create(HttpStatus.CREATED)
                            .header(HttpHeaders.LOCATION, "http://localhost:8080/admin/realms/minibank/users/user-123")
                            .build()
                    )
                }
                path.endsWith("/admin/realms/minibank/roles/CUSTOMER") -> {
                    Mono.just(jsonResponse(HttpStatus.OK, "{\"id\":\"role-1\",\"name\":\"CUSTOMER\"}"))
                }
                path.endsWith("/admin/realms/minibank/users/user-123/role-mappings/realm") -> {
                    Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build())
                }
                else -> Mono.just(jsonResponse(HttpStatus.INTERNAL_SERVER_ERROR, "{}"))
            }
        }

        val service = KeycloakAdminService(keycloakProperties, WebClient.builder().exchangeFunction(exchangeFunction))
        val request = CreateUserRequestDto(
            username = "new-user",
            password = "secret",
            firstName = "New",
            lastName = "User",
            email = "new.user@example.com",
            roles = setOf("CUSTOMER"),
        )

        StepVerifier.create(service.createUser(request))
            .assertNext { response ->
                kotlin.test.assertEquals("user-123", response.userId)
            }
            .verifyComplete()

        val calledPaths = requests.map { it.url().path }
        kotlin.test.assertTrue(calledPaths.any { it.endsWith("/protocol/openid-connect/token") })
        kotlin.test.assertTrue(calledPaths.any { it.endsWith("/admin/realms/minibank/users") })
        kotlin.test.assertTrue(calledPaths.any { it.endsWith("/admin/realms/minibank/roles/CUSTOMER") })
        kotlin.test.assertTrue(calledPaths.any { it.endsWith("/admin/realms/minibank/users/user-123/role-mappings/realm") })
    }

    private fun jsonResponse(status: HttpStatus, body: String): ClientResponse {
        val buffer = DefaultDataBufferFactory().wrap(body.toByteArray())
        return ClientResponse.create(status)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Flux.just(buffer))
            .build()
    }
}

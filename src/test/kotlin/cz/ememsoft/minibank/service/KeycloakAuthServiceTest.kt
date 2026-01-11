package cz.ememsoft.minibank.service

import cz.ememsoft.minibank.config.KeycloakProperties
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

class KeycloakAuthServiceTest {

    private val keycloakProperties = KeycloakProperties(
        baseUrl = "http://localhost:8080",
        realm = "minibank",
        clientId = "minibank-api",
        clientSecret = "minibank-secret",
    )

    @Test
    fun `login should map token response`() {
        val exchangeFunction = tokenExchangeFunction(
            """
            {
              "access_token": "access-123",
              "refresh_token": "refresh-456",
              "expires_in": 300,
              "refresh_expires_in": 600,
              "token_type": "Bearer",
              "scope": "openid profile"
            }
            """.trimIndent()
        )
        val service = KeycloakAuthService(keycloakProperties, WebClient.builder().exchangeFunction(exchangeFunction))

        StepVerifier.create(service.login("user", "password"))
            .assertNext { response ->
                kotlin.test.assertEquals("access-123", response.accessToken)
                kotlin.test.assertEquals("refresh-456", response.refreshToken)
                kotlin.test.assertEquals(300, response.expiresIn)
                kotlin.test.assertEquals(600, response.refreshExpiresIn)
                kotlin.test.assertEquals("Bearer", response.tokenType)
                kotlin.test.assertEquals("openid profile", response.scope)
            }
            .verifyComplete()
    }

    @Test
    fun `refresh should map token response`() {
        val exchangeFunction = tokenExchangeFunction(
            """
            {
              "access_token": "access-789",
              "refresh_token": "refresh-999",
              "expires_in": 120,
              "refresh_expires_in": 480,
              "token_type": "Bearer",
              "scope": "openid"
            }
            """.trimIndent()
        )
        val service = KeycloakAuthService(keycloakProperties, WebClient.builder().exchangeFunction(exchangeFunction))

        StepVerifier.create(service.refresh("refresh-999"))
            .assertNext { response ->
                kotlin.test.assertEquals("access-789", response.accessToken)
                kotlin.test.assertEquals("refresh-999", response.refreshToken)
                kotlin.test.assertEquals(120, response.expiresIn)
                kotlin.test.assertEquals(480, response.refreshExpiresIn)
                kotlin.test.assertEquals("Bearer", response.tokenType)
                kotlin.test.assertEquals("openid", response.scope)
            }
            .verifyComplete()
    }

    private fun tokenExchangeFunction(jsonBody: String): ExchangeFunction {
        return ExchangeFunction { request: ClientRequest ->
            val response = jsonResponse(HttpStatus.OK, jsonBody)
            Mono.just(response)
        }
    }

    private fun jsonResponse(status: HttpStatus, body: String): ClientResponse {
        val buffer = DefaultDataBufferFactory().wrap(body.toByteArray())
        return ClientResponse.create(status)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .body(Flux.just(buffer))
            .build()
    }
}

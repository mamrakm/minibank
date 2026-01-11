package cz.ememsoft.minibank.integration

import cz.ememsoft.minibank.TestBase
import cz.ememsoft.minibank.dto.auth.CreateUserRequestDto
import cz.ememsoft.minibank.dto.auth.CreateUserResponseDto
import cz.ememsoft.minibank.dto.auth.LoginRequestDto
import cz.ememsoft.minibank.dto.auth.RefreshTokenRequestDto
import cz.ememsoft.minibank.dto.auth.TokenResponseDto
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@AutoConfigureWebTestClient
class AuthControllerIntegrationTest : TestBase() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `login and refresh should return tokens`() {
        val loginResponse = webTestClient.post()
            .uri("/api/auth/login")
            .bodyValue(LoginRequestDto("admin", "admin"))
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenResponseDto::class.java)
            .returnResult()
            .responseBody!!

        val refreshToken = loginResponse.refreshToken
        require(!refreshToken.isNullOrBlank()) { "Expected refresh token" }

        webTestClient.post()
            .uri("/api/auth/refresh")
            .bodyValue(RefreshTokenRequestDto(refreshToken))
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenResponseDto::class.java)
            .consumeWith { result ->
                val refreshed = result.responseBody!!
                kotlin.test.assertTrue(refreshed.accessToken.isNotBlank())
            }
    }

    @Test
    fun `register should create user and allow login`() {
        val uniqueId = UUID.randomUUID().toString().substring(0, 8)
        val username = "user-$uniqueId"
        val password = "Pass-$uniqueId!"

        val createResponse = webTestClient.post()
            .uri("/api/auth/register")
            .bodyValue(
                CreateUserRequestDto(
                    username = username,
                    password = password,
                    firstName = "Test",
                    lastName = "User",
                    email = "$username@minibank.test",
                )
            )
            .exchange()
            .expectStatus().isOk
            .expectBody(CreateUserResponseDto::class.java)
            .returnResult()
            .responseBody!!

        kotlin.test.assertTrue(createResponse.userId.isNotBlank())

        webTestClient.post()
            .uri("/api/auth/login")
            .bodyValue(LoginRequestDto(username, password))
            .exchange()
            .expectStatus().isOk
            .expectBody(TokenResponseDto::class.java)
            .consumeWith { result ->
                val tokenResponse = result.responseBody!!
                kotlin.test.assertTrue(tokenResponse.accessToken.isNotBlank())
            }
    }
}

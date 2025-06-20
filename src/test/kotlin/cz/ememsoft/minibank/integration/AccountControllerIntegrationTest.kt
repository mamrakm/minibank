// src/test/kotlin/cz/ememsoft/minibank/integration/AccountControllerIntegrationTest.kt
package cz.ememsoft.minibank.integration

import cz.ememsoft.minibank.TestBase
import cz.ememsoft.minibank.api.account.request.CreateAccountRequestDto
import cz.ememsoft.minibank.dto.client.CreateClientProfileRequestDto
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.math.BigDecimal

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class AccountControllerIntegrationTest : TestBase() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private lateinit var adminToken: String
    private lateinit var userToken: String
    private var userClientId: Long = 0
    private var userAccountId: Long = 0

    @BeforeAll
    fun setup() {
        adminToken = getAccessToken("admin", "admin")
        userToken = getAccessToken("user", "user")

        val request = CreateClientProfileRequestDto(
            keycloakUserId = "c9b2a7a4-3e9f-4b9a-b24c-22a36d23f310",
            firstName = "Test",
            lastName = "User",
            email = "user@minibank.com",
            phoneNumber = null,
            address = null,
            dateOfBirth = null
        )
        val clientBody = webTestClient.post().uri("/admin/clients")
            .header("Authorization", "Bearer $adminToken").bodyValue(request).exchange()
            .expectStatus().isCreated.expectBody(Map::class.java).returnResult().responseBody!!
        userClientId = (clientBody["id"] as Number).toLong()
    }

    private fun getAccessToken(user: String, pass: String): String {
        val response = WebClient.create().post()
            .uri("${keycloakContainer.authServerUrl}realms/minibank/protocol/openid-connect/token")
            .body(BodyInserters.fromFormData("grant_type", "password").with("client_id", "minibank-api").with("client_secret", "minibank-secret").with("username", user).with("password", pass))
            .retrieve().bodyToMono(Map::class.java).block()!!

        return (response as Map<String, Any>).getValue("access_token").toString()
    }

    @Test
    @Order(1)
    fun `user can create an account for themselves`() {
        val accountRequest = CreateAccountRequestDto("SAVINGS", BigDecimal("500.00"), "USD", userClientId)
        val accountBody = webTestClient.post().uri("/accounts")
            .header("Authorization", "Bearer $userToken").bodyValue(accountRequest).exchange()
            .expectStatus().isCreated.expectBody(Map::class.java).returnResult().responseBody!!
        userAccountId = (accountBody["id"] as Number).toLong()
    }

    @Test
    @Order(2)
    fun `user can get their own account by id`() {
        webTestClient.get().uri("/accounts/$userAccountId")
            .header("Authorization", "Bearer $userToken").exchange()
            .expectStatus().isOk
            .expectBody().jsonPath("$.id").isEqualTo(userAccountId)
    }
}

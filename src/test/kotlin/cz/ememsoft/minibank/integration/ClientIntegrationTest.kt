package cz.ememsoft.minibank.integration

import cz.ememsoft.minibank.TestBase
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

class ClientIntegrationTest : TestBase() {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `should allow access to public endpoints`() {
        webTestClient.get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus().isOk
    }
}
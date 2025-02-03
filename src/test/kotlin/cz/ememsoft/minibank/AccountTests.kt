package cz.ememsoft.minibank

import cz.ememsoft.minibank.repository.ClientRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountTests(
    @Autowired
    private val clientRepository: ClientRepository,
    @Autowired
    private val restTemplateBuilder: RestTemplateBuilder,
) {

    @Test
    fun create_new_account_success() {

    }
}


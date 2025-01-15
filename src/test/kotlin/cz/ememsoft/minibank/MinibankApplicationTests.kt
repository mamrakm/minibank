package cz.ememsoft.minibank

import cz.ememsoft.minibank.repository.CustomerRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.client.RestTemplate
import org.testcontainers.junit.jupiter.Testcontainers

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class MinibankApplicationTests(
    @Autowired private val customerRepository: CustomerRepository,
    @Autowired private var restTemplateBuilder: RestTemplateBuilder,
) {
    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `save a customer to DB`() {
        val customerJson = """
            {
                "firstName": "Johnny",
                "lastName": "Silverhand",
                "email": "john.doe@example.com",
                "phone": "+123456789",
                "address": "123 Main Street"
            }
        """.trimIndent()

        // Create a RestTemplate instance from RestTemplateBuilder
        val restTemplate: RestTemplate by lazy {
            restTemplateBuilder.build()
        }

        // Set up headers
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        // Create HTTP entity with headers and body
        val request = HttpEntity(customerJson, headers)

        restTemplate.exchange(
            "http://localhost:$port/customers/save", HttpMethod.POST, request, String::class.java
        )

        val results = customerRepository.findAll().forEach { println(it) }
        sequenceOf(results)
    }
}

package cz.ememsoft.minibank

import cz.ememsoft.minibank.repository.ClientRepository
import org.assertj.core.api.Assertions.assertThat
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
    @Autowired private val clientRepository: ClientRepository,
    @Autowired private val restTemplateBuilder: RestTemplateBuilder,
) {
    @LocalServerPort
    private var port: Int = 0

//    @BeforeEach
//    fun setup() {
//        // Create a RestTemplate instance from RestTemplateBuilder
//        val restTemplate: RestTemplate by lazy {
//            restTemplateBuilder.build()
//        }
//        sendInitialClientData()
//    }
//
//    private fun sendInitialClientData() {
//        val customerJson = """
//            {
//                "firstName": "Johnny",
//                "lastName": "Silverhand",
//                "email": "johnny.silverhand@nightcity.com",
//                "phone": "+123456789",
//                "address": "Mega Building 10, Litlle China, Watson, Night City"
//            }
//        """.trimIndent()
//
//        val headers = HttpHeaders().apply {
//            contentType = MediaType.APPLICATION_JSON
//        }
//
//        val request = HttpEntity(customerJson, headers)
//
//        val response = restTemplate.exchange(
//            "http://localhost:$port/customers/save", // Ensure the correct endpoint
//            HttpMethod.POST,
//            request,
//            String::class.java
//        )
//
//        assertThat(response).isEqualTo(201) // Verify the data is saved
//    }

    @Test
    fun `save a client to DB`() {
        val customerJson = """
            {
                "firstName": "Johnny",
                "lastName": "Silverhand",
                "email": "johnny.silverhand@nightcity.com",
                "phone": "+123456789",
                "address": "Mega Building 10, Litlle China, Watson, Night City"
            }
        """.trimIndent()

        val restTemplate: RestTemplate by lazy {
            restTemplateBuilder.build()
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON

        val request = HttpEntity(customerJson, headers)

        restTemplate.exchange(
            "http://localhost:$port/customers/save", HttpMethod.POST, request, String::class.java
        )

        val result = clientRepository.findAll()
        result.forEach { println(it) }

        assertThat(result).hasSize(1)
        assertThat(result[0]?.firstName).isEqualTo("Johnny")
        assertThat(result[0]?.lastName).isEqualTo("Silverhand")
        assertThat(result[0]?.email).isEqualTo("johnny.silverhand@nightcity.com")
        assertThat(result[0]?.phone).isEqualTo("+123456789")
        assertThat(result[0]?.address).isEqualTo("Mega Building 10, Litlle China, Watson, Night City")
    }
}

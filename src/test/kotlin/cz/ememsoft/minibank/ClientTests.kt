package cz.ememsoft.minibank

import cz.ememsoft.minibank.repository.ClientRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.testcontainers.junit.jupiter.Testcontainers

@Import(TestcontainersConfiguration::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ClientTests(
    @Autowired private val clientRepository: ClientRepository,
    @Autowired private val restTemplateBuilder: RestTemplateBuilder,
) {


    /*    @LocalServerPort
        private var port: Int = 0

        private var clientId: Long = 0L
        private val restTemplate: RestTemplate by lazy {
            restTemplateBuilder.build()
        }

        private lateinit var initialClient: ClientDto

        @BeforeEach
        fun setUp() {
            clientRepository.deleteAll()

            val clientJson = """
                {
                    "firstName": "Johnny",
                    "lastName": "Silverhand",
                    "email": "johnny.silverhand@nightcity.com",
                    "phone": "+123456789",
                    "address": "Mega Building 10, Little China, Watson, Night City"
                }
            """.trimIndent()

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val request = HttpEntity(clientJson, headers)

            restTemplate.exchange(
                "http://localhost:$port/clients", HttpMethod.POST, request, String::class.java
            )

            // Fetch the created client to use in subsequent tests
            initialClient = clientRepository.findAll().first().let {
                this.clientId = it.id
                ClientDto(
                    firstName = it.firstName,
                    lastName = it.lastName,
                    email = it.email,
                    phone = it.phone,
                    address = it.address
                )
            }
        }

        @Test
        fun `fetch a client by ID`() {
            val result = restTemplate.exchange(
                "http://localhost:$port/clients/${clientId}",
                HttpMethod.GET,
                null,
                ClientDto::class.java
            ).body!!

            assertThat(result.firstName).isEqualTo(initialClient.firstName)
            assertThat(result.lastName).isEqualTo(initialClient.lastName)
            assertThat(result.email).isEqualTo(initialClient.email)
            assertThat(result.phone).isEqualTo(initialClient.phone)
            assertThat(result.address).isEqualTo(initialClient.address)
        }

        //
        @Test
        fun `fetch all clients`() {
            val anotherClientJson = """
                {
                    "firstName": "Mary",
                    "lastName": "Jane",
                    "email": "mary.jane@420we.ed",
                    "phone": "+1 0003 33 444 555",
                    "address": "High Street 1, Cannabisville"
                }
            """.trimIndent()

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val request = HttpEntity(anotherClientJson, headers)

            val saveRestTemplate = restTemplate.exchange(
                "http://localhost:$port/clients",
                HttpMethod.POST,
                HttpEntity(anotherClientJson, headers),
                String::class.java
            )

            val anotherClient: ClientDto = clientRepository.findAll()[1].let {
                Mappers.getMapper(ClientMapper::class.java).entityToDto(it)
            }

            val result = restTemplate.exchange(
                "http://localhost:$port/clients",
                HttpMethod.GET,
                null,
                Array<ClientDto>::class.java
            ).body

            println("size of result: ${result?.size}")
            result?.forEach {
                println(it)
            }

            assertThat(result).hasSize(2)

            assertThat(result?.get(0)?.firstName).isEqualTo(initialClient.firstName)
            assertThat(result?.get(0)?.lastName).isEqualTo(initialClient.lastName)
            assertThat(result?.get(0)?.email).isEqualTo(initialClient.email)
            assertThat(result?.get(0)?.phone).isEqualTo(initialClient.phone)
            assertThat(result?.get(0)?.address).isEqualTo(initialClient.address)

            assertThat(result?.get(1)?.firstName).isEqualTo(anotherClient.firstName)
            assertThat(result?.get(1)?.lastName).isEqualTo(anotherClient.lastName)
            assertThat(result?.get(1)?.email).isEqualTo(anotherClient.email)
            assertThat(result?.get(1)?.phone).isEqualTo(anotherClient.phone)
            assertThat(result?.get(1)?.address).isEqualTo(anotherClient.address)
        }


        @Test
        fun `update a client`() {
            val updatedClientJson = """
                {
                    "firstName": "Johnny Updated",
                    "lastName": "Silverhand Updated",
                    "email": "updated@nightcity.com",
                    "phone": "+987654321",
                    "address": "Updated Address, Night City"
                }
            """.trimIndent()

            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }

            val request = HttpEntity(updatedClientJson, headers)

            val result = restTemplate.exchange(
                "http://localhost:$port/clients/${initialClient.id}",
                HttpMethod.PUT,
                request,
                ClientDto::class.java
            ).body!!

            assertThat(result.firstName).isEqualTo("Johnny Updated")
            assertThat(result.lastName).isEqualTo("Silverhand Updated")
            assertThat(result.email).isEqualTo("updated@nightcity.com")
            assertThat(result.phone).isEqualTo("+987654321")
            assertThat(result.address).isEqualTo("Updated Address, Night City")
        }

        @Test
        fun `delete a client`() {
            restTemplate.exchange(
                "http://localhost:$port/clients/${clientId}",
                HttpMethod.DELETE,
                null,
                Void::class.java
            )

            val result = clientRepository.findAll()
            assertThat(result).isEmpty()
        }

        @Test
        fun `search client by email`() {
            val result = restTemplate.exchange(
                "http://localhost:$port/clients/search-by-email/${initialClient.email}",
                HttpMethod.GET,
                null,
                ClientDto::class.java
            ).body!!

            assertThat(result.firstName).isEqualTo(initialClient.firstName)
        }

        @Test
        fun `search clients by first name`() {
            val result = restTemplate.exchange(
                "http://localhost:$port/clients/search-by-name/${initialClient.firstName}",
                HttpMethod.GET,
                null,
                Array<ClientDto>::class.java
            ).body!!

            assertThat(result).hasSize(1)
            assertThat(result[0].firstName).isEqualTo(initialClient.firstName)
        }*/
}

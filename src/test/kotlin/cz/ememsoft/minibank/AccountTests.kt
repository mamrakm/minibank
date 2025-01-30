package cz.ememsoft.minibank

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig

@SpringBootTest
@SpringJUnitWebConfig
@Import(TestcontainersConfiguration::class)
class AccountTests
package cz.ememsoft.minibank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.scheduling.annotation.EnableAsync

@EnableR2dbcRepositories
@EnableAsync
@SpringBootApplication
class MinibankApplication

fun main(args: Array<String>) {
    runApplication<MinibankApplication>(*args)
}

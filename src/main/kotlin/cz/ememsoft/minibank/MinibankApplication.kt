package cz.ememsoft.minibank

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync

@EnableAsync
@SpringBootApplication
class MinibankApplication

fun main(args: Array<String>) {
    runApplication<MinibankApplication>(*args)
}

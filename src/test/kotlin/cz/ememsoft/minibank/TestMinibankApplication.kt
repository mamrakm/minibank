package cz.ememsoft.minibank

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<MinibankApplication>().with(TestcontainersConfiguration::class).run(*args)
}

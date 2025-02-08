package cz.ememsoft.minibank

import org.springframework.boot.fromApplication

fun main(args: Array<String>) {
    fromApplication<MinibankApplication>().run(*args)
}

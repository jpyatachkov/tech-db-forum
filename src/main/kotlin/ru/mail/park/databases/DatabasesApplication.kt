package ru.mail.park.databases

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DatabasesApplication

fun main(args: Array<String>) {
    runApplication<DatabasesApplication>(*args)
}

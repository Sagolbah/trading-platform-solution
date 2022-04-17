package ru.ifmo

import io.ktor.server.engine.*
import io.ktor.server.locations.*
import io.ktor.server.netty.*
import ru.ifmo.dao.InMemoryStocksDao
import ru.ifmo.plugins.*

@KtorExperimentalLocationsAPI
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting(InMemoryStocksDao())
    }.start(wait = true)
}

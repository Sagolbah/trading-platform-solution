package ru.ifmo

import io.ktor.client.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import ru.ifmo.plugins.*

fun main() {
    embeddedServer(Netty, port = 8222, host = "0.0.0.0") {
        configureRouting(HttpClient())
    }.start(wait = true)
}

package ru.ifmo.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(httpClient: HttpClient) {

    // Do not allow using admin endpoints

    routing {
        for (route in listOf("newUser", "addMoney", "buy", "sell")) {
            post("/$route/{args...}") {
                call.respondText(httpClient.post("http://localhost:8080" + call.request.uri).bodyAsText())
            }
        }
        for (route in listOf("users", "companies", "stocks")) {
            get("/$route/{args...}") {
                call.respondText(httpClient.get("http://localhost:8080" + call.request.uri).body())
            }
        }
        // Admin endpoints. For testing purposes only.
        // (possible fix: prebuilt data in docker image)
        for (route in listOf("addStocks", "setPrice", "newCompany")) {
            post("/$route/{args...}") {
                call.respondText(httpClient.post("http://localhost:8080" + call.request.uri).bodyAsText())
            }
        }
    }
}

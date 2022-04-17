@file:OptIn(KtorExperimentalLocationsAPI::class)

package ru.ifmo.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.locations.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.ifmo.dao.StocksDao
import ru.ifmo.domain.Company
import ru.ifmo.domain.Stock
import ru.ifmo.domain.User

@KtorExperimentalLocationsAPI
fun Application.configureRouting(dao: StocksDao) {
    install(Locations) {
    }

    routing {
        get<UserIdLocation> {
            val usr = dao.getUser(it.id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(Json.encodeToString(usr))
        }
        get<CompanyLocation> {
            val company = dao.getCompany(it.name) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(Json.encodeToString(company))
        }
        get<StockLocation> {
            val stocks = dao.getStockByName(it.name) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(Json.encodeToString(stocks))
        }
        get<StockAvailableLocation> {
            dao.getUser(it.id) ?: return@get call.respond(HttpStatusCode.NotFound)
            call.respond(Json.encodeToString(dao.getUserStock(it.id, it.name)))
        }
        post("/newCompany/{name}/{price}/{count}") {
            if (call.parameters["name"].isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest) { "Invalid name" }
            try {
                dao.addCompany(
                    Company(
                        call.parameters["name"]!!,
                        Stock(call.parameters["price"]?.toLong()?.takeIf { it >= 0 } ?: 0,
                            call.parameters["count"]?.toLong()?.takeIf { it >= 0 } ?: 0)))
                call.respond(HttpStatusCode.OK)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest) { e.message }
            }
        }
        post("/newUser/{name}") {
            if (call.parameters["name"].isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest) { "Invalid name" }
            call.respondText { dao.addUser(User(call.parameters["name"]!!, 0)).toString() }
        }
        post("/addMoney/{id}/{money}") {
            val arg =
                call.parameters["id"]?.toLong() ?: return@post call.respond(HttpStatusCode.BadRequest) { "Missing id" }
            val usr = dao.getUser(arg) ?: return@post call.respond(HttpStatusCode.NotFound) { "Unknown user" }
            val money = call.parameters["money"]?.toLong()
                ?: return@post call.respond(HttpStatusCode.BadRequest) { "No money provided" }
            dao.addMoney(usr, money)
            call.respond(HttpStatusCode.OK)
        }
        post("/addStocks/{name}/{price}") {
            if (call.parameters["name"].isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest) { "Invalid name" }
            try {
                dao.addStocks(
                    call.parameters["name"]!!,
                    call.parameters["price"]?.toLong() ?: 0
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest) { e.message }
            }
        }
        post("/setPrice/{name}/{price}") {
            if (call.parameters["name"].isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest) { "Invalid name" }
            try {
                dao.modifyPrice(
                    call.parameters["name"]!!,
                    call.parameters["price"]?.toLong() ?: 0
                )
                call.respond(HttpStatusCode.OK)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest) { e.message }
            }
        }
        post("/buy/{id}/{name}/{count}") {
            if (call.parameters["name"].isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest) { "Invalid name" }
            val id =
                call.parameters["id"]?.toLong() ?: return@post call.respond(HttpStatusCode.BadRequest) { "Missing id" }
            try {
                dao.buy(
                    id,
                    call.parameters["name"]!!,
                    call.parameters["count"]?.toLong()?.takeIf { it >= 0 } ?: 0)
                call.respond(HttpStatusCode.OK)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest) { e.message }
            }
        }
        post("/sell/{id}/{name}/{count}") {
            val name = call.parameters["name"]
            if (name.isNullOrBlank()) return@post call.respond(HttpStatusCode.BadRequest) { "Invalid name" }
            val id =
                call.parameters["id"]?.toLong() ?: return@post call.respond(HttpStatusCode.BadRequest) { "Missing id" }
            try {
                dao.sell(
                    id,
                    name,
                    call.parameters["count"]?.toLong()?.takeIf { it >= 0 } ?: 0)
                call.respond(HttpStatusCode.OK)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest) { e.message }
            }
        }
    }
}

@Location("/users/{id}")
data class UserIdLocation(val id: Long)

@Location("/companies/{name}")
data class CompanyLocation(val name: String)

@Location("/stocks/{name}")
data class StockLocation(val name: String)

@Location("/users/{id}/stocks/{name}")
data class StockAvailableLocation(val id: Long, val name: String)


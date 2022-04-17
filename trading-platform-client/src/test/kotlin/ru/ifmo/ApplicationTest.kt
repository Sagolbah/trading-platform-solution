package ru.ifmo

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.EmptyContent.status
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.ifmo.plugins.configureRouting

@Testcontainers
class ApplicationTest {

    companion object {
        @Container
        private val container = FixedHostPortGenericContainer("trading-platform")
            .withExposedPorts(8080)
            .withFixedExposedPort(8080, 8080)

    }

    @Test
    fun testNewUser() = testApplication {
        application {
            configureRouting(HttpClient())
        }
        client.post("/newUser/john").apply {
            assertEquals("0", bodyAsText())
        }
        client.get("/users/0").apply {
            assertEquals("{\"username\":\"john\",\"balance\":0}", bodyAsText())
        }
    }

    @Test
    fun testAddMoney() = testApplication {
        application {
            configureRouting(HttpClient())
        }
        client.post("/newUser/john").apply {
            assertEquals("0", bodyAsText())
        }
        client.post("/addMoney/0/100")
        client.get("/users/0").apply {
            assertEquals("{\"username\":\"john\",\"balance\":100}", bodyAsText())
        }
    }

    @Test
    fun testCompanyStocks() = testApplication {
        application {
            configureRouting(HttpClient())
        }
        client.post("/newCompany/google/10/5")
        client.get("/companies/google").apply {
            assertEquals("{\"name\":\"google\",\"stock\":{\"price\":10,\"count\":5}}", bodyAsText())
        }
        client.post("/newCompany/netflix/2/1")
        client.post("/setPrice/google/20")
        client.post("/addStocks/netflix/5")
        client.get("/companies/google").apply {
            assertEquals("{\"name\":\"google\",\"stock\":{\"price\":20,\"count\":5}}", bodyAsText())
        }
        client.get("/companies/netflix").apply {
            assertEquals("{\"name\":\"netflix\",\"stock\":{\"price\":2,\"count\":6}}", bodyAsText())
        }
    }

    @Test
    fun testBuy() = testApplication {
        application {
            configureRouting(HttpClient())
        }
        client.post("/newUser/john")
        client.post("/newCompany/google/10/5")
        client.get("/companies/google").apply {
            assertEquals("{\"name\":\"google\",\"stock\":{\"price\":10,\"count\":5}}", bodyAsText())
        }
        client.post("/buy/0/google/1")  // attempting to make inconsistent purchase - no money
        client.post("/addMoney/0/200")
        client.post("/buy/0/google/99999")  // too many shares
        client.post("/buy/0/google/3")
        client.get("/companies/google").apply {
            assertEquals("{\"name\":\"google\",\"stock\":{\"price\":10,\"count\":2}}", bodyAsText())
        }
        client.get("/users/0/stocks/google").apply {
            assertEquals("3", bodyAsText())
        }
    }

    @Test
    fun testSell() = testApplication {
        application {
            configureRouting(HttpClient())
        }
        client.post("/newUser/john")
        client.post("/addMoney/0/100")
        client.post("/newCompany/google/10/5")
        client.post("/buy/0/google/5")
        client.post("/setPrice/google/20")
        client.post("/sell/0/google/3")  // sell 3 shares with x2 price - 60 money
        client.get("/companies/google").apply {
            assertEquals("{\"name\":\"google\",\"stock\":{\"price\":20,\"count\":3}}", bodyAsText())
        }
        client.get("/users/0/stocks/google").apply {
            assertEquals("2", bodyAsText())
        }
        client.get("/users/0").apply {
            assertEquals("{\"username\":\"john\",\"balance\":60}", bodyAsText())
        }
    }


}
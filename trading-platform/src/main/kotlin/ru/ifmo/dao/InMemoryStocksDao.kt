package ru.ifmo.dao

import ru.ifmo.domain.Company
import ru.ifmo.domain.Stock
import ru.ifmo.domain.User
import java.lang.Math.abs

class InMemoryStocksDao : StocksDao {
    private val companies = HashMap<String, Company>()
    private val users = HashMap<Long, User>()
    private val userStocks = HashMap<Long, MutableMap<String, Long>>()
    private val companyStocks = HashMap<String, Stock>()

    override fun getUser(id: Long): User? {
        return users[id]
    }

    override fun getCompany(name: String): Company? {
        return companies[name]
    }

    override fun getStockByName(name: String): Stock? {
        return companyStocks[name]
    }

    @Synchronized
    override fun getUserStock(userId: Long, name: String): Long {
        return userStocks[userId]?.get(name) ?: 0
    }

    @Synchronized
    override fun addUser(user: User): Long {
        val newId = users.size.toLong()
        users[newId] = user
        return newId
    }

    override fun addMoney(user: User, money: Long) {
        if (money < 0 && user.balance < kotlin.math.abs(money)) {
            throw IllegalArgumentException("Not enough money to withdraw")
        }
        user.balance += money
    }

    @Synchronized
    override fun addCompany(company: Company) {
        if (companies.containsKey(company.name)) {
            throw IllegalArgumentException("Company already exists")
        }
        companies[company.name] = company
        companyStocks[company.name] = company.stock
    }

    @Synchronized
    override fun buy(userId: Long, companyName: String, count: Long) {
        val availableStocks = companyStocks[companyName] ?: throw IllegalArgumentException("Unknown company")
        val user = users[userId] ?: throw IllegalArgumentException("Unknown user")
        if (availableStocks.count < count) throw IllegalArgumentException("Not enough stocks listed")
        val orderPrice = count * availableStocks.price
        if (user.balance >= orderPrice) {
            user.balance -= orderPrice
            val tmp = userStocks.getOrPut(userId) { mutableMapOf() }
            tmp[companyName] = tmp.getOrDefault(companyName, 0) + count
            companyStocks[companyName]!!.count -= count
        } else {
            throw IllegalArgumentException("Not enough money")
        }
    }

    @Synchronized
    override fun sell(userId: Long, companyName: String, count: Long) {
        val user = users[userId] ?: throw IllegalArgumentException("Unknown user")
        val stocks = userStocks.getOrDefault(userId, emptyMap())[companyName]
        if (stocks == null || stocks < count) throw IllegalArgumentException("Not enough stocks to sell")
        val company = companies[companyName] ?: throw IllegalArgumentException("Unknown company")
        user.balance += count * company.stock.price
        company.stock.count += count
        userStocks[userId]!![companyName] = userStocks[userId]!![companyName]!! - count
    }

    @Synchronized
    override fun modifyPrice(companyName: String, newPrice: Long) {
        val availableStocks = companyStocks[companyName] ?: throw IllegalArgumentException("Unknown company")
        availableStocks.price = newPrice
    }

    @Synchronized
    override fun addStocks(companyName: String, newStocks: Long) {
        val availableStocks = companyStocks[companyName] ?: throw IllegalArgumentException("Unknown company")
        if (newStocks < 0) throw IllegalArgumentException("Attempting to remove stock")
        availableStocks.count += newStocks
    }
}
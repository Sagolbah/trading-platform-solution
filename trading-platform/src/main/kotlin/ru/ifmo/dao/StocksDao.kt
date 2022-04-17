package ru.ifmo.dao

import ru.ifmo.domain.Company
import ru.ifmo.domain.Stock
import ru.ifmo.domain.User

// assuming that tickers are unique, like in real stock market (e.g. YNDX, AMZN, NKE)
interface StocksDao {
    fun getUser(id: Long): User?
    fun getCompany(name: String): Company?
    fun getStockByName(name: String): Stock?
    fun getUserStock(userId: Long, name: String): Long

    fun addUser(user: User): Long  // returns ID of new user
    fun addMoney(user: User, money: Long)
    fun addCompany(company: Company)
    fun buy(userId: Long, companyName: String, count: Long)
    fun sell(userId: Long, companyName: String, count: Long)
    fun modifyPrice(companyName: String, newPrice: Long)
    fun addStocks(companyName: String, newStocks: Long)
}
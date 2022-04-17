package ru.ifmo.domain

@kotlinx.serialization.Serializable
data class Stock(var price: Long, var count: Long)

package ru.ifmo.domain

@kotlinx.serialization.Serializable
data class User(val username: String, var balance: Long)

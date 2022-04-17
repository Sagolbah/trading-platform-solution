package ru.ifmo.domain

@kotlinx.serialization.Serializable
data class Company(val name: String, val stock: Stock)

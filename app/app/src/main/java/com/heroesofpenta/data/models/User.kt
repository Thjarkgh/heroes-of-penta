package com.heroesofpenta.data.models

data class User(
    val id: UInt,
    val name: String,
    val metamaskAddress: String? = null,
    val maxTrainees: UInt = 1u
    // ... other user-specific fields
)
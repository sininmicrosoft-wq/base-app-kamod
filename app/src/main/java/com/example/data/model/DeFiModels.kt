package com.example.data.model

data class Token(
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val balance: Double,
    val iconColor: Long
)

data class LendingMarket(
    val token: String,
    val protocol: String,
    val supplyApy: Double,
    val borrowApy: Double,
    val totalSuppliedUsd: String,
    val totalBorrowedUsd: String,
    val ltvPct: Double,
    var userSupplied: Double = 0.0,
    var userBorrowed: Double = 0.0
)

data class YieldVault(
    val id: String,
    val name: String,
    val protocol: String,
    val underlyingAsset: String,
    val apyPct: Double,
    val tvlUsd: String,
    val riskTier: String, // "Low Risk", "Medium", "Curated"
    var userDeposited: Double = 0.0
)

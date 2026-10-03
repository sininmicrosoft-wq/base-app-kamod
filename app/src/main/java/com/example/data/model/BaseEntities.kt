package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvoiceStatus {
    REQUESTED,
    AUTHORIZED,
    CAPTURED,
    VERIFIED,
    REFUNDED,
    RECONCILED
}

@Entity(tableName = "payment_invoices")
data class PaymentInvoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceId: String,
    val customerName: String,
    val amount: Double,
    val currency: String, // USDC, ETH, cbETH, EURC
    val memo: String,
    val status: InvoiceStatus = InvoiceStatus.REQUESTED,
    val txHash: String = "",
    val paymasterSponsored: Boolean = true,
    val refundReason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val settledAt: Long = 0L
)

@Entity(tableName = "tokenized_assets")
data class B20Asset(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val symbol: String,
    val assetCategory: String, // "Real Estate", "Treasury Bills", "Private Credit", "Commodities"
    val totalValuationUsd: Double,
    val totalSupply: Long,
    val pricePerToken: Double,
    val kycRequired: Boolean = true,
    val accreditedOnly: Boolean = false,
    val transferRestricted: Boolean = true,
    val dividendYieldPct: Double = 5.2,
    val custodian: String = "Securitize / Coinbase Prime",
    val contractAddress: String = "0x" + (1..40).map { "0123456789abcdef".random() }.joinToString(""),
    val totalDistributedUsd: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "stablecoins")
data class Stablecoin(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val symbol: String,
    val pegCurrency: String, // "USD", "EUR", "GBP"
    val totalSupply: Double,
    val fiatReserves: Double,
    val reserveCustodian: String = "BNY Mellon & Cantor Fitzgerald",
    val proofOfReservePct: Double = 100.5,
    val isPaused: Boolean = false,
    val contractAddress: String = "0x" + (1..40).map { "0123456789abcdef".random() }.joinToString(""),
    val totalMintEvents: Int = 1,
    val totalBurnEvents: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "private_transactions")
data class PrivateTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val stealthRecipient: String,
    val amount: Double,
    val token: String,
    val zkProofHash: String,
    val auditViewingKey: String,
    val status: String = "CONFIRMED", // "GENERATING_ZK_PROOF", "CONFIRMED", "SETTLED"
    val proofGenerationMs: Long = 284,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "PAYMENT", "SWAP", "SUPPLY", "BORROW", "MINT", "FAUCET", "PRIVATE_TRANSFER"
    val title: String,
    val amountFormatted: String,
    val network: String,
    val txHash: String,
    val status: String = "CONFIRMED",
    val timestamp: Long = System.currentTimeMillis()
)

package com.example.data.repository

import com.example.data.local.AssetDao
import com.example.data.local.InvoiceDao
import com.example.data.local.PrivateTxDao
import com.example.data.local.StablecoinDao
import com.example.data.local.WalletTxDao
import com.example.data.model.B20Asset
import com.example.data.model.InvoiceStatus
import com.example.data.model.PaymentInvoice
import com.example.data.model.PrivateTransaction
import com.example.data.model.Stablecoin
import com.example.data.model.WalletTransaction
import com.example.data.remote.BaseRpcClient
import com.example.data.remote.NetworkTelemetry
import com.example.data.remote.RpcCallResult
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class BaseRepository(
    private val invoiceDao: InvoiceDao,
    private val assetDao: AssetDao,
    private val stablecoinDao: StablecoinDao,
    private val privateTxDao: PrivateTxDao,
    private val walletTxDao: WalletTxDao,
    private val rpcClient: BaseRpcClient
) {
    val allInvoices: Flow<List<PaymentInvoice>> = invoiceDao.getAllInvoices()
    val allAssets: Flow<List<B20Asset>> = assetDao.getAllAssets()
    val allStablecoins: Flow<List<Stablecoin>> = stablecoinDao.getAllStablecoins()
    val allPrivateTxs: Flow<List<PrivateTransaction>> = privateTxDao.getAllPrivateTxs()
    val allTransactions: Flow<List<WalletTransaction>> = walletTxDao.getAllTransactions()

    suspend fun getNetworkTelemetry(rpcUrl: String, chainId: Long): NetworkTelemetry {
        return rpcClient.getTelemetry(rpcUrl, chainId)
    }

    suspend fun executeRpc(rpcUrl: String, method: String, params: List<Any>): RpcCallResult {
        return rpcClient.executeCustomRpc(rpcUrl, method, params)
    }

    // Invoices / Payments Lifecycle
    suspend fun createInvoice(customer: String, amount: Double, currency: String, memo: String): Long {
        val invoice = PaymentInvoice(
            invoiceId = "inv_base_" + UUID.randomUUID().toString().substring(0, 8),
            customerName = customer,
            amount = amount,
            currency = currency,
            memo = memo,
            status = InvoiceStatus.REQUESTED
        )
        val id = invoiceDao.insertInvoice(invoice)
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "PAYMENT",
                title = "Invoice Created: $customer",
                amountFormatted = "$amount $currency",
                network = "Base",
                txHash = "0x" + UUID.randomUUID().toString().replace("-", "")
            )
        )
        return id
    }

    suspend fun advanceInvoiceStatus(invoice: PaymentInvoice, targetStatus: InvoiceStatus) {
        val updatedTx = when (targetStatus) {
            InvoiceStatus.AUTHORIZED -> invoice.copy(
                status = targetStatus,
                txHash = if (invoice.txHash.isEmpty()) "0xauth_" + UUID.randomUUID().toString().substring(0, 16) else invoice.txHash
            )
            InvoiceStatus.CAPTURED -> invoice.copy(
                status = targetStatus,
                txHash = "0xcap_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32),
                settledAt = System.currentTimeMillis()
            )
            InvoiceStatus.VERIFIED -> invoice.copy(
                status = targetStatus
            )
            InvoiceStatus.REFUNDED -> invoice.copy(
                status = targetStatus,
                refundReason = "Customer requested reversal",
                txHash = "0xref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32)
            )
            InvoiceStatus.RECONCILED -> invoice.copy(
                status = targetStatus
            )
            else -> invoice.copy(status = targetStatus)
        }
        invoiceDao.updateInvoice(updatedTx)

        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "PAYMENT",
                title = "Payment ${targetStatus.name.lowercase().replaceFirstChar { it.uppercase() }}: ${invoice.customerName}",
                amountFormatted = "${invoice.amount} ${invoice.currency}",
                network = "Base",
                txHash = updatedTx.txHash
            )
        )
    }

    suspend fun deleteInvoice(invoice: PaymentInvoice) {
        invoiceDao.deleteInvoice(invoice)
    }

    // Tokenized Assets (RWA B20)
    suspend fun issueB20Asset(
        name: String,
        symbol: String,
        category: String,
        valuation: Double,
        supply: Long,
        dividendYield: Double
    ): Long {
        val price = if (supply > 0) valuation / supply.toDouble() else 1.0
        val asset = B20Asset(
            name = name,
            symbol = symbol,
            assetCategory = category,
            totalValuationUsd = valuation,
            totalSupply = supply,
            pricePerToken = price,
            dividendYieldPct = dividendYield
        )
        val id = assetDao.insertAsset(asset)
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "MINT",
                title = "Tokenized Asset Issued: $symbol",
                amountFormatted = "$supply tokens ($${String.format("%.2f", valuation)})",
                network = "Base",
                txHash = asset.contractAddress
            )
        )
        return id
    }

    suspend fun distributeDividends(asset: B20Asset, totalAmountUsd: Double) {
        val updated = asset.copy(
            totalDistributedUsd = asset.totalDistributedUsd + totalAmountUsd
        )
        assetDao.updateAsset(updated)
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "SUPPLY",
                title = "USDC Dividend Payout (${asset.symbol})",
                amountFormatted = "+$${String.format("%.2f", totalAmountUsd)} USDC",
                network = "Base",
                txHash = "0xdiv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 32)
            )
        )
    }

    suspend fun deleteAsset(asset: B20Asset) {
        assetDao.deleteAsset(asset)
    }

    // Stablecoins
    suspend fun issueStablecoin(
        name: String,
        symbol: String,
        peg: String,
        initialMint: Double
    ): Long {
        val sc = Stablecoin(
            name = name,
            symbol = symbol,
            pegCurrency = peg,
            totalSupply = initialMint,
            fiatReserves = initialMint * 1.002 // 100.2% reserve backing
        )
        val id = stablecoinDao.insertStablecoin(sc)
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "MINT",
                title = "Stablecoin Deployed: $symbol",
                amountFormatted = "$${String.format("%.2f", initialMint)} $symbol",
                network = "Base",
                txHash = sc.contractAddress
            )
        )
        return id
    }

    suspend fun mintOrBurnStablecoin(stablecoin: Stablecoin, amount: Double, isMint: Boolean) {
        val newSupply = if (isMint) stablecoin.totalSupply + amount else (stablecoin.totalSupply - amount).coerceAtLeast(0.0)
        val newReserve = if (isMint) stablecoin.fiatReserves + amount else (stablecoin.fiatReserves - amount).coerceAtLeast(0.0)
        val updated = stablecoin.copy(
            totalSupply = newSupply,
            fiatReserves = newReserve,
            totalMintEvents = if (isMint) stablecoin.totalMintEvents + 1 else stablecoin.totalMintEvents,
            totalBurnEvents = if (!isMint) stablecoin.totalBurnEvents + 1 else stablecoin.totalBurnEvents
        )
        stablecoinDao.updateStablecoin(updated)
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = if (isMint) "MINT" else "BURN",
                title = if (isMint) "Minted $amount ${stablecoin.symbol}" else "Burned $amount ${stablecoin.symbol}",
                amountFormatted = "${if (isMint) "+" else "-"}$amount ${stablecoin.symbol}",
                network = "Base",
                txHash = "0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 32)
            )
        )
    }

    suspend fun toggleStablecoinPause(stablecoin: Stablecoin) {
        val updated = stablecoin.copy(isPaused = !stablecoin.isPaused)
        stablecoinDao.updateStablecoin(updated)
    }

    suspend fun deleteStablecoin(stablecoin: Stablecoin) {
        stablecoinDao.deleteStablecoin(stablecoin)
    }

    // Private Transactions
    suspend fun executePrivateTransfer(stealthAddress: String, amount: Double, token: String): Long {
        val zkProof = "0xzk_" + UUID.randomUUID().toString().replace("-", "")
        val viewingKey = "vk_base_" + UUID.randomUUID().toString().substring(0, 16)
        val tx = PrivateTransaction(
            stealthRecipient = stealthAddress,
            amount = amount,
            token = token,
            zkProofHash = zkProof,
            auditViewingKey = viewingKey,
            proofGenerationMs = (180..340).random().toLong()
        )
        val id = privateTxDao.insertPrivateTx(tx)
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "PRIVATE_TRANSFER",
                title = "Shielded Ledger Settlement",
                amountFormatted = "$amount $token (Confidential)",
                network = "Base Ledgers",
                txHash = zkProof
            )
        )
        return id
    }

    suspend fun recordWalletTx(title: String, amountFormatted: String, network: String = "Base") {
        walletTxDao.insertTransaction(
            WalletTransaction(
                type = "DEFI",
                title = title,
                amountFormatted = amountFormatted,
                network = network,
                txHash = "0x" + UUID.randomUUID().toString().replace("-", "").substring(0, 32)
            )
        )
    }

    // Prepopulate sample realistic data if database is empty
    suspend fun prepopulateIfEmpty() {
        // Will be called in ViewModel init
    }
}

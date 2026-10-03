package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BaseDatabase
import com.example.data.model.B20Asset
import com.example.data.model.BaseNetwork
import com.example.data.model.InvoiceStatus
import com.example.data.model.LendingMarket
import com.example.data.model.PaymentInvoice
import com.example.data.model.PrivateTransaction
import com.example.data.model.Stablecoin
import com.example.data.model.Token
import com.example.data.model.WalletTransaction
import com.example.data.model.YieldVault
import com.example.data.remote.BaseRpcClient
import com.example.data.remote.NetworkTelemetry
import com.example.data.remote.RpcCallResult
import com.example.data.repository.BaseRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BaseViewModel(application: Application) : AndroidViewModel(application) {
    private val db = BaseDatabase.getDatabase(application)
    private val rpcClient = BaseRpcClient()
    private val retrofitClient = com.example.data.remote.BaseRetrofitClient()
    private val repository = BaseRepository(
        invoiceDao = db.invoiceDao(),
        assetDao = db.assetDao(),
        stablecoinDao = db.stablecoinDao(),
        privateTxDao = db.privateTxDao(),
        walletTxDao = db.walletTxDao(),
        rpcClient = rpcClient
    )

    // Retrofit Live Telemetry
    private val notificationManager = com.example.notification.GasAlertNotificationManager(application)

    private val _gasAlertConfig = MutableStateFlow(
        com.example.notification.GasAlertConfig(
            isEnabled = true,
            thresholdGwei = 0.0050,
            cooldownSeconds = 60L
        )
    )
    val gasAlertConfig: StateFlow<com.example.notification.GasAlertConfig> = _gasAlertConfig.asStateFlow()

    private val _gasAlertHistory = MutableStateFlow<List<com.example.notification.GasAlertHistoryItem>>(
        listOf(
            com.example.notification.GasAlertHistoryItem(
                gasPriceGwei = 0.0078,
                thresholdGwei = 0.0050,
                timestamp = System.currentTimeMillis() - 7200000,
                network = "Base Mainnet"
            )
        )
    )
    val gasAlertHistory: StateFlow<List<com.example.notification.GasAlertHistoryItem>> = _gasAlertHistory.asStateFlow()

    private val _liveTelemetry = MutableStateFlow(
        com.example.data.remote.BaseLiveTelemetry(
            blockHeight = 24200840L,
            gasPriceGwei = 0.0042,
            gasPriceUsd = 0.00064,
            blockTxVolume = 172,
            estimatedTps = 86.0,
            gasUsedHex = "0x1bfa30",
            blockHash = "0x892a...4b12",
            latencyMs = 54,
            rpcEndpoint = "https://mainnet.base.org",
            isLive = true
        )
    )
    val liveTelemetry: StateFlow<com.example.data.remote.BaseLiveTelemetry> = _liveTelemetry.asStateFlow()

    private val _isTelemetryRefreshing = MutableStateFlow(false)
    val isTelemetryRefreshing: StateFlow<Boolean> = _isTelemetryRefreshing.asStateFlow()

    // Network & Telemetry
    private val _selectedNetwork = MutableStateFlow(BaseNetwork.MAINNET)
    val selectedNetwork: StateFlow<BaseNetwork> = _selectedNetwork.asStateFlow()

    private val _telemetry = MutableStateFlow(
        NetworkTelemetry(
            blockNumber = 24190824L,
            gasPriceGwei = 0.004,
            chainId = 8453,
            latencyMs = 62,
            isLive = true
        )
    )
    val telemetry: StateFlow<NetworkTelemetry> = _telemetry.asStateFlow()

    // Smart Wallet & Account Abstraction (ERC-4337)
    val smartWalletAddress = "0x84537c3B91d9472Da4e0bC1045E1643c7B512061"
    val paymasterAddress = "0x000043370429f4309328B0ef13567D2c3a5026B3"

    private val _tokens = MutableStateFlow(
        listOf(
            Token("ETH", "Ethereum (Base)", 3420.50, 1.450, 0xFF627EEA),
            Token("USDC", "USD Coin (Native)", 1.00, 2850.00, 0xFF2775CA),
            Token("cbETH", "Coinbase Wrapped Staked ETH", 3640.20, 0.500, 0xFF0052FF),
            Token("AERO", "Aerodrome Finance", 1.28, 650.0, 0xFF00E5FF),
            Token("DEGEN", "Degen Token", 0.0142, 12000.0, 0xFF9945FF)
        )
    )
    val tokens: StateFlow<List<Token>> = _tokens.asStateFlow()

    // DeFi Markets
    private val _lendingMarkets = MutableStateFlow(
        listOf(
            LendingMarket("USDC", "Aave v3 Base", 5.4, 6.8, "$482M", "$210M", 80.0, userSupplied = 1000.0, userBorrowed = 0.0),
            LendingMarket("ETH", "Aave v3 Base", 2.1, 3.4, "$310M", "$95M", 82.5, userSupplied = 0.5, userBorrowed = 0.0),
            LendingMarket("cbETH", "Aave v3 Base", 3.2, 4.6, "$190M", "$42M", 75.0, userSupplied = 0.0, userBorrowed = 0.0)
        )
    )
    val lendingMarkets: StateFlow<List<LendingMarket>> = _lendingMarkets.asStateFlow()

    private val _yieldVaults = MutableStateFlow(
        listOf(
            YieldVault("v1", "Morpho Blue USDC Prime", "Morpho", "USDC", 7.45, "$84M", "Low Risk", userDeposited = 500.0),
            YieldVault("v2", "Yearn Base Liquidity Multiplier", "Yearn", "USDC", 6.10, "$32M", "Curated", userDeposited = 0.0),
            YieldVault("v3", "cbETH Liquid Restaking Pool", "Symbiotic / Base", "cbETH", 4.80, "$55M", "Medium", userDeposited = 0.2)
        )
    )
    val yieldVaults: StateFlow<List<YieldVault>> = _yieldVaults.asStateFlow()

    // RPC Playground State
    private val _rpcConsoleResult = MutableStateFlow<RpcCallResult?>(null)
    val rpcConsoleResult: StateFlow<RpcCallResult?> = _rpcConsoleResult.asStateFlow()

    private val _isRpcLoading = MutableStateFlow(false)
    val isRpcLoading: StateFlow<Boolean> = _isRpcLoading.asStateFlow()

    // Database Flows
    val invoices: StateFlow<List<PaymentInvoice>> = repository.allInvoices.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val assets: StateFlow<List<B20Asset>> = repository.allAssets.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val stablecoins: StateFlow<List<Stablecoin>> = repository.allStablecoins.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val privateTxs: StateFlow<List<PrivateTransaction>> = repository.allPrivateTxs.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val walletTransactions: StateFlow<List<WalletTransaction>> = repository.allTransactions.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    private var telemetryPollingJob: Job? = null

    init {
        prepopulateInitialData()
        startTelemetryPolling()
    }

    private fun startTelemetryPolling() {
        telemetryPollingJob?.cancel()
        telemetryPollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val currentNet = _selectedNetwork.value
                    val result = repository.getNetworkTelemetry(currentNet.rpcUrl, currentNet.chainId)
                    _telemetry.value = result

                    // Fetch Retrofit Live Telemetry (Block height, gas price, transaction volume)
                    val liveResult = retrofitClient.fetchLiveTelemetry(currentNet.rpcUrl)
                    _liveTelemetry.value = liveResult
                    checkGasThresholdAndNotify(liveResult.gasPriceGwei)
                } catch (e: Exception) {
                    // Handled inside BaseRpcClient
                }
                delay(30_000L) // Poll every 30 seconds automatically
            }
        }
    }

    fun setNetwork(network: BaseNetwork) {
        _selectedNetwork.value = network
        viewModelScope.launch {
            val result = repository.getNetworkTelemetry(network.rpcUrl, network.chainId)
            _telemetry.value = result
            val liveResult = retrofitClient.fetchLiveTelemetry(network.rpcUrl)
            _liveTelemetry.value = liveResult
            checkGasThresholdAndNotify(liveResult.gasPriceGwei)
        }
    }

    fun refreshTelemetry() {
        viewModelScope.launch {
            _isTelemetryRefreshing.value = true
            val net = _selectedNetwork.value
            _telemetry.value = repository.getNetworkTelemetry(net.rpcUrl, net.chainId)
            val liveResult = retrofitClient.fetchLiveTelemetry(net.rpcUrl)
            _liveTelemetry.value = liveResult
            checkGasThresholdAndNotify(liveResult.gasPriceGwei)
            _isTelemetryRefreshing.value = false
        }
    }

    // Push Notification Gas Threshold Controls
    fun setGasAlertThreshold(thresholdGwei: Double) {
        _gasAlertConfig.value = _gasAlertConfig.value.copy(thresholdGwei = thresholdGwei)
    }

    // Vibenet Faucet Integration
    private val _faucetHistory = MutableStateFlow<List<com.example.data.remote.VibenetFaucetResult>>(emptyList())
    val faucetHistory: StateFlow<List<com.example.data.remote.VibenetFaucetResult>> = _faucetHistory.asStateFlow()

    private val _isFaucetLoading = MutableStateFlow(false)
    val isFaucetLoading: StateFlow<Boolean> = _isFaucetLoading.asStateFlow()

    suspend fun requestVibenetFaucet(address: String): com.example.data.remote.VibenetFaucetResult {
        _isFaucetLoading.value = true
        return try {
            val result = retrofitClient.requestVibenetFaucetDrip(address)
            _faucetHistory.value = listOf(result) + _faucetHistory.value
            result
        } finally {
            _isFaucetLoading.value = false
        }
    }

    fun setGasAlertEnabled(enabled: Boolean) {
        _gasAlertConfig.value = _gasAlertConfig.value.copy(isEnabled = enabled)
    }

    fun triggerTestGasAlert(customGasPriceGwei: Double? = null): Boolean {
        val cfg = _gasAlertConfig.value
        val gasToReport = customGasPriceGwei ?: (cfg.thresholdGwei + 0.0028)
        val net = _selectedNetwork.value.displayName
        val fired = notificationManager.sendGasAlertNotification(
            currentGasGwei = gasToReport,
            thresholdGwei = cfg.thresholdGwei,
            networkName = net
        )
        if (fired) {
            val item = com.example.notification.GasAlertHistoryItem(
                gasPriceGwei = gasToReport,
                thresholdGwei = cfg.thresholdGwei,
                network = net
            )
            _gasAlertHistory.value = listOf(item) + _gasAlertHistory.value
            _gasAlertConfig.value = cfg.copy(
                totalAlertsFired = cfg.totalAlertsFired + 1,
                lastAlertTimestamp = System.currentTimeMillis()
            )
        }
        return fired
    }

    private fun checkGasThresholdAndNotify(currentGasGwei: Double) {
        val cfg = _gasAlertConfig.value
        if (!cfg.isEnabled) return

        val now = System.currentTimeMillis()
        val elapsedSec = (now - cfg.lastAlertTimestamp) / 1000

        if (currentGasGwei >= cfg.thresholdGwei && elapsedSec >= cfg.cooldownSeconds) {
            val net = _selectedNetwork.value.displayName
            val fired = notificationManager.sendGasAlertNotification(
                currentGasGwei = currentGasGwei,
                thresholdGwei = cfg.thresholdGwei,
                networkName = net
            )
            if (fired) {
                val item = com.example.notification.GasAlertHistoryItem(
                    gasPriceGwei = currentGasGwei,
                    thresholdGwei = cfg.thresholdGwei,
                    network = net
                )
                _gasAlertHistory.value = listOf(item) + _gasAlertHistory.value
                _gasAlertConfig.value = cfg.copy(
                    totalAlertsFired = cfg.totalAlertsFired + 1,
                    lastAlertTimestamp = now
                )
            }
        }
    }

    // Invoice lifecycle actions
    fun createInvoice(customer: String, amount: Double, currency: String, memo: String) {
        viewModelScope.launch {
            repository.createInvoice(customer, amount, currency, memo)
        }
    }

    fun advanceInvoice(invoice: PaymentInvoice, targetStatus: InvoiceStatus) {
        viewModelScope.launch {
            repository.advanceInvoiceStatus(invoice, targetStatus)
        }
    }

    fun deleteInvoice(invoice: PaymentInvoice) {
        viewModelScope.launch {
            repository.deleteInvoice(invoice)
        }
    }

    // RWA Tokenization actions
    fun issueB20Asset(
        name: String,
        symbol: String,
        category: String,
        valuation: Double,
        supply: Long,
        dividendYield: Double
    ) {
        viewModelScope.launch {
            repository.issueB20Asset(name, symbol, category, valuation, supply, dividendYield)
        }
    }

    fun distributeDividends(asset: B20Asset, totalAmountUsd: Double) {
        viewModelScope.launch {
            repository.distributeDividends(asset, totalAmountUsd)
        }
    }

    fun deleteAsset(asset: B20Asset) {
        viewModelScope.launch {
            repository.deleteAsset(asset)
        }
    }

    // Stablecoin actions
    fun issueStablecoin(name: String, symbol: String, peg: String, initialSupply: Double) {
        viewModelScope.launch {
            repository.issueStablecoin(name, symbol, peg, initialSupply)
        }
    }

    fun mintOrBurnStablecoin(stablecoin: Stablecoin, amount: Double, isMint: Boolean) {
        viewModelScope.launch {
            repository.mintOrBurnStablecoin(stablecoin, amount, isMint)
        }
    }

    fun toggleStablecoinPause(stablecoin: Stablecoin) {
        viewModelScope.launch {
            repository.toggleStablecoinPause(stablecoin)
        }
    }

    fun deleteStablecoin(stablecoin: Stablecoin) {
        viewModelScope.launch {
            repository.deleteStablecoin(stablecoin)
        }
    }

    // Private Transfers (Base Ledgers)
    fun executePrivateTransfer(stealthAddress: String, amount: Double, token: String) {
        viewModelScope.launch {
            repository.executePrivateTransfer(stealthAddress, amount, token)
        }
    }

    // DeFi Swap Simulation
    fun executeSwap(fromSymbol: String, toSymbol: String, fromAmount: Double, toAmount: Double) {
        val currentTokens = _tokens.value.toMutableList()
        val fromIdx = currentTokens.indexOfFirst { it.symbol == fromSymbol }
        val toIdx = currentTokens.indexOfFirst { it.symbol == toSymbol }
        if (fromIdx != -1 && toIdx != -1 && currentTokens[fromIdx].balance >= fromAmount) {
            currentTokens[fromIdx] = currentTokens[fromIdx].copy(balance = currentTokens[fromIdx].balance - fromAmount)
            currentTokens[toIdx] = currentTokens[toIdx].copy(balance = currentTokens[toIdx].balance + toAmount)
            _tokens.value = currentTokens

            viewModelScope.launch {
                repository.recordWalletTx(
                    title = "Swap $fromSymbol for $toSymbol (Aerodrome AMM)",
                    amountFormatted = "-$fromAmount $fromSymbol / +$toAmount $toSymbol"
                )
            }
        }
    }

    // DeFi Supply / Borrow
    fun supplyToLending(tokenSymbol: String, amount: Double) {
        val markets = _lendingMarkets.value.toMutableList()
        val idx = markets.indexOfFirst { it.token == tokenSymbol }
        if (idx != -1) {
            markets[idx] = markets[idx].copy(userSupplied = markets[idx].userSupplied + amount)
            _lendingMarkets.value = markets
            viewModelScope.launch {
                repository.recordWalletTx(
                    title = "Supplied $amount $tokenSymbol to Aave v3 Base",
                    amountFormatted = "+$amount $tokenSymbol"
                )
            }
        }
    }

    fun depositToVault(vaultId: String, amount: Double) {
        val vaults = _yieldVaults.value.toMutableList()
        val idx = vaults.indexOfFirst { it.id == vaultId }
        if (idx != -1) {
            vaults[idx] = vaults[idx].copy(userDeposited = vaults[idx].userDeposited + amount)
            _yieldVaults.value = vaults
            viewModelScope.launch {
                repository.recordWalletTx(
                    title = "Deposited $amount ${vaults[idx].underlyingAsset} to ${vaults[idx].name}",
                    amountFormatted = "+$amount ${vaults[idx].underlyingAsset}"
                )
            }
        }
    }

    // Vibenet testnet faucet
    fun requestFaucetFunds() {
        val currentTokens = _tokens.value.toMutableList()
        val ethIdx = currentTokens.indexOfFirst { it.symbol == "ETH" }
        val usdcIdx = currentTokens.indexOfFirst { it.symbol == "USDC" }
        if (ethIdx != -1) {
            currentTokens[ethIdx] = currentTokens[ethIdx].copy(balance = currentTokens[ethIdx].balance + 1.0)
        }
        if (usdcIdx != -1) {
            currentTokens[usdcIdx] = currentTokens[usdcIdx].copy(balance = currentTokens[usdcIdx].balance + 1000.0)
        }
        _tokens.value = currentTokens

        viewModelScope.launch {
            repository.recordWalletTx(
                title = "Vibenet Testnet Faucet Dispense",
                amountFormatted = "+1.0 ETH, +1,000 USDC",
                network = "Vibenet"
            )
        }
    }

    // RPC Playground Execution
    fun runCustomRpc(method: String, params: List<Any>) {
        viewModelScope.launch {
            _isRpcLoading.value = true
            val net = _selectedNetwork.value
            val result = repository.executeRpc(net.rpcUrl, method, params)
            _rpcConsoleResult.value = result
            _isRpcLoading.value = false
        }
    }

    private fun prepopulateInitialData() {
        viewModelScope.launch {
            // Check if invoices are empty
            db.invoiceDao().insertInvoice(
                PaymentInvoice(
                    invoiceId = "inv_base_a912f4",
                    customerName = "Superfluid Labs Inc",
                    amount = 450.00,
                    currency = "USDC",
                    memo = "Enterprise API Gateway Subscription - Q4",
                    status = InvoiceStatus.CAPTURED,
                    txHash = "0x892e591c28f099c017bc4498aa2938174510bc4409ef182a491bc0",
                    paymasterSponsored = true,
                    settledAt = System.currentTimeMillis() - 3600000
                )
            )

            db.assetDao().insertAsset(
                B20Asset(
                    name = "US Short-Term Treasury Note 3M (B20)",
                    symbol = "bTBILL",
                    assetCategory = "Treasury Bills",
                    totalValuationUsd = 15000000.00,
                    totalSupply = 150000,
                    pricePerToken = 100.00,
                    kycRequired = true,
                    accreditedOnly = false,
                    transferRestricted = true,
                    dividendYieldPct = 5.18,
                    custodian = "Coinbase Prime & BNY Mellon",
                    totalDistributedUsd = 194250.00
                )
            )

            db.stablecoinDao().insertStablecoin(
                Stablecoin(
                    name = "Base Dollar",
                    symbol = "bUSD",
                    pegCurrency = "USD",
                    totalSupply = 2500000.00,
                    fiatReserves = 2505000.00,
                    reserveCustodian = "BNY Mellon (Cash & US 30-day T-Bills)",
                    proofOfReservePct = 100.2,
                    isPaused = false
                )
            )

            db.privateTxDao().insertPrivateTx(
                PrivateTransaction(
                    stealthRecipient = "stealth_0x8f4c...91bc",
                    amount = 250.0,
                    token = "USDC",
                    zkProofHash = "0xzk_8a992bc0183e9102cae1",
                    auditViewingKey = "vk_base_pub_9a102",
                    status = "SETTLED",
                    proofGenerationMs = 214
                )
            )
        }
    }

    // Vibenet Real-time WebSocket Client
    val vibenetWsClient = com.example.data.remote.VibenetWebSocketClient(
        scope = viewModelScope,
        endpointUrl = "wss://rpc.vibes.base.org/ws"
    )

    fun connectVibenetWs() = vibenetWsClient.connect()
    fun disconnectVibenetWs() = vibenetWsClient.disconnect()

    override fun onCleared() {
        super.onCleared()
        vibenetWsClient.disconnect()
    }
}

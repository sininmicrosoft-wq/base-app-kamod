package com.example.data.model

enum class BaseNetwork(
    val chainId: Long,
    val displayName: String,
    val rpcUrl: String,
    val explorerUrl: String,
    val isTestnet: Boolean,
    val currencySymbol: String = "ETH"
) {
    MAINNET(
        chainId = 8453,
        displayName = "Base Mainnet",
        rpcUrl = "https://mainnet.base.org",
        explorerUrl = "https://basescan.org",
        isTestnet = false
    ),
    SEPOLIA(
        chainId = 84532,
        displayName = "Base Sepolia",
        rpcUrl = "https://sepolia.base.org",
        explorerUrl = "https://sepolia.basescan.org",
        isTestnet = true
    ),
    VIBENET(
        chainId = 845399,
        displayName = "Vibenet (Devnet)",
        rpcUrl = "https://mainnet.base.org", // Uses mainnet mirror for RPC queries in dev mode
        explorerUrl = "https://basescan.org",
        isTestnet = true
    )
}

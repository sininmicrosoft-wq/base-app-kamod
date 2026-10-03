package com.example.data.model

enum class BaseNetwork(
    val chainId: Long,
    val displayName: String,
    val rpcUrl: String,
    val explorerUrl: String,
    val isTestnet: Boolean,
    val currencySymbol: String = "ETH",
    val wsUrl: String = "",
    val faucetUrl: String = "",
    val blockTimeMs: Int = 2000
) {
    MAINNET(
        chainId = 8453,
        displayName = "Base Mainnet",
        rpcUrl = "https://mainnet.base.org",
        explorerUrl = "https://basescan.org",
        isTestnet = false,
        wsUrl = "",
        faucetUrl = "",
        blockTimeMs = 2000
    ),
    SEPOLIA(
        chainId = 84532,
        displayName = "Base Sepolia",
        rpcUrl = "https://sepolia.base.org",
        explorerUrl = "https://sepolia.basescan.org",
        isTestnet = true,
        wsUrl = "",
        faucetUrl = "https://www.coinbase.com/faucets/base-ethereum-sepolia-faucet",
        blockTimeMs = 2000
    ),
    VIBENET(
        chainId = 84538453L,
        displayName = "Base Vibenet",
        rpcUrl = "https://rpc.vibes.base.org",
        explorerUrl = "https://chain.base.org/vibenet/explorer",
        isTestnet = true,
        wsUrl = "wss://rpc.vibes.base.org/ws",
        faucetUrl = "https://chain.base.org/vibenet/faucet",
        blockTimeMs = 200
    )
}

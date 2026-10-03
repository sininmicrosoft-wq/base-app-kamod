package com.example

import com.example.notification.GasAlertConfig
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun gasThresholdAlert_exceededEvaluation() {
    val config = GasAlertConfig(isEnabled = true, thresholdGwei = 0.0050)
    val lowGas = 0.0035
    val spikeGas = 0.0082

    assertFalse(lowGas >= config.thresholdGwei)
    assertTrue(spikeGas >= config.thresholdGwei)
  }

  @Test
  fun gasThresholdAlert_disabledState() {
    val config = GasAlertConfig(isEnabled = false, thresholdGwei = 0.0050)
    val spikeGas = 0.0090

    // When disabled, no alert should be sent even if gas exceeds threshold
    val shouldNotify = config.isEnabled && (spikeGas >= config.thresholdGwei)
    assertFalse(shouldNotify)
  }

  @Test
  fun vibenetNetwork_specificationsValid() {
    val vibenet = com.example.data.model.BaseNetwork.VIBENET
    assertEquals(84538453L, vibenet.chainId)
    assertEquals("https://rpc.vibes.base.org", vibenet.rpcUrl)
    assertEquals("wss://rpc.vibes.base.org/ws", vibenet.wsUrl)
    assertEquals("https://chain.base.org/vibenet/explorer", vibenet.explorerUrl)
    assertEquals(200, vibenet.blockTimeMs)
    assertTrue(vibenet.isTestnet)
  }

  @Test
  fun vibenetFaucet_addressValidation() {
    val validAddress = "0x71C84539820f4E71B20f92418a901f4c7849a290"
    val invalidShort = "0x1234"
    val invalidNoPrefix = "71C84539820f4E71B20f92418a901f4c7849a290"
    val invalidChars = "0xZZZ84539820f4E71B20f92418a901f4c7849a290"

    fun isValid(addr: String): Boolean {
      val trimmed = addr.trim()
      return trimmed.startsWith("0x") && trimmed.length == 42 && trimmed.drop(2).all { it in "0123456789abcdefABCDEF" }
    }

    assertTrue(isValid(validAddress))
    assertFalse(isValid(invalidShort))
    assertFalse(isValid(invalidNoPrefix))
    assertFalse(isValid(invalidChars))
  }

  @Test
  fun vibenetFaucet_resultModel() {
    val result = com.example.data.remote.VibenetFaucetResult(
      isSuccess = true,
      recipientAddress = "0x71C84539820f4E71B20f92418a901f4c7849a290",
      amountEth = 0.5,
      txHash = "0x8453abcdef123456",
      message = "Drip confirmed"
    )
    assertTrue(result.isSuccess)
    assertEquals(0.5, result.amountEth, 0.001)
    assertTrue(result.txHash.startsWith("0x"))
  }

  @Test
  fun vibenetBlockHeader_dataModelValid() {
    val header = com.example.data.remote.VibenetBlockHeader(
      blockNumber = 1048590L,
      blockNumberHex = "0x10007e",
      hash = "0x8453abcdef0123456789",
      parentHash = "0x8453prev0123456789",
      gasUsed = 125000L,
      gasLimit = 30000000L,
      baseFeeGwei = 0.0042,
      timestamp = System.currentTimeMillis(),
      latencyMs = 202L,
      rawJson = "{\"jsonrpc\":\"2.0\"}"
    )

    assertEquals(1048590L, header.blockNumber)
    assertEquals("0x10007e", header.blockNumberHex)
    assertTrue(header.gasUsed < header.gasLimit)
    assertTrue(header.latencyMs in 150L..350L)
    assertTrue(header.hash.startsWith("0x"))
  }

  @Test
  fun b20Asset_supplyCapAndHolderCountValid() {
    val b20 = com.example.data.model.B20Asset(
      name = "Base Commercial Paper Prime Yield",
      symbol = "bCP-USD",
      assetCategory = "Private Credit",
      totalValuationUsd = 12500000.0,
      totalSupply = 12500000L,
      supplyCap = 25000000L,
      holderCount = 1842,
      pricePerToken = 1.0,
      memosEnabled = true,
      network = "Base Vibenet"
    )

    assertEquals(12500000L, b20.totalSupply)
    assertEquals(25000000L, b20.supplyCap)
    assertEquals(1842, b20.holderCount)
    assertTrue(b20.supplyCap >= b20.totalSupply)

    val utilizationPct = (b20.totalSupply.toDouble() / b20.supplyCap.toDouble()) * 100.0
    assertEquals(50.0, utilizationPct, 0.01)
    assertTrue(b20.memosEnabled)
  }

  @Test
  fun ethBalance_weiToEthConversion() {
    val weiHex = "0x221b29270e060000" // 2.4576 ETH in hex
    val weiBigInt = java.math.BigInteger(weiHex.removePrefix("0x"), 16)
    val eth = weiBigInt.toDouble() / 1e18

    assertEquals(2.4576, eth, 0.001)

    val ethPriceUsd = 3450.0
    val usd = eth * ethPriceUsd
    assertEquals(8478.72, usd, 0.5)

    val result = com.example.data.remote.WalletEthBalanceResult(
      address = "0x71C84539820f4E71B20f92418a901f4c7849a290",
      balanceWei = weiBigInt.toString(),
      balanceEth = eth,
      balanceUsd = usd,
      rpcEndpoint = "https://mainnet.base.org",
      latencyMs = 24L
    )

    assertTrue(result.isSuccess)
    assertEquals("0x71C84539820f4E71B20f92418a901f4c7849a290", result.address)
    assertEquals(2.4576, result.balanceEth, 0.001)
  }
}


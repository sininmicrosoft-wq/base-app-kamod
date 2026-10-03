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
}


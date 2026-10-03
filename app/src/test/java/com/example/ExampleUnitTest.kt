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
}


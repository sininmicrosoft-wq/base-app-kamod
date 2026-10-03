package com.example.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

data class GasAlertConfig(
    val isEnabled: Boolean = true,
    val thresholdGwei: Double = 0.0060, // Alert if exceeds 0.0060 Gwei
    val lastAlertTimestamp: Long = 0L,
    val totalAlertsFired: Int = 0,
    val cooldownSeconds: Long = 60L // At least 60s between duplicate alerts
)

data class GasAlertHistoryItem(
    val id: Long = System.currentTimeMillis(),
    val gasPriceGwei: Double,
    val thresholdGwei: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val network: String = "Base Mainnet"
)

class GasAlertNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "base_gas_threshold_alerts"
        const val CHANNEL_NAME = "Base Gas Threshold Alerts"
        const val CHANNEL_DESC = "Notifications fired when Base L2 gas price exceeds user-defined threshold"
        const val NOTIFICATION_ID = 8453
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Sends an Android notification alerting that Base gas price has exceeded threshold.
     */
    fun sendGasAlertNotification(
        currentGasGwei: Double,
        thresholdGwei: Double,
        networkName: String = "Base Mainnet"
    ): Boolean {
        if (!hasNotificationPermission()) {
            return false
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val costUsd = currentGasGwei * 0.000000001 * 45000 * 3420
        val title = "⚠️ Base Gas Spike: ${String.format("%.4f", currentGasGwei)} Gwei"
        val message = "Gas price exceeded your threshold of ${String.format("%.4f", thresholdGwei)} Gwei on $networkName (Est: $${String.format("%.4f", costUsd)} USD per transfer)."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$message\n\nTip: You can delay non-urgent transactions or leverage Base Flashblocks and Paymaster-sponsored routes.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 250, 150, 250))

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
            true
        } catch (e: SecurityException) {
            false
        }
    }
}

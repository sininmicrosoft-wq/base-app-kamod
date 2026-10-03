package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.VibenetFaucetResult
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseTeal
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Composable that allows users to request testnet ETH from the Vibenet faucet
 * by entering their wallet address and calling the official faucet API endpoint:
 * POST https://api.vibes.base.org/api/vibenet/faucet/drip
 */
@Composable
fun VibenetFaucetCard(
    onRequestFaucet: suspend (String) -> VibenetFaucetResult,
    isLoading: Boolean = false,
    defaultAddress: String = "0x71C84539820f4E71B20f92418a901f4c7849a290",
    history: List<VibenetFaucetResult> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var walletAddress by remember { mutableStateOf(defaultAddress) }
    var latestReceipt by remember { mutableStateOf<VibenetFaucetResult?>(null) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    val isValidAddress = remember(walletAddress) {
        val trimmed = walletAddress.trim()
        trimmed.startsWith("0x") && trimmed.length == 42 && trimmed.drop(2).all { it in "0123456789abcdefABCDEF" }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vibenet_faucet_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, BaseCyan.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Faucet Icon + Title + API Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BaseCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Faucet",
                            tint = BaseCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Vibenet Testnet Faucet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Drip 0.5 ETH via api.vibes.base.org",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BasePurple.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, BasePurple.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "POST /api/vibenet/faucet/drip",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = BasePurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wallet Address Input Field
            OutlinedTextField(
                value = walletAddress,
                onValueChange = { walletAddress = it.trim() },
                label = { Text("Target EVM Wallet Address") },
                placeholder = { Text("0x...") },
                singleLine = true,
                isError = walletAddress.isNotBlank() && !isValidAddress,
                supportingText = {
                    if (walletAddress.isNotBlank() && !isValidAddress) {
                        Text("Address must be 42 characters starting with 0x", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    } else {
                        Text("Enter EVM address to receive 0.5 testnet ETH on Base Vibenet", fontSize = 11.sp)
                    }
                },
                trailingIcon = {
                    if (walletAddress != defaultAddress) {
                        TextButton(onClick = { walletAddress = defaultAddress }) {
                            Text("Reset", fontSize = 10.sp, color = BaseCyan)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("faucet_address_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BaseCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Quick Autofill Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { walletAddress = defaultAddress }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Autofill Connected Smart Wallet (${defaultAddress.take(6)}...${defaultAddress.takeLast(4)})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = BaseCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Drip Button calling the API endpoint
            Button(
                onClick = {
                    coroutineScope.launch {
                        val result = onRequestFaucet(walletAddress)
                        latestReceipt = result
                        toastMessage = "Drip request sent to Vibenet sequencer!"
                    }
                },
                enabled = !isLoading && isValidAddress,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BaseCyan,
                    disabledContainerColor = BaseCyan.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("request_faucet_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Calling POST /faucet/drip...",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Request 0.5 Vibenet ETH from Faucet",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            // Success Confirmation Card
            AnimatedVisibility(visible = latestReceipt != null) {
                latestReceipt?.let { receipt ->
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BaseTeal.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BaseTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().testTag("faucet_receipt_card")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Faucet Drip Confirmed (+${receipt.amountEth} ETH)",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BaseTeal
                                    )
                                }

                                Surface(shape = RoundedCornerShape(4.dp), color = BaseTeal.copy(alpha = 0.25f)) {
                                    Text(
                                        text = "CANONICAL 200MS",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BaseTeal,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Recipient: ${receipt.recipientAddress}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tx Hash: ${receipt.txHash}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(receipt.txHash))
                                        toastMessage = "Copied transaction hash!"
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Tx", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = {
                                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(receipt.explorerUrl))
                                        context.startActivity(browserIntent)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BaseTeal),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Explorer", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Developer cURL Snippet Terminal
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Terminal, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Developer API Reference (cURL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            val curlCommand = """curl -X POST https://api.vibes.base.org/api/vibenet/faucet/drip \
  -H "content-type: application/json" \
  -d '{"address":"$walletAddress"}'"""

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF07090E),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = curlCommand,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BaseCyan,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(curlCommand))
                            toastMessage = "Copied cURL command!"
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }

            // Notification / Toast Banner
            toastMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BaseCyan.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BaseCyan,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // History Log (if any requests have been made)
            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Recent Faucet Requests (${history.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    history.take(3).forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${item.recipientAddress.take(8)}...${item.recipientAddress.takeLast(6)}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "+${item.amountEth} ETH • ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(item.timestamp))}",
                                        fontSize = 10.sp,
                                        color = BaseTeal
                                    )
                                }
                                Text(
                                    text = item.txHash.take(12) + "...",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

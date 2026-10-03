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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.remote.WalletEthBalanceResult
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseTeal
import java.text.NumberFormat
import java.util.Locale

/**
 * Composable that fetches and displays the native ETH balance for a provided
 * wallet address on Base using the Retrofit RPC client.
 */
@Composable
fun BaseWalletBalanceCard(
    onFetchBalance: (address: String, endpointUrl: String?) -> Unit,
    balanceResult: WalletEthBalanceResult?,
    isLoading: Boolean,
    defaultAddress: String = "0x71C84539820f4E71B20f92418a901f4c7849a290",
    currentRpcUrl: String = "https://mainnet.base.org",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 6 } }

    var walletAddressInput by remember { mutableStateOf(defaultAddress) }
    var copiedNotice by remember { mutableStateOf<String?>(null) }
    var showDevDetails by remember { mutableStateOf(false) }

    val isValidAddress = remember(walletAddressInput) {
        val trimmed = walletAddressInput.trim()
        trimmed.startsWith("0x") && trimmed.length == 42 && trimmed.drop(2).all { it in "0123456789abcdefABCDEF" }
    }

    // Trigger initial fetch when composable appears
    LaunchedEffect(Unit) {
        if (balanceResult == null && isValidAddress) {
            onFetchBalance(walletAddressInput.trim(), currentRpcUrl)
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, BaseCyan.copy(alpha = 0.5f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("base_wallet_balance_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(BaseCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = "Wallet Balance",
                            tint = BaseCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Native ETH Balance",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "JSON-RPC via Retrofit • eth_getBalance",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BaseTeal.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, BaseTeal.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "RETROFIT RPC",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wallet Address Input Field
            OutlinedTextField(
                value = walletAddressInput,
                onValueChange = { walletAddressInput = it },
                label = { Text("Base EVM Wallet Address") },
                placeholder = { Text("0x...") },
                singleLine = true,
                isError = walletAddressInput.isNotBlank() && !isValidAddress,
                supportingText = {
                    if (walletAddressInput.isNotBlank() && !isValidAddress) {
                        Text("Address must be 42 characters starting with 0x", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                    } else {
                        Text("Target RPC: $currentRpcUrl", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("eth_balance_address_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BaseCyan,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Preset Quick Fill Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            walletAddressInput = "0x71C84539820f4E71B20f92418a901f4c7849a290"
                            onFetchBalance("0x71C84539820f4E71B20f92418a901f4c7849a290", currentRpcUrl)
                        }
                        .testTag("chip_smart_wallet")
                ) {
                    Text(
                        text = "Smart Wallet (0x71C8)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BaseCyan,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            walletAddressInput = "0x503828976D22510aad0201ac7EC88293211A23Da"
                            onFetchBalance("0x503828976D22510aad0201ac7EC88293211A23Da", currentRpcUrl)
                        }
                        .testTag("chip_coinbase_custody")
                ) {
                    Text(
                        text = "Coinbase Custody (0x5038)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BasePurple,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fetch Button
            Button(
                onClick = {
                    if (isValidAddress) {
                        onFetchBalance(walletAddressInput.trim(), currentRpcUrl)
                    }
                },
                enabled = !isLoading && isValidAddress,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("fetch_eth_balance_btn")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Calling eth_getBalance via Retrofit...", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                } else {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Query Native ETH Balance", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            // Toast feedback
            copiedNotice?.let { notice ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BaseTeal.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(notice, fontSize = 11.sp, color = BaseTeal, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Results Display Card
            balanceResult?.let { result ->
                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("eth_balance_result_panel")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NATIVE BASE BALANCE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8),
                                letterSpacing = 0.5.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BaseTeal.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "${result.latencyMs} ms",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = BaseTeal,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Large ETH Balance
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = numberFormat.format(result.balanceEth),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ETH",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }

                            Text(
                                text = "≈ ${currencyFormat.format(result.balanceUsd)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = BaseTeal,
                                modifier = Modifier.padding(bottom = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Detailed Units breakdown (Wei, Gwei)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF030712),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Wei:", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = result.balanceWei,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFCBD5E1)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Endpoint:", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = result.rpcEndpoint,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = BaseCyan
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Copy & Explorer Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("${result.balanceEth} ETH"))
                                    copiedNotice = "Copied balance (${result.balanceEth} ETH)!"
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy ETH", fontSize = 10.sp)
                            }

                            Button(
                                onClick = {
                                    val isVibenet = result.rpcEndpoint.contains("vibes.base.org")
                                    val baseUrl = if (isVibenet) {
                                        "https://chain.base.org/vibenet/explorer"
                                    } else {
                                        "https://basescan.org/address/${result.address}"
                                    }
                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(baseUrl))
                                    context.startActivity(browserIntent)
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("View on Explorer", fontSize = 10.sp, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Developer Accordion: View JSON-RPC Spec & cURL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showDevDetails = !showDevDetails }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Developer JSON-RPC Spec & cURL", fontSize = 11.sp, color = BaseCyan, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    imageVector = if (showDevDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = showDevDetails) {
                val curlSnippet = """
curl -X POST $currentRpcUrl \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","method":"eth_getBalance","params":["$walletAddressInput","latest"],"id":1}'
                """.trimIndent()

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF030712),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = curlSnippet,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BaseCyan,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(curlSnippet))
                                copiedNotice = "Copied cURL snippet!"
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(13.dp))
                        }
                    }
                }
            }
        }
    }
}

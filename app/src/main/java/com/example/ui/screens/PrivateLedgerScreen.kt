package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.BaseViewModel
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseTeal

@Composable
fun PrivateLedgerScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val privateTxs by viewModel.privateTxs.collectAsStateWithLifecycle()
    var showSendDialog by remember { mutableStateOf(false) }
    var selectedViewingKey by remember { mutableStateOf<String?>(null) }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Scaffold(
        modifier = modifier.testTag("private_ledger_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSendDialog = true },
                containerColor = BasePurple,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("send_private_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Shielded Transfer")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Shielded Transfer", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("private_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = "Private Transactions",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Base Ledgers • Confidential Settlement & zk-SNARKs",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Shielded Balance Overview
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BasePurple.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(BasePurple.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = BasePurple, modifier = Modifier.size(18.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Base Ledgers Shielded Pool", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BasePurple.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "ENCRYPTED ONCHAIN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BasePurple,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "$12,450.00 USDC",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Shielded balance hidden from public block explorers",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Confidential Activity (${privateTxs.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(privateTxs) { tx ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().testTag("private_tx_${tx.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Stealth Transfer: ${tx.stealthRecipient.take(16)}...",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "Amount: ${tx.amount} ${tx.token} (Confidential)",
                                    fontSize = 12.sp,
                                    color = BasePurple,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(BaseTeal.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SETTLED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "ZK-Proof: ${tx.zkProofHash.take(24)}... (generated in ${tx.proofGenerationMs}ms)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { selectedViewingKey = tx.auditViewingKey },
                                modifier = Modifier.testTag("viewing_key_btn_${tx.id}")
                            ) {
                                Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Audit Viewing Key", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showSendDialog) {
        SendPrivateDialog(
            onDismiss = { showSendDialog = false },
            onSend = { stealthAddr, amt, token ->
                viewModel.executePrivateTransfer(stealthAddr, amt, token)
                showSendDialog = false
            }
        )
    }

    selectedViewingKey?.let { key ->
        AlertDialog(
            onDismissRequest = { selectedViewingKey = null },
            title = { Text("Regulatory Audit Viewing Key", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "This cryptographic key grants read-only permission to decode this confidential settlement for tax and regulatory compliance.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = key,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = BaseCyan,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(key))
                        selectedViewingKey = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                ) {
                    Text("Copy Key")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedViewingKey = null }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SendPrivateDialog(
    onDismiss: () -> Unit,
    onSend: (stealth: String, amount: Double, token: String) -> Unit
) {
    var stealthAddr by remember { mutableStateOf("stealth_0x" + (1..32).map { "0123456789abcdef".random() }.joinToString("")) }
    var amountText by remember { mutableStateOf("150.0") }
    var token by remember { mutableStateOf("USDC") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Base Ledgers Confidential Transfer", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "A one-time ephemeral stealth address and zero-knowledge proof will be generated for confidential onchain settlement.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = stealthAddr,
                    onValueChange = { stealthAddr = it },
                    label = { Text("Stealth Recipient Address") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("stealth_addr_input")
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Shielded Amount ($token)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shielded_amount_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 50.0
                    onSend(stealthAddr, amt, token)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BasePurple),
                modifier = Modifier.testTag("submit_private_transfer_btn")
            ) {
                Text("Generate Proof & Settle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

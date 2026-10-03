package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.model.B20Asset
import com.example.ui.BaseViewModel
import com.example.ui.components.B20AssetDirectoryView
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseTeal

@Composable
fun RwaScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val assets by viewModel.assets.collectAsStateWithLifecycle()
    val isFetchingAssets by viewModel.isFetchingB20Assets.collectAsStateWithLifecycle()
    val network by viewModel.selectedNetwork.collectAsStateWithLifecycle()
    var showIssueDialog by remember { mutableStateOf(false) }
    var distributionSuccessMessage by remember { mutableStateOf<String?>(null) }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val totalValuation = assets.sumOf { it.totalValuationUsd }
    val totalDistributed = assets.sumOf { it.totalDistributedUsd }

    Scaffold(
        modifier = modifier.testTag("rwa_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showIssueDialog = true },
                containerColor = BaseAmber,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("issue_asset_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Issue B20")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Issue B20 Asset", fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("rwa_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = "Tokenize Assets (RWA)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Base B20 Asset Standard • Holder Controls & Yield",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Overview Metric Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Total RWA Market Cap", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$${String.format("%,.0f", totalValuation)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BaseAmber)
                            Text(text = "${assets.size} B20 Assets deployed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "USDC Yield Distributed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "$${String.format("%,.0f", totalDistributed)}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                            Text(text = "Automated on Base", fontSize = 11.sp, color = BaseTeal)
                        }
                    }
                }
            }

            distributionSuccessMessage?.let { msg ->
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BaseTeal.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BaseTeal.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth().testTag("distribution_msg")
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, fontSize = 12.sp, color = BaseTeal, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            item {
                B20AssetDirectoryView(
                    assets = assets,
                    isRefreshing = isFetchingAssets,
                    onRefresh = { viewModel.refreshB20Assets() },
                    networkName = network.displayName,
                    onAssetClick = { asset ->
                        val monthlyYield = (asset.totalValuationUsd * (asset.dividendYieldPct / 100.0)) / 12.0
                        viewModel.distributeDividends(asset, monthlyYield)
                        distributionSuccessMessage = "Distributed $${String.format("%,.2f", monthlyYield)} USDC dividend to ${asset.symbol} holders!"
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showIssueDialog) {
        IssueB20Dialog(
            onDismiss = { showIssueDialog = false },
            onIssue = { name, symbol, cat, valUsd, supply, yieldPct, cap, holders ->
                viewModel.issueB20Asset(name, symbol, cat, valUsd, supply, yieldPct, cap, holders)
                showIssueDialog = false
            }
        )
    }
}

@Composable
fun B20AssetCard(
    asset: B20Asset,
    onDistribute: () -> Unit,
    onCopyContract: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().testTag("b20_card_${asset.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = asset.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "${asset.symbol} • ${asset.assetCategory}", fontSize = 12.sp, color = BaseAmber, fontWeight = FontWeight.SemiBold)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BaseAmber.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "B20 Asset",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseAmber,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Valuation", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%,.0f", asset.totalValuationUsd)}", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Column {
                    Text("Token Price", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%.2f", asset.pricePerToken)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Dividend Yield", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${asset.dividendYieldPct}% APY", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Holder Controls & Compliance Badges
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Holder Controls & Compliance (Base Standard):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ComplianceBadge(label = "KYC Whitelist", active = asset.kycRequired)
                        ComplianceBadge(label = "Transfer Restricted", active = asset.transferRestricted)
                        ComplianceBadge(label = "Securitize Prime", active = true)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Contract: ${asset.contractAddress.take(12)}...",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = onCopyContract, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                    }
                }

                Button(
                    onClick = onDistribute,
                    colors = ButtonDefaults.buttonColors(containerColor = BaseTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("distribute_btn_${asset.id}")
                ) {
                    Icon(imageVector = Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Distribute Yield", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ComplianceBadge(label: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (active) BaseTeal.copy(alpha = 0.15f) else Color(0xFF334155),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, if (active) BaseTeal.copy(alpha = 0.4f) else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (active) Icons.Default.CheckCircle else Icons.Default.Lock,
                contentDescription = null,
                tint = if (active) BaseTeal else Color.LightGray,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = if (active) BaseTeal else Color.LightGray)
        }
    }
}

@Composable
fun IssueB20Dialog(
    onDismiss: () -> Unit,
    onIssue: (name: String, symbol: String, category: String, valUsd: Double, supply: Long, yieldPct: Double, supplyCap: Long, holderCount: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Treasury Bills") }
    var valuationText by remember { mutableStateOf("10000000") }
    var supplyText by remember { mutableStateOf("100000") }
    var supplyCapText by remember { mutableStateOf("250000") }
    var holderCountText by remember { mutableStateOf("142") }
    var yieldText by remember { mutableStateOf("5.25") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Issue New B20 Asset", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (e.g. Austin Commercial RE)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_b20_name")
                )
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("Ticker Symbol (e.g. ACRE, USTB)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_b20_symbol")
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = supplyText,
                        onValueChange = { supplyText = it },
                        label = { Text("Initial Mint Supply") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_b20_supply")
                    )
                    OutlinedTextField(
                        value = supplyCapText,
                        onValueChange = { supplyCapText = it },
                        label = { Text("Max Supply Cap") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_b20_cap")
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = valuationText,
                        onValueChange = { valuationText = it },
                        label = { Text("Valuation ($)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_b20_valuation")
                    )
                    OutlinedTextField(
                        value = holderCountText,
                        onValueChange = { holderCountText = it },
                        label = { Text("Initial Holders") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("input_b20_holders")
                    )
                }
                OutlinedTextField(
                    value = yieldText,
                    onValueChange = { yieldText = it },
                    label = { Text("Dividend Yield (% APY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_b20_yield")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val valUsd = valuationText.toDoubleOrNull() ?: 1000000.0
                    val supply = supplyText.toLongOrNull() ?: 10000L
                    val cap = supplyCapText.toLongOrNull() ?: (supply * 2)
                    val holders = holderCountText.toIntOrNull() ?: 1
                    val yield = yieldText.toDoubleOrNull() ?: 5.0
                    onIssue(
                        name.ifEmpty { "Tokenized Real Asset" },
                        symbol.ifEmpty { "RWA" },
                        category,
                        valUsd,
                        supply,
                        yield,
                        cap.coerceAtLeast(supply),
                        holders
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = BaseAmber),
                modifier = Modifier.testTag("submit_issue_b20_btn")
            ) {
                Text("Deploy B20 Asset", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

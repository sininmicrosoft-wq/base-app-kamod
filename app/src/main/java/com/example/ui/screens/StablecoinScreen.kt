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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Stablecoin
import com.example.ui.BaseViewModel
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseRed
import com.example.ui.theme.BaseTeal

@Composable
fun StablecoinScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stablecoins by viewModel.stablecoins.collectAsStateWithLifecycle()
    var showDeployDialog by remember { mutableStateOf(false) }
    var activeMintBurnCoin by remember { mutableStateOf<Pair<Stablecoin, Boolean>?>(null) }

    Scaffold(
        modifier = modifier.testTag("stablecoins_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDeployDialog = true },
                containerColor = BaseBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("deploy_stablecoin_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Deploy")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Deploy Stablecoin", fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("stablecoin_back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = "Issue Stablecoins",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Fiat-Backed Tokens • Proof of Reserve • Mint & Burn",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Proof of Reserve Attestation Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseTeal.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("proof_of_reserves_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Proof of Reserves (Chainlink PoR)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BaseTeal.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "VERIFIED 100.2%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseTeal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Every minted stablecoin is audited by cryptographic Proof-of-Reserve feeds on Base, backed by FDIC-insured cash deposits & 30-day US T-bills.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Active Stablecoins (${stablecoins.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            items(stablecoins) { coin ->
                StablecoinCard(
                    coin = coin,
                    onMint = { activeMintBurnCoin = coin to true },
                    onBurn = { activeMintBurnCoin = coin to false },
                    onTogglePause = { viewModel.toggleStablecoinPause(coin) }
                )
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    if (showDeployDialog) {
        DeployStablecoinDialog(
            onDismiss = { showDeployDialog = false },
            onDeploy = { name, symbol, peg, initialSupply ->
                viewModel.issueStablecoin(name, symbol, peg, initialSupply)
                showDeployDialog = false
            }
        )
    }

    activeMintBurnCoin?.let { (coin, isMint) ->
        MintBurnDialog(
            coin = coin,
            isMint = isMint,
            onDismiss = { activeMintBurnCoin = null },
            onConfirm = { amount ->
                viewModel.mintOrBurnStablecoin(coin, amount, isMint)
                activeMintBurnCoin = null
            }
        )
    }
}

@Composable
fun StablecoinCard(
    coin: Stablecoin,
    onMint: () -> Unit,
    onBurn: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().testTag("stablecoin_card_${coin.id}")
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BaseBlue.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(coin.symbol.take(2), fontWeight = FontWeight.Bold, color = BaseCyan, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = coin.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Peg: 1 ${coin.symbol} = 1.00 ${coin.pegCurrency}", fontSize = 11.sp, color = BaseCyan)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (coin.isPaused) BaseRed.copy(alpha = 0.15f) else BaseTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (coin.isPaused) "PAUSED" else "ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (coin.isPaused) BaseRed else BaseTeal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Minted Supply", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${String.format("%,.0f", coin.totalSupply)} ${coin.symbol}", fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Fiat Reserve Backing", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$${String.format("%,.0f", coin.fiatReserves)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Custodian: ${coin.reserveCustodian}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mint / Burn / Pause Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMint,
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("mint_btn_${coin.id}")
                ) {
                    Icon(imageVector = Icons.Default.Paid, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mint", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onBurn,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).testTag("burn_btn_${coin.id}")
                ) {
                    Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = BaseAmber, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Burn", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onTogglePause,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("pause_btn_${coin.id}")
                ) {
                    Icon(
                        imageVector = if (coin.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = if (coin.isPaused) BaseTeal else BaseRed,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DeployStablecoinDialog(
    onDismiss: () -> Unit,
    onDeploy: (name: String, symbol: String, peg: String, initialSupply: Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var symbol by remember { mutableStateOf("") }
    var peg by remember { mutableStateOf("USD") }
    var supplyText by remember { mutableStateOf("1000000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Deploy Fiat-Backed Stablecoin", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. Base Dollar)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_sc_name")
                )
                OutlinedTextField(
                    value = symbol,
                    onValueChange = { symbol = it },
                    label = { Text("Symbol (e.g. bUSD, bEUR)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_sc_symbol")
                )
                OutlinedTextField(
                    value = supplyText,
                    onValueChange = { supplyText = it },
                    label = { Text("Initial Mint Supply") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_sc_supply")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = supplyText.toDoubleOrNull() ?: 500000.0
                    onDeploy(name.ifEmpty { "Base USD" }, symbol.ifEmpty { "bUSD" }, peg, s)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                modifier = Modifier.testTag("submit_deploy_sc_btn")
            ) {
                Text("Deploy on Base")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun MintBurnDialog(
    coin: Stablecoin,
    isMint: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountText by remember { mutableStateOf("50000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isMint) "Mint ${coin.symbol}" else "Burn ${coin.symbol}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = if (isMint)
                        "Mint additional ${coin.symbol} against verified wire transfer in reserve custody."
                    else
                        "Burn ${coin.symbol} from total supply for redemption and wire fiat to customer.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${coin.symbol})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("mint_burn_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 10000.0
                    onConfirm(amt)
                },
                colors = ButtonDefaults.buttonColors(containerColor = if (isMint) BaseBlue else BaseAmber),
                modifier = Modifier.testTag("confirm_mint_burn_btn")
            ) {
                Text(if (isMint) "Confirm Mint" else "Confirm Burn")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

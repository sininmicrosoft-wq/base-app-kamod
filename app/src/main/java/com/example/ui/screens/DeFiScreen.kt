package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.LendingMarket
import com.example.data.model.YieldVault
import com.example.ui.BaseViewModel
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseTeal

@Composable
fun DeFiScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tokens by viewModel.tokens.collectAsStateWithLifecycle()
    val lendingMarkets by viewModel.lendingMarkets.collectAsStateWithLifecycle()
    val yieldVaults by viewModel.yieldVaults.collectAsStateWithLifecycle()

    var swapSuccessBanner by remember { mutableStateOf<String?>(null) }
    var supplyModalMarket by remember { mutableStateOf<LendingMarket?>(null) }
    var depositModalVault by remember { mutableStateOf<YieldVault?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("defi_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("defi_back_btn")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    text = "Integrate DeFi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Aerodrome AMM, Aave v3 Base & Curated Vaults",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BaseBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = BaseBlue
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Trading (Swap)", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_swap")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Lend & Borrow", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_lending")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Earn Vaults", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("tab_vaults")
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // SWAP / TRADING TAB
                    item {
                        SwapView(
                            tokens = tokens,
                            onSwap = { fromToken, toToken, fromAmt, toAmt ->
                                viewModel.executeSwap(fromToken, toToken, fromAmt, toAmt)
                                swapSuccessBanner = "Swapped $fromAmt $fromToken for $toAmt $toToken on Aerodrome AMM!"
                            }
                        )
                    }

                    swapSuccessBanner?.let { msg ->
                        item {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = BaseTeal.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BaseTeal.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth().testTag("swap_success_banner")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = msg, fontSize = 12.sp, color = BaseTeal, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // LENDING & BORROWING TAB
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = "Borrow Health Factor", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(text = "2.45 Safe", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BaseTeal.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("No Liquidation Risk", fontSize = 11.sp, color = BaseTeal, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Slider(
                                    value = 0.35f,
                                    onValueChange = {},
                                    colors = SliderDefaults.colors(thumbColor = BaseTeal, activeTrackColor = BaseTeal),
                                    enabled = false
                                )
                                Text(
                                    text = "Collateralized on Aave v3 Base markets with sub-cent gas fees.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Aave v3 Base Markets",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(lendingMarkets) { market ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().testTag("market_card_${market.token}")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
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
                                                .background(BaseBlue.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(market.token.take(2), fontWeight = FontWeight.Bold, color = BaseCyan, fontSize = 12.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = market.token, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text(text = market.protocol, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "${market.supplyApy}% APY", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                                        Text(text = "Borrow: ${market.borrowApy}%", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Your Supplied: ${market.userSupplied} ${market.token}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Button(
                                        onClick = { supplyModalMarket = market },
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("supply_btn_${market.token}")
                                    ) {
                                        Text("Supply", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // EARN VAULTS TAB
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Base Vault-Based Earn Products",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Non-custodial smart contract yield aggregators (Morpho Blue, Yearn) native to Base network.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }

                    items(yieldVaults) { vault ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth().testTag("vault_card_${vault.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = vault.name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "Protocol: ${vault.protocol} • TVL: ${vault.tvlUsd}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "${vault.apyPct}%", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BaseTeal)
                                        Text(text = "Net APY", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Deposited: ${vault.userDeposited} ${vault.underlyingAsset}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Button(
                                        onClick = { depositModalVault = vault },
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("deposit_vault_btn_${vault.id}")
                                    ) {
                                        Text("Deposit", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // Supply Modal
    supplyModalMarket?.let { market ->
        var amountInput by remember { mutableStateOf("100") }
        AlertDialog(
            onDismissRequest = { supplyModalMarket = null },
            title = { Text("Supply to ${market.token} Pool", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Supply ${market.token} on Base to earn ${market.supplyApy}% variable APY.")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("supply_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull() ?: 50.0
                        viewModel.supplyToLending(market.token, amt)
                        supplyModalMarket = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    modifier = Modifier.testTag("confirm_supply_btn")
                ) {
                    Text("Confirm Supply")
                }
            },
            dismissButton = {
                TextButton(onClick = { supplyModalMarket = null }) { Text("Cancel") }
            }
        )
    }

    // Deposit Vault Modal
    depositModalVault?.let { vault ->
        var depositInput by remember { mutableStateOf("250") }
        AlertDialog(
            onDismissRequest = { depositModalVault = null },
            title = { Text("Deposit to ${vault.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Deposit ${vault.underlyingAsset} to compound yield automatically at ${vault.apyPct}% APY.")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = depositInput,
                        onValueChange = { depositInput = it },
                        label = { Text("Amount (${vault.underlyingAsset})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("vault_deposit_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = depositInput.toDoubleOrNull() ?: 100.0
                        viewModel.depositToVault(vault.id, amt)
                        depositModalVault = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                    modifier = Modifier.testTag("confirm_deposit_vault_btn")
                ) {
                    Text("Deposit & Compound")
                }
            },
            dismissButton = {
                TextButton(onClick = { depositModalVault = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun SwapView(
    tokens: List<com.example.data.model.Token>,
    onSwap: (fromToken: String, toToken: String, fromAmt: Double, toAmt: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var fromSymbol by remember { mutableStateOf("ETH") }
    var toSymbol by remember { mutableStateOf("USDC") }
    var fromAmountText by remember { mutableStateOf("0.25") }

    val fromToken = tokens.find { it.symbol == fromSymbol } ?: tokens.first()
    val toToken = tokens.find { it.symbol == toSymbol } ?: tokens.last()

    val fromAmount = fromAmountText.toDoubleOrNull() ?: 0.0
    val toAmount = if (toToken.priceUsd > 0) (fromAmount * fromToken.priceUsd) / toToken.priceUsd else 0.0

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().testTag("swap_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Swap Tokens on Base", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BaseTeal.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "Aerodrome AMM v2",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseTeal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pay Input
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("You Pay", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Balance: ${String.format("%.3f", fromToken.balance)} ${fromToken.symbol}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = fromAmountText,
                            onValueChange = { fromAmountText = it },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("swap_from_amount_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TokenPill(symbol = fromSymbol, onClick = {
                            fromSymbol = if (fromSymbol == "ETH") "USDC" else "ETH"
                        })
                    }
                }
            }

            // Invert Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        val temp = fromSymbol
                        fromSymbol = toSymbol
                        toSymbol = temp
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("invert_swap_btn")
                ) {
                    Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = "Invert", tint = BaseCyan, modifier = Modifier.size(18.dp))
                }
            }

            // Receive Output
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("You Receive (Estimated)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1 $fromSymbol ≈ ${String.format("%.2f", if (toToken.priceUsd > 0) fromToken.priceUsd / toToken.priceUsd else 0.0)} $toSymbol", fontSize = 11.sp, color = BaseCyan)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = String.format("%.4f", toAmount),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        TokenPill(symbol = toSymbol, onClick = {
                            toSymbol = if (toSymbol == "USDC") "cbETH" else if (toSymbol == "cbETH") "AERO" else "USDC"
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Transaction detail breakdown
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Gas Fee", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$0.00 (Paymaster Sponsored)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Price Impact", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("< 0.01%", fontSize = 11.sp, color = BaseTeal)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Execution Speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("~185ms (Flashblocks)", fontSize = 11.sp, color = BaseCyan)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSwap(fromSymbol, toSymbol, fromAmount, toAmount) },
                enabled = fromAmount > 0 && fromAmount <= fromToken.balance,
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("execute_swap_btn")
            ) {
                Text(
                    text = if (fromAmount > fromToken.balance) "Insufficient $fromSymbol Balance" else "Execute Swap on Base",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun TokenPill(symbol: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(symbol, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

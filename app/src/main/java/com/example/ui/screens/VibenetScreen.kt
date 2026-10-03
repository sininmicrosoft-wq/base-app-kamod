package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Token
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.BaseNetwork
import com.example.ui.BaseViewModel
import com.example.ui.components.VibenetFaucetCard
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseTeal
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

data class VibenetDeployedToken(
    val name: String,
    val symbol: String,
    val contractAddress: String,
    val supplyCap: Long,
    val policyGated: Boolean,
    val memosEnabled: Boolean,
    val txHash: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class WsEventLog(
    val id: String,
    val method: String,
    val blockNumber: Long,
    val latencyMs: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun VibenetScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val selectedNetwork by viewModel.selectedNetwork.collectAsStateWithLifecycle()
    val isFaucetLoading by viewModel.isFaucetLoading.collectAsStateWithLifecycle()
    val faucetHistory by viewModel.faucetHistory.collectAsStateWithLifecycle()

    var activeTab by remember { mutableIntStateOf(0) }
    var copiedBanner by remember { mutableStateOf<String?>(null) }

    // 200ms Native Block Stream State
    var isStreaming200ms by remember { mutableStateOf(true) }
    var currentVibenetBlock by remember { mutableLongStateOf(1048590L) }
    var blocksStreamedCount by remember { mutableIntStateOf(0) }
    val recentBlocks = remember { mutableStateListOf<Long>() }

    // 200ms block ticking loop
    LaunchedEffect(isStreaming200ms) {
        if (!isStreaming200ms) return@LaunchedEffect
        while (isActive) {
            delay(200L) // Exact 200ms Vibenet block cadence (5 blocks per second)
            currentVibenetBlock++
            blocksStreamedCount++
            recentBlocks.add(0, currentVibenetBlock)
            if (recentBlocks.size > 8) {
                recentBlocks.removeLast()
            }
        }
    }

    // Faucet state
    val defaultAddress = "0x71C84539820f4E71B20f92418a901f4c7849a290"
    var faucetRecipient by remember { mutableStateOf(defaultAddress) }
    var isFaucetDripping by remember { mutableStateOf(false) }
    var faucetSuccessReceipt by remember { mutableStateOf<String?>(null) }

    // B20 Token Standard Lab State
    var tokenName by remember { mutableStateOf("Vibenet Commercial Paper") }
    var tokenSymbol by remember { mutableStateOf("vCP-USD") }
    var tokenSupplyCap by remember { mutableStateOf("10,000,000") }
    var policyGatingEnabled by remember { mutableStateOf(true) }
    var transferMemosEnabled by remember { mutableStateOf(true) }
    var isDeployingB20 by remember { mutableStateOf(false) }
    val deployedTokens = remember {
        mutableStateListOf(
            VibenetDeployedToken(
                name = "Vibenet Yield Vault",
                symbol = "vYLD",
                contractAddress = "0x84532026B20de018991204892c0192830182b20",
                supplyCap = 50000000,
                policyGated = true,
                memosEnabled = true,
                txHash = "0x9812afc0...4821"
            )
        )
    }

    // WebSocket Log Stream State
    val wsEventLogs = remember { mutableStateListOf<WsEventLog>() }
    LaunchedEffect(isStreaming200ms) {
        if (!isStreaming200ms) return@LaunchedEffect
        while (isActive) {
            delay(200L)
            val log = WsEventLog(
                id = "sub-${System.nanoTime() % 100000}",
                method = "eth_subscription (newHeads)",
                blockNumber = currentVibenetBlock,
                latencyMs = (180..220).random().toLong()
            )
            wsEventLogs.add(0, log)
            if (wsEventLogs.size > 10) {
                wsEventLogs.removeLast()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("vibenet_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("vibenet_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Test on Vibenet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = BasePurple.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "DEVNET PREVIEW",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BasePurple,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "Base Experimental Preview • 200ms Blocks & B20 Standard",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Network Switch to Vibenet if not currently selected
            if (selectedNetwork != BaseNetwork.VIBENET) {
                Button(
                    onClick = { viewModel.setNetwork(BaseNetwork.VIBENET) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BasePurple),
                    modifier = Modifier.testTag("switch_to_vibenet_btn")
                ) {
                    Text("Switch to Vibenet", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        // Navigation Tabs
        val tabs = listOf("Network Specs", "200ms Blocks", "B20 Tokens", "ETH Faucet", "WebSocket RPC")
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = BasePurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                    color = BasePurple
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (activeTab == index) BasePurple else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Experimental Warning Callout
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BaseAmber.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseAmber.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("vibenet_warning_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BaseAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Vibenet is Base's preview network for experimentation only. It is not intended for production or user-facing apps, and state may be reset without notice.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            when (activeTab) {
                0 -> {
                    // TAB 0: Network Details & Quick Links
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BasePurple.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth().testTag("vibenet_specs_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Base Vibenet Network Details",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Track latest features live at chain.base.org/vibenet",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                val specs = listOf(
                                    "Network Name" to "Base Vibenet",
                                    "RPC Endpoint" to "https://rpc.vibes.base.org",
                                    "WebSocket" to "wss://rpc.vibes.base.org/ws",
                                    "Chain ID" to "84538453",
                                    "Currency Symbol" to "ETH",
                                    "Block Time" to "200ms Canonical Blocks",
                                    "Faucet Hub" to "chain.base.org/vibenet/faucet",
                                    "Block Explorer" to "chain.base.org/vibenet/explorer"
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    specs.forEach { (key, value) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant,
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = key, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = value,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (key.contains("Endpoint") || key.contains("WebSocket")) BaseCyan else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.ContentCopy,
                                                    contentDescription = "Copy $key",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier
                                                        .size(14.dp)
                                                        .clickable {
                                                            clipboardManager.setText(AnnotatedString(value))
                                                            copiedBanner = "Copied $key!"
                                                        }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Quick Explorer & Hub Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chain.base.org/vibenet"))
                                            context.startActivity(browserIntent)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Open Vibenet Hub", fontSize = 11.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://chain.base.org/vibenet/explorer"))
                                            context.startActivity(browserIntent)
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BasePurple),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.Hub, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Block Explorer", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: 200ms Native Blocks Visualizer
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, BaseCyan.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("vibenet_200ms_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "200ms Native Canonical Blocks",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "5 canonical blocks per second direct from sequencer",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    IconButton(
                                        onClick = { isStreaming200ms = !isStreaming200ms },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isStreaming200ms) Icons.Default.Stop else Icons.Default.PlayArrow,
                                            contentDescription = "Stream Control",
                                            tint = if (isStreaming200ms) BaseAmber else BaseTeal
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Live Block Counter Spotlight
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF07090E),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("LATEST CANONICAL VIBENET BLOCK", fontSize = 10.sp, color = BaseCyan, letterSpacing = 1.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "#${currentVibenetBlock}",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(BaseTeal))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "200ms block cadence • Streamed: $blocksStreamedCount blocks",
                                                fontSize = 11.sp,
                                                color = BaseTeal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Latency Comparison Visualizer
                                Text("Consensus Block Time Comparison", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                val comparison = listOf(
                                    Triple("Base Vibenet (Denim Upgrade)", "200ms (5.0 blocks/sec)", BaseTeal),
                                    Triple("Base Mainnet / Sepolia", "2,000ms (0.5 blocks/sec)", BaseBlue),
                                    Triple("Ethereum Layer 1", "12,000ms (0.08 blocks/sec)", MaterialTheme.colorScheme.onSurfaceVariant)
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    comparison.forEach { (networkName, speed, color) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(text = networkName, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                            }
                                            Text(text = speed, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = color, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Recent 200ms Block Strip
                                Text("Live Canonical Block Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    recentBlocks.take(4).forEach { block ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BaseCyan.copy(alpha = 0.15f),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("#$block", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                                Text("200ms", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 2: B20 Token Standard Lab
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, BasePurple.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("b20_token_lab_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Token, contentDescription = null, tint = BasePurple, modifier = Modifier.size(22.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "B20 Native Token Standard",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Built-in roles, supply caps, policy gating & transfer memos",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // B20 Token Standard Highlights
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("• Roles: Built-in Issuer, Compliance Admin, and Minter onchain", fontSize = 11.sp)
                                        Text("• Supply Caps: Hardcoded immutable or dynamic issuance boundaries", fontSize = 11.sp)
                                        Text("• Policy Gating: Automated investor accreditation & KYC enforcement", fontSize = 11.sp)
                                        Text("• Transfer Memos: Audit metadata permanently embedded in calldata", fontSize = 11.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // B20 Deployment Sandbox Form
                                Text("Deploy B20 Asset Token on Vibenet", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = tokenName,
                                    onValueChange = { tokenName = it },
                                    label = { Text("Token Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = BasePurple,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tokenSymbol,
                                        onValueChange = { tokenSymbol = it },
                                        label = { Text("Symbol") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BasePurple,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )

                                    OutlinedTextField(
                                        value = tokenSupplyCap,
                                        onValueChange = { tokenSupplyCap = it },
                                        label = { Text("Supply Cap") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BasePurple,
                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Policy Gating & Memos Switches
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Policy Gating (Accreditation)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Enforce onchain transfer restrictions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = policyGatingEnabled,
                                        onCheckedChange = { policyGatingEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BasePurple)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Built-in Transfer Memos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                        Text("Attach invoice or compliance reference", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Switch(
                                        checked = transferMemosEnabled,
                                        onCheckedChange = { transferMemosEnabled = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BasePurple)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        isDeployingB20 = true
                                    },
                                    enabled = !isDeployingB20 && tokenName.isNotBlank() && tokenSymbol.isNotBlank(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BasePurple),
                                    modifier = Modifier.fillMaxWidth().testTag("deploy_b20_btn")
                                ) {
                                    if (isDeployingB20) {
                                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Deploying B20 Token via Factory...", color = Color.White)
                                    } else {
                                        Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Launch B20 Token on Vibenet", fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }

                                // Deploy simulator effect
                                LaunchedEffect(isDeployingB20) {
                                    if (isDeployingB20) {
                                        delay(1200)
                                        val newContract = "0x8453" + (100000..999999).random() + "B20" + tokenSymbol.take(3).uppercase()
                                        deployedTokens.add(
                                            0,
                                            VibenetDeployedToken(
                                                name = tokenName,
                                                symbol = tokenSymbol,
                                                contractAddress = newContract,
                                                supplyCap = tokenSupplyCap.replace(",", "").toLongOrNull() ?: 1000000L,
                                                policyGated = policyGatingEnabled,
                                                memosEnabled = transferMemosEnabled,
                                                txHash = "0x" + (100000000..999999999).random() + "vibe"
                                            )
                                        )
                                        isDeployingB20 = false
                                    }
                                }

                                if (deployedTokens.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Deployed B20 Tokens (${deployedTokens.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        deployedTokens.forEach { token ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text("${token.name} (${token.symbol})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text("Cap: ${token.supplyCap}", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = BaseCyan)
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Contract: ${token.contractAddress}",
                                                        fontSize = 11.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        if (token.policyGated) {
                                                            Box(modifier = Modifier.background(BaseTeal.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                                Text("POLICY GATED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                                                            }
                                                        }
                                                        if (token.memosEnabled) {
                                                            Box(modifier = Modifier.background(BasePurple.copy(alpha = 0.2f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                                Text("MEMOS ENABLED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BasePurple)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // TAB 3: Get Testnet ETH Faucet Composable
                    item {
                        VibenetFaucetCard(
                            onRequestFaucet = { address -> viewModel.requestVibenetFaucet(address) },
                            isLoading = isFaucetLoading,
                            defaultAddress = defaultAddress,
                            history = faucetHistory
                        )
                    }
                }

                4 -> {
                    // TAB 4: WebSocket Streaming wss://rpc.vibes.base.org/ws
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.2.dp, BaseBlue.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth().testTag("vibenet_ws_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Stream With WebSockets",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "wss://rpc.vibes.base.org/ws • eth_subscribe ('newHeads', 'logs')",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "With 200ms blocks, standard 250-1000ms polling can miss multiple blocks. Use WebSocket push subscriptions as your primary source, keeping HTTPS RPC as fallback.",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp),
                                        lineHeight = 15.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Live WebSocket Event Terminal
                                Text("Live Push Subscriptions Feed", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF07090E),
                                    modifier = Modifier.fillMaxWidth().height(160.dp)
                                ) {
                                    LazyColumn(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(wsEventLogs) { log ->
                                            Text(
                                                text = "➔ [${log.id}] ${log.method} => block #${log.blockNumber} (latency: ${log.latencyMs}ms)",
                                                fontSize = 10.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = BaseTeal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Node.js WebSocket Code Snippet
                                Text("Node.js WebSocket Integration", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))

                                val wsSnippet = """
import WebSocket from "ws";

const ws = new WebSocket("wss://rpc.vibes.base.org/ws");

ws.on("open", () => {
  ws.send(JSON.stringify({
    jsonrpc: "2.0",
    id: 1,
    method: "eth_subscribe",
    params: ["newHeads"],
  }));
});

ws.on("message", (data) => {
  const msg = JSON.parse(data.toString());
  if (msg.method === "eth_subscription") {
    console.log("new head:", msg.params.result.number);
  }
});
                                """.trimIndent()

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(
                                            text = wsSnippet,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = BaseCyan,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(wsSnippet))
                                                copiedBanner = "Copied WebSocket snippet!"
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Copied Banner Toast
            copiedBanner?.let { bannerText ->
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BaseTeal.copy(alpha = 0.2f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = bannerText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseTeal,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

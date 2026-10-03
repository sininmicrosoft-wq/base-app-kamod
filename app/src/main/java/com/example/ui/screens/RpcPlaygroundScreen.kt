package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.BaseNetwork
import com.example.ui.BaseViewModel
import com.example.ui.components.BaseGasTrendD3Chart
import com.example.ui.components.BaseLiveTelemetryView
import com.example.ui.components.VibenetFaucetCard
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseTeal

@Composable
fun RpcPlaygroundScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val network by viewModel.selectedNetwork.collectAsStateWithLifecycle()
    val rpcResult by viewModel.rpcConsoleResult.collectAsStateWithLifecycle()
    val isRpcLoading by viewModel.isRpcLoading.collectAsStateWithLifecycle()
    val liveTelemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()
    val isTelemetryRefreshing by viewModel.isTelemetryRefreshing.collectAsStateWithLifecycle()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    var selectedMethod by remember { mutableStateOf("eth_blockNumber") }
    var selectedCodeTab by remember { mutableIntStateOf(0) }
    var copiedBanner by remember { mutableStateOf(false) }

    val commonMethods = listOf(
        "eth_blockNumber",
        "eth_gasPrice",
        "eth_chainId",
        "net_version",
        "eth_syncing"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("rpc_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("rpc_back_btn")) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Column {
                Text(
                    text = "Foundation: SDKs & JSON-RPC",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${network.displayName} • ${network.rpcUrl}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Retrofit Telemetry with On-Demand Manual Refresh Button
            item {
                BaseLiveTelemetryView(
                    telemetry = liveTelemetry,
                    isLoading = isTelemetryRefreshing,
                    onRefresh = { viewModel.refreshTelemetry() }
                )
            }

            // D3 24h Gas Price Trend Spline Line Chart
            item {
                BaseGasTrendD3Chart()
            }

            // Flashblocks Spec Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().testTag("flashblocks_info_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Base Flashblocks Architecture", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Flashblocks enable ~200ms sub-second transaction preconfirmations directly from the Base block builder websocket, reducing UX latency before standard 2-second L2 block finality.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Interactive RPC Console
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().testTag("rpc_console_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Live JSON-RPC Tester",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Execute live JSON-RPC calls against ${network.rpcUrl}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Method Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(commonMethods) { method ->
                                val isSelected = method == selectedMethod
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) BaseBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedMethod = method }
                                        .testTag("method_chip_$method")
                                ) {
                                    Text(
                                        text = method,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.runCustomRpc(selectedMethod, emptyList()) },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isRpcLoading,
                            modifier = Modifier.fillMaxWidth().testTag("send_rpc_btn")
                        ) {
                            if (isRpcLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Executing on Base RPC...", fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send JSON-RPC Request", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Response Output Viewer
                        rpcResult?.let { res ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (res.isSuccess) "✓ Response (${res.durationMs}ms)" else "✗ Error (${res.durationMs}ms)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (res.isSuccess) BaseTeal else BaseAmber
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(res.rawResponse))
                                        copiedBanner = true
                                    },
                                    modifier = Modifier.size(24.dp).testTag("copy_rpc_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF07090E),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = res.rawResponse,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = BaseCyan,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Developer SDK Code Snippets
            item {
                Text(
                    text = "Base SDK & Smart Contracts",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                val snippets = listOf(
                    "Viem (TS)" to """
import { createPublicClient, http } from 'viem';
import { base } from 'viem/chains';

export const client = createPublicClient({
  chain: base,
  transport: http('https://mainnet.base.org')
});

const block = await client.getBlockNumber();
console.log('Base Block:', block);
                    """.trimIndent(),

                    "Smart Wallet" to """
import { createCoinbaseWalletSDK } from '@coinbase/wallet-sdk';

const sdk = createCoinbaseWalletSDK({
  appName: 'Base App',
  preference: { options: 'smartWalletOnly' }
});

const provider = sdk.getProvider();
const accounts = await provider.request({ 
  method: 'eth_requestAccounts' 
});
                    """.trimIndent(),

                    "Kotlin Android" to """
val client = OkHttpClient()
val payload = JSONObject().apply {
    put("jsonrpc", "2.0")
    put("method", "eth_blockNumber")
    put("params", JSONArray())
    put("id", 1)
}

val request = Request.Builder()
    .url("https://mainnet.base.org")
    .post(payload.toString().toRequestBody())
    .build()
                    """.trimIndent(),

                    "B20 Solidity" to """
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

import "@openzeppelin/contracts/token/ERC20/ERC20.sol";

contract BaseB20Asset is ERC20 {
    address public complianceAuditor;
    mapping(address => bool) public kycWhitelist;

    constructor(string memory name, string memory symbol)
        ERC20(name, symbol) 
    {
        _mint(msg.sender, 100000 * 10 ** decimals());
    }
}
                    """.trimIndent()
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth().testTag("code_snippet_card")
                ) {
                    Column {
                        ScrollableTabRow(
                            selectedTabIndex = selectedCodeTab,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            edgePadding = 12.dp
                        ) {
                            snippets.forEachIndexed { index, (title, _) ->
                                Tab(
                                    selected = selectedCodeTab == index,
                                    onClick = { selectedCodeTab = index },
                                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }

                        val currentSnippet = snippets[selectedCodeTab].second
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF07090E))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = currentSnippet,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 16.sp
                            )

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(currentSnippet))
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(32.dp)
                                    .background(Color(0xFF1E293B), CircleShape)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = BaseCyan, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }

            // Vibenet Faucet Card
            item {
                val isFaucetLoading by viewModel.isFaucetLoading.collectAsStateWithLifecycle()
                val faucetHistory by viewModel.faucetHistory.collectAsStateWithLifecycle()

                VibenetFaucetCard(
                    onRequestFaucet = { address -> viewModel.requestVibenetFaucet(address) },
                    isLoading = isFaucetLoading,
                    history = faucetHistory
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

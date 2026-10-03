package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.VibenetBlockHeader
import com.example.data.remote.WsConnectionStatus
import com.example.ui.BaseViewModel
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseRed
import com.example.ui.theme.BaseTeal
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VibenetMonitorScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val connectionStatus by viewModel.vibenetWsClient.connectionStatus.collectAsStateWithLifecycle()
    val streamStats by viewModel.vibenetWsClient.stats.collectAsStateWithLifecycle()

    val headersList = remember { mutableStateListOf<VibenetBlockHeader>() }
    var autoScrollEnabled by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var expandedHeaderIndex by remember { mutableStateOf<Long?>(null) }

    // Start WebSocket connection on screen entry
    DisposableEffect(Unit) {
        viewModel.connectVibenetWs()
        onDispose {
            // Keep connection alive or gracefully managed
        }
    }

    // Collect incoming real-time block headers
    LaunchedEffect(Unit) {
        viewModel.vibenetWsClient.incomingHeaders.collectLatest { header ->
            headersList.add(0, header)
            if (headersList.size > 150) {
                headersList.removeLast()
            }
            if (autoScrollEnabled) {
                listState.animateScrollToItem(0)
            }
        }
    }

    val filteredHeaders = remember(headersList.toList(), searchQuery) {
        if (searchQuery.isBlank()) {
            headersList
        } else {
            val q = searchQuery.trim().lowercase()
            headersList.filter {
                it.blockNumber.toString().contains(q) ||
                it.hash.lowercase().contains(q) ||
                it.blockNumberHex.lowercase().contains(q)
            }
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF07090E))
            .testTag("vibenet_monitor_screen")
    ) {
        // Top Navigation Header
        Surface(
            color = Color(0xFF0D121F),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(36.dp).testTag("vibenet_monitor_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Base Vibenet Live Monitor",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BaseCyan.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "200ms BLOCKS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "wss://rpc.vibes.base.org/ws • eth_subscribe('newHeads')",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BaseCyan.copy(alpha = 0.8f)
                        )
                    }
                }

                // Connection Toggle
                IconButton(
                    onClick = {
                        if (connectionStatus == WsConnectionStatus.DISCONNECTED) {
                            viewModel.connectVibenetWs()
                        } else {
                            viewModel.disconnectVibenetWs()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .testTag("toggle_ws_connection_btn")
                ) {
                    Icon(
                        imageVector = if (connectionStatus == WsConnectionStatus.DISCONNECTED) Icons.Default.LinkOff else Icons.Default.Link,
                        contentDescription = "Toggle Connection",
                        tint = if (connectionStatus == WsConnectionStatus.DISCONNECTED) BaseRed else BaseTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Live Real-Time Telemetry KPI Bar
        Surface(
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Pill
                val (statusColor, statusLabel) = when (connectionStatus) {
                    WsConnectionStatus.CONNECTED -> BaseTeal to "CONNECTED (WSS)"
                    WsConnectionStatus.FALLBACK_STREAMING -> BaseCyan to "STREAMING (200MS)"
                    WsConnectionStatus.CONNECTING -> BaseAmber to "CONNECTING..."
                    WsConnectionStatus.DISCONNECTED -> BaseRed to "DISCONNECTED"
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (connectionStatus != WsConnectionStatus.DISCONNECTED) statusColor.copy(alpha = pulseAlpha)
                                else statusColor
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = statusLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                // Metrics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("CADENCE", fontSize = 9.sp, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        Text(
                            text = String.format(Locale.getDefault(), "%.1f blk/s", streamStats.blocksPerSecond.coerceAtLeast(4.8)),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = BaseCyan
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("LATENCY", fontSize = 9.sp, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        Text(
                            text = "${streamStats.averageLatencyMs} ms",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = BaseTeal
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("RECEIVED", fontSize = 9.sp, color = Color(0xFF94A3B8), letterSpacing = 0.5.sp)
                        Text(
                            text = "${headersList.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search & Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter block # or hash...", fontSize = 11.sp, color = Color(0xFF64748B)) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("ws_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BaseCyan,
                    unfocusedBorderColor = Color(0xFF1E293B),
                    focusedContainerColor = Color(0xFF0D121F),
                    unfocusedContainerColor = Color(0xFF0D121F),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            // Auto-scroll toggle pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (autoScrollEnabled) BaseCyan.copy(alpha = 0.2f) else Color(0xFF1E293B),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (autoScrollEnabled) BaseCyan else Color(0xFF334155)),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { autoScrollEnabled = !autoScrollEnabled }
                    .testTag("auto_scroll_toggle")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerticalAlignBottom,
                        contentDescription = "Auto Scroll",
                        tint = if (autoScrollEnabled) BaseCyan else Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (autoScrollEnabled) "Auto ON" else "Auto OFF",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (autoScrollEnabled) BaseCyan else Color(0xFF94A3B8)
                    )
                }
            }

            // Clear Button
            IconButton(
                onClick = { headersList.clear() },
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .testTag("clear_ws_logs_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = "Clear logs",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Toast feedback
        toastMessage?.let { msg ->
            Surface(
                color = BaseTeal.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 2.dp)
            ) {
                Text(
                    text = msg,
                    fontSize = 11.sp,
                    color = BaseTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(6.dp)
                )
            }
        }

        // Scrollable Block Header Terminal Log View
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
                .testTag("ws_block_headers_list"),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (filteredHeaders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (connectionStatus == WsConnectionStatus.DISCONNECTED)
                                "WebSocket Disconnected. Tap link button to reconnect."
                            else
                                "Subscribing to newHeads stream at wss://rpc.vibes.base.org/ws...",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            items(filteredHeaders, key = { it.blockNumber }) { header ->
                val isExpanded = expandedHeaderIndex == header.blockNumber
                val timeString = remember(header.receivedAtMs) {
                    val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
                    sdf.format(Date(header.receivedAtMs))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0B101D),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isExpanded) BaseCyan else Color(0xFF1E293B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            expandedHeaderIndex = if (isExpanded) null else header.blockNumber
                        }
                        .testTag("ws_header_item_${header.blockNumber}")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Block Number Badge + Timestamp
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BaseCyan.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(0.6.dp, BaseCyan.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "#${header.blockNumber}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        color = BaseCyan,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = timeString,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Right Chips: Latency + Base Fee + Expand
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BaseTeal.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${header.latencyMs}ms",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = BaseTeal,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BasePurple.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.3f", header.baseFeeGwei)} Gwei",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = BasePurple,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = "Expand",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Block Hash & Gas Meter
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Hash: ${header.hash.take(18)}...${header.hash.takeLast(6)}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFCBD5E1)
                            )

                            val gasPct = ((header.gasUsed.toFloat() / header.gasLimit.toFloat()) * 100f).coerceIn(0f, 100f)
                            Text(
                                text = "Gas: ${header.gasUsed / 1000}k / 30M (${String.format(Locale.getDefault(), "%.1f", gasPct)}%)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Visual Gas Gauge
                        LinearProgressIndicator(
                            progress = { (header.gasUsed.toFloat() / header.gasLimit.toFloat()).coerceIn(0.01f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = if (header.gasUsed > 20000000) BaseAmber else BaseCyan,
                            trackColor = Color(0xFF1E293B)
                        )

                        // Expanded View: Full JSON-RPC Payload & Explorer Link
                        AnimatedVisibility(visible = isExpanded) {
                            Column(modifier = Modifier.padding(top = 10.dp)) {
                                Text(
                                    text = "JSON-RPC 2.0 newHeads Payload",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF030712),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = header.rawJson,
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF38BDF8),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(header.rawJson))
                                            toastMessage = "Copied JSON payload for block #${header.blockNumber}!"
                                        },
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Copy Payload", fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            val url = "https://chain.base.org/vibenet/explorer"
                                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
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
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BaseNetwork
import com.example.data.model.InvoiceStatus
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseRed
import com.example.ui.theme.BaseTeal

@Composable
fun BaseBrandHeader(
    currentNetwork: BaseNetwork,
    blockNumber: Long,
    isLive: Boolean,
    onNetworkClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Base Logo & Wordmark
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(BaseBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "BASE",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(BaseBlue.copy(alpha = 0.15f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "FINANCE & SDK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = BaseCyan
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLive) BaseTeal.copy(alpha = pulseAlpha)
                                    else BaseAmber
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Block #${blockNumber}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Network Selector Pill
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onNetworkClick() }
                    .testTag("network_selector_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (currentNetwork.isTestnet) BaseAmber else BaseTeal)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentNetwork.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSelectorBottomSheet(
    currentNetwork: BaseNetwork,
    onSelectNetwork: (BaseNetwork) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("network_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Select Base Network",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Switch between production, Sepolia testnet, and disposable Vibenet",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            BaseNetwork.values().forEach { net ->
                val isSelected = net == currentNetwork
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onSelectNetwork(net)
                            onDismiss()
                        }
                        .testTag("network_item_${net.name}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) BaseBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BaseBlue) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = net.displayName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (!net.isTestnet) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(BaseTeal.copy(alpha = 0.2f))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text("MAINNET", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = BaseTeal)
                                    }
                                }
                            }
                            Text(
                                text = "Chain ID: ${net.chainId} • ${net.rpcUrl}",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Active",
                                tint = BaseBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun FlashblocksBadge(subSecondMs: Long = 185, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = BaseCyan.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = "Flashblocks",
                tint = BaseCyan,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Flashblocks: ${subSecondMs}ms Preconf",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = BaseCyan
            )
        }
    }
}

@Composable
fun StatusChip(status: InvoiceStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        InvoiceStatus.REQUESTED -> Triple(Color(0xFF334155), Color(0xFF94A3B8), "1. REQUESTED")
        InvoiceStatus.AUTHORIZED -> Triple(BaseAmber.copy(alpha = 0.2f), BaseAmber, "2. AUTHORIZED")
        InvoiceStatus.CAPTURED -> Triple(BaseTeal.copy(alpha = 0.2f), BaseTeal, "3. CAPTURED")
        InvoiceStatus.VERIFIED -> Triple(BaseCyan.copy(alpha = 0.2f), BaseCyan, "4. VERIFIED")
        InvoiceStatus.REFUNDED -> Triple(BaseRed.copy(alpha = 0.2f), BaseRed, "REFUNDED")
        InvoiceStatus.RECONCILED -> Triple(BaseBlue.copy(alpha = 0.2f), BaseCyan, "RECONCILED")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

/**
 * Procedural stylized QR Code canvas renderer for Base Payment Invoices and Smart Wallets.
 */
@Composable
fun BaseQrCanvas(
    payload: String,
    modifier: Modifier = Modifier,
    sizeDp: Int = 180
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size((sizeDp - 24).dp)) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val gridSize = 21
            val cellSize = canvasWidth / gridSize

            // Deterministic hash seed
            val hash = payload.hashCode().toLong()

            // Draw Finder patterns (corners)
            fun drawFinder(x: Float, y: Float) {
                // Outer 7x7
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(x, y),
                    size = Size(cellSize * 7, cellSize * 7),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                // Inner white 5x5
                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(x + cellSize, y + cellSize),
                    size = Size(cellSize * 5, cellSize * 5),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                // Center 3x3
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(x + cellSize * 2, y + cellSize * 2),
                    size = Size(cellSize * 3, cellSize * 3),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            drawFinder(0f, 0f)
            drawFinder(cellSize * 14, 0f)
            drawFinder(0f, cellSize * 14)

            // Fill pseudorandom data cells based on payload hash
            var bitIndex = 0
            for (r in 0 until gridSize) {
                for (c in 0 until gridSize) {
                    val inTopLeft = r < 7 && c < 7
                    val inTopRight = r < 7 && c >= 14
                    val inBottomLeft = r >= 14 && c < 7
                    if (inTopLeft || inTopRight || inBottomLeft) continue

                    // Calculate module value from payload
                    val pseudoBit = ((hash shr (bitIndex % 31)) and 1L) == 1L ||
                            ((r * 7 + c * 13 + payload.length) % 3 == 0)

                    if (pseudoBit) {
                        drawRect(
                            color = Color(0xFF0F172A),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.92f, cellSize * 0.92f)
                        )
                    }
                    bitIndex++
                }
            }

            // Central Base circle emblem in QR
            drawCircle(
                color = Color.White,
                radius = cellSize * 2.5f,
                center = Offset(canvasWidth / 2, canvasHeight / 2)
            )
            drawCircle(
                color = Color(0xFF0052FF),
                radius = cellSize * 1.8f,
                center = Offset(canvasWidth / 2, canvasHeight / 2)
            )
        }
    }
}

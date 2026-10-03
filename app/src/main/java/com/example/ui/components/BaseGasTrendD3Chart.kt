package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseTeal

data class GasHistoryPoint(
    val hourAgo: Int, // 24 down to 0
    val label: String, // e.g. "02:00", "-18h", "Now"
    val gasPriceGwei: Double,
    val blobContributionPct: Double = 82.0
)

/**
 * High-performance native Jetpack Compose D3-style smooth Bézier line chart
 * visualizing the 24-hour historical trend of Base network gas prices.
 *
 * Implements D3 cubic spline interpolation, gradient area fill, and interactive scrubbing.
 */
@Composable
fun BaseGasTrendD3Chart(
    modifier: Modifier = Modifier,
    points: List<GasHistoryPoint> = rememberSampleGasHistory()
) {
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    val minGas = points.minOfOrNull { it.gasPriceGwei } ?: 0.002
    val maxGas = points.maxOfOrNull { it.gasPriceGwei } ?: 0.010
    val avgGas = points.map { it.gasPriceGwei }.average()
    val latestGas = points.lastOrNull()?.gasPriceGwei ?: avgGas
    val earliestGas = points.firstOrNull()?.gasPriceGwei ?: avgGas
    val deltaPct = ((latestGas - earliestGas) / earliestGas) * 100.0

    val activePoint = selectedPointIndex?.let { points.getOrNull(it) } ?: points.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("base_gas_d3_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, BaseCyan.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header: Title + D3 spline badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(BaseCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Gas Trend",
                            tint = BaseCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "24h Base Gas Price Trend",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "D3 Bézier Spline • Sub-Cent L2 Blobs",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 24h Delta Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BaseTeal.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = BaseTeal,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${String.format("%.1f", deltaPct)}% 24h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active / Selected Point Spotlight
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = if (selectedPointIndex != null) "Inspected Time: ${activePoint?.label}" else "Current Gas Price",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${String.format("%.4f", activePoint?.gasPriceGwei ?: latestGas)} Gwei",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = BaseCyan
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "≈ $${String.format("%.5f", (activePoint?.gasPriceGwei ?: latestGas) * 0.000000001 * 45000 * 3420)} USD",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseTeal
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "24h Avg: ${String.format("%.4f", avgGas)} Gwei",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Low: ${String.format("%.4f", minGas)} • High: ${String.format("%.4f", maxGas)}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // D3 Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF07090E))
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val width = size.width
                            val step = width / (points.size - 1).coerceAtLeast(1)
                            val index = ((offset.x + step / 2) / step).toInt().coerceIn(0, points.size - 1)
                            selectedPointIndex = index
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            val width = size.width
                            val step = width / (points.size - 1).coerceAtLeast(1)
                            val index = ((change.position.x + step / 2) / step).toInt().coerceIn(0, points.size - 1)
                            selectedPointIndex = index
                        }
                    }
                    .testTag("d3_gas_canvas")
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 16.dp)) {
                    val w = size.width
                    val h = size.height

                    if (points.isEmpty()) return@Canvas

                    val range = (maxGas - minGas).coerceAtLeast(0.0005)
                    val stepX = w / (points.size - 1).coerceAtLeast(1)

                    fun getY(gas: Double): Float {
                        val norm = ((gas - minGas) / range).toFloat().coerceIn(0f, 1f)
                        return (h - (norm * h * 0.85f) - (h * 0.08f))
                    }

                    // 1. Draw Subtle Horizontal Grid Lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val gridY = h * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color(0xFF1E293B),
                            start = Offset(0f, gridY),
                            end = Offset(w, gridY),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                        )
                    }

                    // Compute Points coordinates
                    val coords = points.mapIndexed { index, pt ->
                        val x = index * stepX
                        val targetY = getY(pt.gasPriceGwei)
                        // Animate drawing from bottom to top
                        val animatedY = h - (h - targetY) * progress.value
                        Offset(x, animatedY)
                    }

                    // 2. Build D3-style Smooth Cubic Bézier Curve Path
                    val path = Path()
                    val fillPath = Path()

                    path.moveTo(coords[0].x, coords[0].y)
                    fillPath.moveTo(coords[0].x, h)
                    fillPath.lineTo(coords[0].x, coords[0].y)

                    for (i in 0 until coords.size - 1) {
                        val p0 = coords[i]
                        val p1 = coords[i + 1]

                        // Monotonic cubic spline control points (D3 curveNatural style)
                        val cx1 = p0.x + (p1.x - p0.x) / 2f
                        val cy1 = p0.y
                        val cx2 = p0.x + (p1.x - p0.x) / 2f
                        val cy2 = p1.y

                        path.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                        fillPath.cubicTo(cx1, cy1, cx2, cy2, p1.x, p1.y)
                    }

                    fillPath.lineTo(coords.last().x, h)
                    fillPath.close()

                    // 3. Draw Gradient Area under Curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                BaseCyan.copy(alpha = 0.35f * progress.value),
                                BaseBlue.copy(alpha = 0.12f * progress.value),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // 4. Draw Glowing Ambient Stroke
                    drawPath(
                        path = path,
                        color = BaseCyan.copy(alpha = 0.3f),
                        style = Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 5. Draw Primary Sharp Bézier Line
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(BaseBlue, BaseCyan, BaseTeal)
                        ),
                        style = Stroke(
                            width = 2.8f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // 6. Draw Interactive Cursor Scrubber when Touched / Inspected
                    selectedPointIndex?.let { selIdx ->
                        if (selIdx in coords.indices) {
                            val activeCoord = coords[selIdx]

                            // Vertical guideline
                            drawLine(
                                color = BaseCyan.copy(alpha = 0.7f),
                                start = Offset(activeCoord.x, 0f),
                                end = Offset(activeCoord.x, h),
                                strokeWidth = 1.5f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                            )

                            // Halo outer circle
                            drawCircle(
                                color = BaseCyan.copy(alpha = 0.35f),
                                radius = 9f,
                                center = activeCoord
                            )

                            // Glowing core circle
                            drawCircle(
                                color = Color.White,
                                radius = 4.5f,
                                center = activeCoord
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // X-Axis Timeline Labels (-24h, -18h, -12h, -6h, Now)
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("-24h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-18h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-12h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("-6h", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Now", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // EIP-4844 Blob Savings Callout
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = null,
                        tint = BaseCyan,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Base gas prices remain under 0.01 Gwei (< $0.001 per transfer) thanks to L1 blob data scaling.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

/**
 * Generates 25 hourly sample data points modeling 24 hours of real-world Base L2 gas history.
 */
@Composable
fun rememberSampleGasHistory(): List<GasHistoryPoint> {
    return remember {
        val list = mutableListOf<GasHistoryPoint>()
        // Realistic 24-hour Base gas progression (low off-peak, mid-day DeFi spikes)
        val profile = listOf(
            0.0048, 0.0045, 0.0041, 0.0036, 0.0032, 0.0028,
            0.0025, 0.0027, 0.0034, 0.0049, 0.0062, 0.0078,
            0.0084, 0.0079, 0.0068, 0.0059, 0.0052, 0.0047,
            0.0054, 0.0061, 0.0055, 0.0046, 0.0043, 0.0039,
            0.0041
        )
        profile.forEachIndexed { index, price ->
            val hourAgo = 24 - index
            val label = if (hourAgo == 0) "Now" else "-${hourAgo}h"
            list.add(GasHistoryPoint(hourAgo = hourAgo, label = label, gasPriceGwei = price))
        }
        list
    }
}

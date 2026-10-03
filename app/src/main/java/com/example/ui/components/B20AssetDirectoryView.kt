package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Token
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.data.model.B20Asset
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseTeal
import java.text.NumberFormat
import java.util.Locale

/**
 * Composable component that fetches and lists deployed B20 assets from the Base network,
 * prominently displaying supply caps, cap utilization progress, and current holder counts.
 */
@Composable
fun B20AssetDirectoryView(
    assets: List<B20Asset>,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
    networkName: String = "Base Vibenet",
    onAssetClick: ((B20Asset) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US).apply { maximumFractionDigits = 0 } }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var copiedBanner by remember { mutableStateOf<String?>(null) }

    val categories = remember(assets) {
        listOf("All") + assets.map { it.assetCategory }.distinct()
    }

    val filteredAssets = remember(assets, searchQuery, selectedCategory) {
        assets.filter { asset ->
            val matchesCategory = selectedCategory == "All" || asset.assetCategory.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                asset.name.contains(searchQuery, ignoreCase = true) ||
                asset.symbol.contains(searchQuery, ignoreCase = true) ||
                asset.contractAddress.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    // Aggregates
    val totalValuation = remember(assets) { assets.sumOf { it.totalValuationUsd } }
    val totalHolders = remember(assets) { assets.sumOf { it.holderCount } }
    val totalMintedSupply = remember(assets) { assets.sumOf { it.totalSupply } }
    val totalCapacity = remember(assets) { assets.sumOf { it.supplyCap } }
    val overallCapUtilization = remember(totalMintedSupply, totalCapacity) {
        if (totalCapacity > 0) (totalMintedSupply.toDouble() / totalCapacity.toDouble()) * 100.0 else 0.0
    }

    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        ),
        label = "spinAngle"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("b20_asset_directory")
    ) {
        // Section Header with Refresh Action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Deployed B20 Assets",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BasePurple.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(0.6.dp, BasePurple.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = networkName.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BasePurple,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = "Base native token standard with built-in caps, holder governance & memos",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .testTag("refresh_b20_assets_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh B20 Assets",
                    tint = BasePurple,
                    modifier = Modifier
                        .size(18.dp)
                        .then(if (isRefreshing) Modifier.rotate(spinAngle) else Modifier)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Aggregate Metrics Dashboard Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, BasePurple.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth().testTag("b20_metrics_card")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Metric 1: Total Valuation
                Column {
                    Text("TOTAL B20 VALUATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = currencyFormat.format(totalValuation),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BaseCyan
                    )
                }

                // Metric 2: Total Active Holders
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TOTAL HOLDERS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Groups, contentDescription = null, tint = BaseTeal, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = numberFormat.format(totalHolders),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = BaseTeal
                        )
                    }
                }

                // Metric 3: Aggregate Cap Utilization
                Column(horizontalAlignment = Alignment.End) {
                    Text("CAP UTILIZATION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f%%", overallCapUtilization),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = BasePurple
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search B20 asset name, symbol, or contract...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("b20_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BasePurple,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BasePurple.copy(alpha = 0.25f),
                        selectedLabelColor = BasePurple
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) BasePurple else MaterialTheme.colorScheme.outline,
                        selectedBorderColor = BasePurple,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        copiedBanner?.let { msg ->
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = BaseTeal.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    fontSize = 11.sp,
                    color = BaseTeal,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Asset Cards List
        if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No B20 assets matching \"$searchQuery\"" else "No B20 assets deployed yet.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredAssets.forEach { asset ->
                    B20AssetCard(
                        asset = asset,
                        onCopy = { text, label ->
                            clipboardManager.setText(AnnotatedString(text))
                            copiedBanner = "Copied $label!"
                        },
                        onExplorerClick = {
                            val url = "https://chain.base.org/vibenet/explorer"
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(browserIntent)
                        },
                        onClick = { onAssetClick?.invoke(asset) }
                    )
                }
            }
        }
    }
}

/**
 * Individual B20 Asset Card displaying supply caps and current holder counts.
 */
@Composable
fun B20AssetCard(
    asset: B20Asset,
    onCopy: (String, String) -> Unit,
    onExplorerClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.US) }
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    val capUtilizationPct = remember(asset.totalSupply, asset.supplyCap) {
        if (asset.supplyCap > 0) {
            ((asset.totalSupply.toDouble() / asset.supplyCap.toDouble()) * 100.0).coerceIn(0.0, 100.0)
        } else {
            100.0
        }
    }

    val remainingSupply = remember(asset.totalSupply, asset.supplyCap) {
        (asset.supplyCap - asset.totalSupply).coerceAtLeast(0L)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("b20_asset_card_${asset.symbol}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Token Name + Symbol Pill + Category
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
                            .background(BasePurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Token,
                            contentDescription = null,
                            tint = BasePurple,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = asset.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = BaseCyan.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = asset.symbol,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${asset.assetCategory} • Custodian: ${asset.custodian}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Yield Badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BaseTeal.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(0.6.dp, BaseTeal.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "${asset.dividendYieldPct}% APY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BaseTeal,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SUPPLY CAP & MINTED PROGRESS (User Request Highlight)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth().testTag("supply_cap_section_${asset.symbol}")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUPPLY CAP UTILIZATION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${String.format(Locale.getDefault(), "%.1f", capUtilizationPct)}% of Cap Minted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (capUtilizationPct > 80.0) BaseAmber else BaseCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Progress Bar
                    LinearProgressIndicator(
                        progress = { (capUtilizationPct / 100.0).toFloat().coerceIn(0.01f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = when {
                            capUtilizationPct > 85.0 -> BaseAmber
                            capUtilizationPct > 50.0 -> BasePurple
                            else -> BaseCyan
                        },
                        trackColor = Color(0xFF1E293B)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Minted: ${numberFormat.format(asset.totalSupply)} ${asset.symbol}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Cap: ${numberFormat.format(asset.supplyCap)} ${asset.symbol}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = BasePurple
                        )
                    }

                    if (remainingSupply > 0) {
                        Text(
                            text = "Remaining Cap Headroom: ${numberFormat.format(remainingSupply)} ${asset.symbol}",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CURRENT HOLDER COUNT & VALUATION ROW (User Request Highlight)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Holder Count Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BaseTeal.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseTeal.copy(alpha = 0.3f)),
                    modifier = Modifier.testTag("holder_count_${asset.symbol}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = "Holders",
                            tint = BaseTeal,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${numberFormat.format(asset.holderCount)} Current Holders",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = BaseTeal
                        )
                    }
                }

                // Valuation
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currencyFormat.format(asset.totalValuationUsd),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$${String.format(Locale.getDefault(), "%.2f", asset.pricePerToken)} / token",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // B20 Standard Built-in Policies & Transfer Memos Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (asset.memosEnabled) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BasePurple.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "MEMOS: ENABLED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BasePurple,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                if (asset.accreditedOnly) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BaseAmber.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "ACCREDITED ONLY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseAmber,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = BaseTeal.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "PUBLIC TRADING",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseTeal,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "NETWORK: ${asset.network}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contract Address & Explorer Actions
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Contract: ${asset.contractAddress.take(12)}...${asset.contractAddress.takeLast(6)}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = BaseCyan
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Contract",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onCopy(asset.contractAddress, "${asset.symbol} Contract") }
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onExplorerClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInBrowser,
                            contentDescription = "View on Explorer",
                            tint = BaseCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Explorer",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BaseCyan
                        )
                    }
                }
            }
        }
    }
}

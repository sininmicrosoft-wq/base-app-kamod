package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BasePurple
import com.example.ui.theme.BaseTeal

enum class FlowKind {
    TRADE, LEND, BORROW, EARN
}

data class MetricItem(
    val label: String,
    val value: String,
    val tone: String = "neutral" // "ok", "warn", "err", "neutral"
)

data class LogEntry(
    val timestamp: String,
    val level: String, // "EVENT", "INFO", "ERROR", "PENDING"
    val name: String,
    val detail: String,
    val kind: String // "ok", "err", "info", "pending"
)

data class StepDef(
    val stage: String,
    val action: String,
    val text: String,
    val summary: List<Pair<String, String>>,
    val caption: String,
    val metricsBuilder: () -> List<MetricItem>,
    val entriesBuilder: () -> List<LogEntry>
)

data class FlowDef(
    val kind: FlowKind,
    val label: String,
    val title: String,
    val footer: String,
    val steps: List<StepDef>
)

/**
 * Interactive Base DeFi Flow Simulation Component based on the official Base Documentation:
 * Supports Trade (0x Swap API AllowanceHolder), Lend (USDC lending), Borrow (WETH collateral), and Earn (Vaults).
 */
@Composable
fun DeFiInteractiveFlowView(
    initialFlow: FlowKind = FlowKind.TRADE,
    onExecuteLiveAction: ((FlowKind) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var activeFlowKind by remember { mutableStateOf(initialFlow) }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var show0xCodeModal by remember { mutableStateOf(false) }
    var copiedBanner by remember { mutableStateOf<String?>(null) }

    val networkName = "Base Vibenet"

    val tradeFlow = remember {
        FlowDef(
            kind = FlowKind.TRADE,
            label = "Trade",
            title = "Swap tokens through an aggregated route (0x AllowanceHolder)",
            footer = "Illustrative only · quotes, routes, and minimum output can change.",
            steps = listOf(
                StepDef(
                    stage = "Quote",
                    action = "Request quote",
                    text = "Request a firm 0x quote to swap 1,000 USDC for WETH on Base.",
                    summary = listOf(
                        "Operation" to "Swap",
                        "Sell" to "1,000 USDC",
                        "Buy" to "WETH",
                        "Slippage" to "0.5%",
                        "Network" to networkName
                    ),
                    caption = "Show the user the minimum output, fees, and route before approval.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Sell", "1,000 USDC"),
                            MetricItem("Quoted output", "0.397 WETH"),
                            MetricItem("Minimum output", "0.395 WETH")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:11", "INFO", "route quoted", "0x · 0.397 WETH", "info"),
                            LogEntry("10:42:12", "INFO", "minimum output", "0.395 WETH", "info")
                        )
                    }
                ),
                StepDef(
                    stage = "Simulate",
                    action = "Simulate swap",
                    text = "Fetch a fresh quote, then simulate its transaction data against the user's current wallet state.",
                    summary = listOf(
                        "Operation" to "Simulate",
                        "Expected" to "0.397 WETH",
                        "Minimum" to "0.395 WETH",
                        "Result" to "No revert",
                        "Network" to networkName
                    ),
                    caption = "Do not submit stale calldata after balances, allowances, or market prices change.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Sell", "1,000 USDC"),
                            MetricItem("Quoted output", "0.397 WETH"),
                            MetricItem("Minimum output", "0.395 WETH", tone = "ok")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:13", "EVENT", "simulation", "transaction succeeds", "ok"),
                            LogEntry("10:42:14", "INFO", "quote refreshed", "allowance satisfied", "info")
                        )
                    }
                ),
                StepDef(
                    stage = "Swap",
                    action = "Approve and submit swap",
                    text = "Approve only the AllowanceHolder address returned by the quote for the exact sell amount, then ask the wallet to sign the prepared transaction and wait for its receipt.",
                    summary = listOf(
                        "Operation" to "Approve and execute swap",
                        "Sell" to "1,000 USDC",
                        "Receive" to "0.397 WETH",
                        "Minimum" to "0.395 WETH",
                        "Spender" to "0x AllowanceHolder",
                        "Network" to networkName
                    ),
                    caption = "Never approve the 0x Settler contract; use the spender returned by the API. Refresh balances from Base after receipt confirms.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("USDC spent", "1,000 USDC"),
                            MetricItem("WETH received", "0.397 WETH", tone = "ok"),
                            MetricItem("Status", "Confirmed", tone = "ok")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:15", "EVENT", "approve", "1,000 USDC · AllowanceHolder", "ok"),
                            LogEntry("10:42:16", "EVENT", "swap submitted", "0x route", "ok"),
                            LogEntry("10:42:17", "EVENT", "swap confirmed", "0.397 WETH received", "ok")
                        )
                    }
                )
            )
        )
    }

    val lendFlow = remember {
        FlowDef(
            kind = FlowKind.LEND,
            label = "Lend",
            title = "Supply assets to a lending market",
            footer = "Illustrative only · rates and liquidity vary by market.",
            steps = listOf(
                StepDef(
                    stage = "Load",
                    action = "Load wallet",
                    text = "A user has 1,000 USDC available in their wallet.",
                    summary = listOf(
                        "Operation" to "Load wallet",
                        "Asset" to "USDC",
                        "Amount" to "1,000 USDC",
                        "Network" to networkName
                    ),
                    caption = "Initial wallet state loaded.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "1,000 USDC"),
                            MetricItem("Supplied", "0 USDC")
                        )
                    },
                    entriesBuilder = {
                        listOf(LogEntry("10:42:11", "INFO", "wallet balance", "1,000 USDC", "info"))
                    }
                ),
                StepDef(
                    stage = "Supply",
                    action = "Supply USDC",
                    text = "Approve the market and supply the USDC from the user's wallet.",
                    summary = listOf(
                        "Operation" to "Supply",
                        "Market" to "USDC lending",
                        "Amount" to "1,000 USDC",
                        "Supply APY" to "4.2%",
                        "Network" to networkName
                    ),
                    caption = "The wallet now owns a direct protocol position.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "0 USDC"),
                            MetricItem("Supplied", "1,000 USDC"),
                            MetricItem("Supply APY", "4.2% variable")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:12", "EVENT", "approve", "1,000 USDC", "ok"),
                            LogEntry("10:42:13", "EVENT", "supply", "1,000 USDC", "ok")
                        )
                    }
                ),
                StepDef(
                    stage = "Accrue",
                    action = "Accrue 30 days",
                    text = "The supplied position accrues illustrative variable interest.",
                    summary = listOf(
                        "Operation" to "Accrue interest",
                        "Period" to "30 days",
                        "Supply APY" to "4.2%",
                        "Balance" to "1,003.45 USDC"
                    ),
                    caption = "Actual rates change with market utilization.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "0 USDC"),
                            MetricItem("Supplied", "1,003.45 USDC"),
                            MetricItem("Supply APY", "4.2% variable")
                        )
                    },
                    entriesBuilder = {
                        listOf(LogEntry("10:42:14", "EVENT", "position updated", "+3.45 USDC", "ok"))
                    }
                ),
                StepDef(
                    stage = "Withdraw",
                    action = "Withdraw",
                    text = "Withdraw the available position back to the user's wallet.",
                    summary = listOf(
                        "Operation" to "Withdraw",
                        "Amount" to "1,003.45 USDC",
                        "To" to "Wallet",
                        "Network" to networkName
                    ),
                    caption = "Withdrawals depend on available market liquidity.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "1,003.45 USDC"),
                            MetricItem("Supplied", "0 USDC")
                        )
                    },
                    entriesBuilder = {
                        listOf(LogEntry("10:42:15", "EVENT", "withdraw", "1,003.45 USDC", "ok"))
                    }
                )
            )
        )
    }

    val borrowFlow = remember {
        FlowDef(
            kind = FlowKind.BORROW,
            label = "Borrow",
            title = "Borrow against supplied collateral",
            footer = "Illustrative only · liquidation parameters differ by protocol and market.",
            steps = listOf(
                StepDef(
                    stage = "Collateral",
                    action = "Supply collateral",
                    text = "A user supplies 2 WETH as collateral at an illustrative $2,500 price.",
                    summary = listOf(
                        "Operation" to "Supply collateral",
                        "Collateral" to "2 WETH",
                        "Value" to "$5,000",
                        "Network" to networkName
                    ),
                    caption = "The collateral remains exposed to market price changes.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Collateral", "2 WETH · $5,000"),
                            MetricItem("Debt", "0 USDC"),
                            MetricItem("Health factor", "—")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:11", "EVENT", "supply collateral", "2 WETH", "ok"),
                            LogEntry("10:42:12", "EVENT", "collateral enabled", "WETH", "ok")
                        )
                    }
                ),
                StepDef(
                    stage = "Borrow",
                    action = "Borrow USDC",
                    text = "Borrow 2,000 USDC against the collateral.",
                    summary = listOf(
                        "Operation" to "Borrow",
                        "Asset" to "USDC",
                        "Amount" to "2,000 USDC",
                        "Health factor" to "2.00",
                        "Network" to networkName
                    ),
                    caption = "A higher health factor provides more room before liquidation.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Collateral", "2 WETH · $5,000"),
                            MetricItem("Debt", "2,000 USDC"),
                            MetricItem("Health factor", "2.00", tone = "ok")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:13", "EVENT", "borrow", "2,000 USDC", "ok"),
                            LogEntry("10:42:14", "INFO", "health factor", "2.00", "info")
                        )
                    }
                ),
                StepDef(
                    stage = "Price drop",
                    action = "Simulate price drop",
                    text = "WETH falls to an illustrative $1,500 while the debt remains unchanged.",
                    summary = listOf(
                        "Operation" to "Price update",
                        "Collateral" to "2 WETH · $3,000",
                        "Debt" to "2,000 USDC",
                        "Health factor" to "1.20"
                    ),
                    caption = "At or below the protocol's liquidation threshold, collateral can be sold to repay debt.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Collateral", "2 WETH · $3,000"),
                            MetricItem("Debt", "2,000 USDC"),
                            MetricItem("Health factor", "1.20", tone = "warn")
                        )
                    },
                    entriesBuilder = {
                        listOf(LogEntry("10:42:15", "ERROR", "risk increased", "health factor 2.00 → 1.20", "err"))
                    }
                )
            )
        )
    }

    val earnFlow = remember {
        FlowDef(
            kind = FlowKind.EARN,
            label = "Earn",
            title = "Embed a vault-based earn product",
            footer = "Illustrative only · vault yield is variable and not guaranteed.",
            steps = listOf(
                StepDef(
                    stage = "Select",
                    action = "Select vault",
                    text = "A user has 1,000 USDC and chooses a curated vault in your app.",
                    summary = listOf(
                        "Operation" to "Select vault",
                        "Vault" to "USDC yield",
                        "Asset" to "USDC",
                        "Network" to networkName
                    ),
                    caption = "The vault abstracts the underlying market allocation.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "1,000 USDC"),
                            MetricItem("Vault shares", "0"),
                            MetricItem("Redeemable", "0 USDC")
                        )
                    },
                    entriesBuilder = {
                        listOf(LogEntry("10:42:11", "INFO", "vault selected", "USDC · variable yield", "info"))
                    }
                ),
                StepDef(
                    stage = "Deposit",
                    action = "Deposit USDC",
                    text = "Deposit once and receive shares that represent the vault position.",
                    summary = listOf(
                        "Operation" to "Deposit",
                        "Amount" to "1,000 USDC",
                        "Vault shares" to "1,000",
                        "Share price" to "$1.00",
                        "Network" to networkName
                    ),
                    caption = "The user holds vault shares instead of managing each market position.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "0 USDC"),
                            MetricItem("Vault shares", "1,000"),
                            MetricItem("Share price", "$1.00"),
                            MetricItem("Redeemable", "1,000 USDC")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:12", "EVENT", "approve", "1,000 USDC", "ok"),
                            LogEntry("10:42:13", "EVENT", "deposit", "1,000 USDC → 1,000 shares", "ok")
                        )
                    }
                ),
                StepDef(
                    stage = "Accrue",
                    action = "Accrue value",
                    text = "As the vault earns, each share becomes redeemable for more USDC.",
                    summary = listOf(
                        "Operation" to "Accrue yield",
                        "Vault shares" to "1,000",
                        "Share price" to "$1.01",
                        "Redeemable" to "1,010 USDC"
                    ),
                    caption = "Actual vault performance can rise or fall and depends on its strategy.",
                    metricsBuilder = {
                        listOf(
                            MetricItem("Wallet", "0 USDC"),
                            MetricItem("Vault shares", "1,000"),
                            MetricItem("Share price", "$1.01"),
                            MetricItem("Redeemable", "1,010 USDC", tone = "ok")
                        )
                    },
                    entriesBuilder = {
                        listOf(
                            LogEntry("10:42:14", "EVENT", "share value updated", "$1.00 → $1.01", "ok"),
                            LogEntry("10:42:15", "INFO", "redeemable assets", "1,010 USDC", "info")
                        )
                    }
                )
            )
        )
    }

    val currentFlow = when (activeFlowKind) {
        FlowKind.TRADE -> tradeFlow
        FlowKind.LEND -> lendFlow
        FlowKind.BORROW -> borrowFlow
        FlowKind.EARN -> earnFlow
    }

    val isDone = currentStepIndex >= currentFlow.steps.size
    val activeStep = if (isDone) currentFlow.steps.last() else currentFlow.steps[currentStepIndex]

    // Cumulative metrics & logs
    val currentMetrics = remember(currentStepIndex, activeFlowKind) {
        if (currentStepIndex == 0) emptyList() else currentFlow.steps[(currentStepIndex - 1).coerceAtMost(currentFlow.steps.size - 1)].metricsBuilder()
    }

    val currentLogs = remember(currentStepIndex, activeFlowKind) {
        val executedSteps = currentFlow.steps.take(currentStepIndex)
        val logs = mutableListOf<LogEntry>()
        executedSteps.forEach { step -> logs.addAll(step.entriesBuilder()) }
        // Add pending steps
        currentFlow.steps.drop(currentStepIndex).forEachIndexed { idx, pendingStep ->
            logs.add(
                LogEntry(
                    timestamp = "10:42:${12 + executedSteps.size + idx}",
                    level = "PENDING",
                    name = pendingStep.action,
                    detail = "",
                    kind = "pending"
                )
            )
        }
        logs
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("defi_interactive_demo_card")
    ) {
        Column {
            // Scenario Selection Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SCENARIO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(FlowKind.TRADE, FlowKind.LEND, FlowKind.BORROW, FlowKind.EARN).forEach { kind ->
                        val isSelected = kind == activeFlowKind
                        val label = when (kind) {
                            FlowKind.TRADE -> "Trade"
                            FlowKind.LEND -> "Lend"
                            FlowKind.BORROW -> "Borrow"
                            FlowKind.EARN -> "Earn"
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) BaseBlue else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) BaseBlue else MaterialTheme.colorScheme.outline
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    activeFlowKind = kind
                                    currentStepIndex = 0
                                }
                                .testTag("flow_pill_$label")
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Reset Action
                IconButton(
                    onClick = { currentStepIndex = 0 },
                    modifier = Modifier.size(28.dp).testTag("reset_flow_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Flow",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Top Horizontal Stages Tracker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentFlow.steps.forEachIndexed { idx, st ->
                    val state = when {
                        idx < currentStepIndex -> "done"
                        idx == currentStepIndex -> "now"
                        else -> "future"
                    }
                    val col = when (state) {
                        "done" -> BaseTeal
                        "now" -> BaseBlue
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "${idx + 1} ",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = col.copy(alpha = 0.8f)
                        )
                        Text(
                            text = st.stage,
                            fontSize = 12.sp,
                            fontWeight = if (state == "now") FontWeight.Bold else FontWeight.Medium,
                            color = col
                        )
                        if (state == "done") {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = BaseTeal,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }

            // Split View: Stepper Rail + Active Step Workspace
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        androidx.compose.foundation.BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant)
                    )
            ) {
                // Left Rail: Step Progress & Metrics
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(14.dp)
                ) {
                    currentFlow.steps.forEachIndexed { idx, st ->
                        val state = when {
                            idx < currentStepIndex -> "done"
                            idx == currentStepIndex -> "now"
                            else -> "future"
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Circle Indicator
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (state) {
                                            "done" -> BaseTeal
                                            "now" -> BaseBlue
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        1.5.dp,
                                        when (state) {
                                            "done" -> BaseTeal
                                            "now" -> BaseBlue
                                            else -> MaterialTheme.colorScheme.outline
                                        },
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (state == "done") {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (state == "now") Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.padding(bottom = if (idx == currentFlow.steps.size - 1) 0.dp else 12.dp)) {
                                Text(
                                    text = st.action,
                                    fontSize = 11.sp,
                                    fontWeight = if (state == "now") FontWeight.Bold else FontWeight.Normal,
                                    color = if (state == "future") MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = when (state) {
                                        "done" -> "Complete"
                                        "now" -> "In progress"
                                        else -> "Pending"
                                    },
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (state) {
                                        "done" -> BaseTeal
                                        "now" -> BaseBlue
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }

                    // Position Metrics readout
                    if (currentMetrics.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.6.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "POSITION",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        currentMetrics.forEach { metric ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = metric.label,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = metric.value,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = when (metric.tone) {
                                        "ok" -> BaseTeal
                                        "warn" -> BaseAmber
                                        "err" -> Color(0xFFFC401F)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(0.6.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                // Right Rail: Active Action Card & Summary
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .padding(14.dp)
                ) {
                    if (isDone) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = BaseTeal.copy(alpha = 0.15f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BaseTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Flow complete",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseTeal
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${currentFlow.title} — every step ran on-chain in the simulation above.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { currentStepIndex = 0 },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().height(38.dp)
                        ) {
                            Text("Run again", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = { show0xCodeModal = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("view_0x_code_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("See 0x Technical Details →", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = activeStep.action,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = activeStep.text,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Summary table
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                activeStep.summary.forEach { (key, value) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = key, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            text = value,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Run Button
                        Button(
                            onClick = {
                                if (currentStepIndex < currentFlow.steps.size) {
                                    currentStepIndex++
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("run_step_btn")
                        ) {
                            Text(
                                text = activeStep.action,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        if (currentStepIndex > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            TextButton(
                                onClick = { currentStepIndex-- },
                                modifier = Modifier.fillMaxWidth().height(32.dp)
                            ) {
                                Text("Back", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Transaction Event Log Console
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF07090E))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TRANSACTION EVENT LOG",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF94A3B8),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Base Vibenet 200ms",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = BaseCyan
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    currentLogs.take(5).forEach { r ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = r.timestamp,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[${r.level}]",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = when (r.level) {
                                    "EVENT" -> BaseCyan
                                    "INFO" -> Color(0xFF94A3B8)
                                    "ERROR" -> Color(0xFFFC401F)
                                    else -> Color(0xFF475569)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${r.name} ${if (r.detail.isNotEmpty()) "· " + r.detail else ""}",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = when (r.kind) {
                                    "err" -> Color(0xFFFC401F)
                                    "pending" -> Color(0xFF64748B)
                                    else -> Color(0xFFE2E8F0)
                                },
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Footer note
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = currentFlow.footer,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 0x Swap API Specification Dialog
    if (show0xCodeModal) {
        val viem0xSnippet = """
// 0x Swap API with AllowanceHolder on Base
import { publicClient, walletClient } from './clients.js';
import { parseUnits, formatUnits, parseAbi } from 'viem';

const USDC = '0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913';
const WETH = '0x4200000000000000000000000000000000000006';
const sellAmount = parseUnits('1000', 6);
const user = walletClient.account.address;

// 1. Fetch Firm 0x Quote
const params = new URLSearchParams({
  chainId: '8453',
  sellToken: USDC,
  buyToken: WETH,
  sellAmount: sellAmount.toString(),
  taker: user,
  slippageBps: '50'
});

const response = await fetch(
  `https://api.0x.org/swap/allowance-holder/quote?${'$'}params`,
  { headers: { '0x-api-key': ZERO_EX_API_KEY, '0x-version': 'v2' } }
);
const quote = await response.json();

// 2. CRITICAL: Approve AllowanceHolder (Never approve Settler!)
if (quote.issues.allowance) {
  const approval = await publicClient.simulateContract({
    account: user,
    address: USDC,
    abi: parseAbi(['function approve(address,uint256) returns (bool)']),
    functionName: 'approve',
    args: [quote.issues.allowance.spender, sellAmount]
  });
  await walletClient.writeContract(approval.request);
}

// 3. Submit Transaction to Base
const tx = {
  account: walletClient.account,
  to: quote.transaction.to,
  data: quote.transaction.data,
  value: BigInt(quote.transaction.value ?? '0')
};
const hash = await walletClient.sendTransaction(tx);
await publicClient.waitForTransactionReceipt({ hash });
        """.trimIndent()

        androidx.compose.material3.AlertDialog(
            onDismissRequest = { show0xCodeModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("0x Swap API • AllowanceHolder Flow", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BaseAmber.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.Top) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BaseAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Never approve the 0x Settler contract directly. Approve ONLY the AllowanceHolder or Permit2 address returned in quote.issues.allowance.spender.",
                                fontSize = 10.sp,
                                color = BaseAmber,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF030712),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = viem0xSnippet,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = BaseCyan
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(viem0xSnippet))
                        show0xCodeModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Integration Code", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { show0xCodeModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}

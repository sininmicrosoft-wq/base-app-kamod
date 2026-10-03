package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.BaseViewModel
import com.example.ui.components.BaseBrandHeader
import com.example.ui.components.NetworkSelectorBottomSheet
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DeFiScreen
import com.example.ui.screens.PaymentsScreen
import com.example.ui.screens.PrivateLedgerScreen
import com.example.ui.screens.RpcPlaygroundScreen
import com.example.ui.screens.RwaScreen
import com.example.ui.screens.StablecoinScreen
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BaseApp()
            }
        }
    }
}

@Composable
fun BaseApp(viewModel: BaseViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf("dashboard") }
    var showNetworkSheet by remember { mutableStateOf(false) }

    val network by viewModel.selectedNetwork.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()

    // Back handling for sub-screens to return to Dashboard
    if (currentScreen != "dashboard") {
        BackHandler {
            currentScreen = "dashboard"
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            BaseBrandHeader(
                currentNetwork = network,
                blockNumber = telemetry.blockNumber,
                isLive = telemetry.isLive,
                onNetworkClick = { showNetworkSheet = true }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("bottom_nav")
            ) {
                val navItems = listOf(
                    Triple("dashboard", "Hub", Icons.Default.Home),
                    Triple("payments", "Pay", Icons.Default.CreditCard),
                    Triple("defi", "DeFi", Icons.Default.CurrencyExchange),
                    Triple("rwa", "RWA", Icons.Default.MonetizationOn),
                    Triple("stablecoins", "Coins", Icons.Default.Paid)
                )

                navItems.forEach { (route, label, icon) ->
                    val isSelected = currentScreen == route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = route },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = BaseCyan,
                            indicatorColor = BaseBlue,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_item_$route")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "dashboard" -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToSolution = { target -> currentScreen = target }
                    )
                }
                "payments" -> {
                    PaymentsScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
                "defi" -> {
                    DeFiScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
                "rwa" -> {
                    RwaScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
                "stablecoins" -> {
                    StablecoinScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
                "private" -> {
                    PrivateLedgerScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
                "rpc" -> {
                    RpcPlaygroundScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = "dashboard" }
                    )
                }
            }
        }
    }

    if (showNetworkSheet) {
        NetworkSelectorBottomSheet(
            currentNetwork = network,
            onSelectNetwork = { chosenNet -> viewModel.setNetwork(chosenNet) },
            onDismiss = { showNetworkSheet = false }
        )
    }
}

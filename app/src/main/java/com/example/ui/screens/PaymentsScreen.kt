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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.data.model.InvoiceStatus
import com.example.data.model.PaymentInvoice
import com.example.ui.BaseViewModel
import com.example.ui.components.BaseQrCanvas
import com.example.ui.components.StatusChip
import com.example.ui.theme.BaseAmber
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.BaseRed
import com.example.ui.theme.BaseTeal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    viewModel: BaseViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedQrInvoice by remember { mutableStateOf<PaymentInvoice?>(null) }
    var showReconciliationBanner by remember { mutableStateOf(false) }

    val totalInvoiced = invoices.sumOf { it.amount }
    val totalCaptured = invoices.filter { it.status == InvoiceStatus.CAPTURED || it.status == InvoiceStatus.VERIFIED || it.status == InvoiceStatus.RECONCILED }.sumOf { it.amount }
    val totalRefunded = invoices.filter { it.status == InvoiceStatus.REFUNDED }.sumOf { it.amount }

    Scaffold(
        modifier = modifier.testTag("payments_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = BaseBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("create_invoice_fab")
            ) {
                Row(modifier = Modifier.padding(horizontal = 14.dp)) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "New Invoice")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Payment", fontWeight = FontWeight.Bold)
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
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = "Accept Payments",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Full Base Onchain Payment Lifecycle",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Lifecycle Visual Stepper
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().testTag("lifecycle_stepper_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Payment Lifecycle Stages",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val steps = listOf(
                                "1. Request" to BaseCyan,
                                "2. Authorize" to BaseAmber,
                                "3. Capture" to BaseTeal,
                                "4. Verify" to BaseBlue,
                                "5. Refund" to BaseRed,
                                "6. Reconcile" to BaseCyan,
                                "7. Pay Out" to BaseTeal
                            )
                            items(steps) { (title, color) ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = color.copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Volume Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricMiniCard(
                        title = "Invoiced",
                        value = "$${String.format("%,.0f", totalInvoiced)}",
                        color = BaseCyan,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Captured",
                        value = "$${String.format("%,.0f", totalCaptured)}",
                        color = BaseTeal,
                        modifier = Modifier.weight(1f)
                    )
                    MetricMiniCard(
                        title = "Refunded",
                        value = "$${String.format("%,.0f", totalRefunded)}",
                        color = BaseRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Payout / Sweep Button
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BaseBlue.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Merchant Auto-Sweep & Reconcile",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Instantly sweep $${String.format("%,.2f", totalCaptured)} to merchant treasury",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {
                                showReconciliationBanner = true
                                viewModel.invoices.value.filter { it.status == InvoiceStatus.CAPTURED }.forEach {
                                    viewModel.advanceInvoice(it, InvoiceStatus.RECONCILED)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                            modifier = Modifier.testTag("sweep_funds_btn")
                        ) {
                            Text("Reconcile", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (showReconciliationBanner) {
                item {
                    Surface(
                        color = BaseTeal.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("reconciliation_success")
                    ) {
                        Text(
                            text = "✓ Merchant ledger updated. Captured funds reconciled on Base.",
                            fontSize = 12.sp,
                            color = BaseTeal,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Payment Invoices (${invoices.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (invoices.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Invoices Yet", fontWeight = FontWeight.Bold)
                            Text("Create an onchain payment request to begin the lifecycle", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            } else {
                items(invoices) { invoice ->
                    InvoiceItemCard(
                        invoice = invoice,
                        onShowQr = { selectedQrInvoice = invoice },
                        onAdvance = { nextStatus -> viewModel.advanceInvoice(invoice, nextStatus) },
                        onDelete = { viewModel.deleteInvoice(invoice) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(60.dp)) }
        }
    }

    // Create Invoice Dialog
    if (showCreateDialog) {
        CreateInvoiceDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { customer, amount, currency, memo ->
                viewModel.createInvoice(customer, amount, currency, memo)
                showCreateDialog = false
            }
        )
    }

    // QR Payment Viewer Modal
    selectedQrInvoice?.let { invoice ->
        val paymentUri = "base://pay?address=${viewModel.smartWalletAddress}&amount=${invoice.amount}&token=${invoice.currency}&id=${invoice.invoiceId}"
        AlertDialog(
            onDismissRequest = { selectedQrInvoice = null },
            confirmButton = {
                TextButton(onClick = { selectedQrInvoice = null }) {
                    Text("Close")
                }
            },
            title = {
                Text("Base Pay QR Code", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${invoice.customerName} • $${invoice.amount} ${invoice.currency}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    BaseQrCanvas(payload = paymentUri, sizeDp = 200)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Scan with any Base-compatible wallet",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Zero gas fees sponsored by Base Paymaster",
                        fontSize = 11.sp,
                        color = BaseCyan,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}

@Composable
fun InvoiceItemCard(
    invoice: PaymentInvoice,
    onShowQr: () -> Unit,
    onAdvance: (InvoiceStatus) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.fillMaxWidth().testTag("invoice_card_${invoice.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = invoice.customerName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = invoice.invoiceId,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(status = invoice.status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$${String.format("%.2f", invoice.amount)} ${invoice.currency}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (invoice.memo.isNotEmpty()) {
                        Text(
                            text = invoice.memo,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onShowQr, modifier = Modifier.testTag("qr_btn_${invoice.id}")) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = "View QR", tint = BaseCyan)
                }
            }

            if (invoice.txHash.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Tx: ${invoice.txHash.take(20)}...",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Triggers based on Lifecycle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (invoice.status) {
                    InvoiceStatus.REQUESTED -> {
                        Button(
                            onClick = { onAdvance(InvoiceStatus.AUTHORIZED) },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseAmber),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_authorize_${invoice.id}")
                        ) {
                            Text("2. Authorize Gasless", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    InvoiceStatus.AUTHORIZED -> {
                        Button(
                            onClick = { onAdvance(InvoiceStatus.CAPTURED) },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseTeal),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_capture_${invoice.id}")
                        ) {
                            Text("3. Capture Onchain", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    InvoiceStatus.CAPTURED -> {
                        Button(
                            onClick = { onAdvance(InvoiceStatus.VERIFIED) },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_verify_${invoice.id}")
                        ) {
                            Text("4. Verify Receipt", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onAdvance(InvoiceStatus.REFUNDED) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("action_refund_${invoice.id}")
                        ) {
                            Text("Refund", fontSize = 11.sp, color = BaseRed)
                        }
                    }
                    InvoiceStatus.VERIFIED -> {
                        Button(
                            onClick = { onAdvance(InvoiceStatus.RECONCILED) },
                            colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("action_reconcile_${invoice.id}")
                        ) {
                            Text("5. Reconcile Payout", fontSize = 11.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                    InvoiceStatus.REFUNDED -> {
                        Text(
                            text = "Refund settled via reversal contract",
                            fontSize = 11.sp,
                            color = BaseRed,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    InvoiceStatus.RECONCILED -> {
                        Text(
                            text = "✓ Fully settled & reconciled into merchant ledger",
                            fontSize = 11.sp,
                            color = BaseTeal,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MetricMiniCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun CreateInvoiceDialog(
    onDismiss: () -> Unit,
    onCreate: (customer: String, amount: Double, currency: String, memo: String) -> Unit
) {
    var customer by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf("USDC") }
    var memo by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Payment Request", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = customer,
                    onValueChange = { customer = it },
                    label = { Text("Customer / Organization") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_customer")
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_amount")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("USDC", "ETH", "cbETH", "EURC").forEach { curr ->
                        val isSel = curr == currency
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) BaseBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { currency = curr }
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = curr,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("Memo / Invoice Note") },
                    modifier = Modifier.fillMaxWidth().testTag("input_memo")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 10.0
                    onCreate(customer.ifEmpty { "Client" }, amt, currency, memo)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                modifier = Modifier.testTag("submit_create_invoice_btn")
            ) {
                Text("Generate Request")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

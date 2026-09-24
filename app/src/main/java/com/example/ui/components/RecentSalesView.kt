package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.TransactionLog
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecentSalesDialog(
    recentSales: List<TransactionLog>,
    onDismiss: () -> Unit,
    onExportClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = modifier
            .fillMaxWidth(0.92f)
            .fillMaxHeight(0.85f),
        text = {
            RecentSalesContent(
                recentSales = recentSales,
                onClose = onDismiss,
                onExportClick = onExportClick
            )
        },
        confirmButton = {}
    )
}

@Composable
fun RecentSalesContent(
    recentSales: List<TransactionLog>,
    onClose: (() -> Unit)? = null,
    onExportClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val timeFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }

    val filteredSales = remember(recentSales, searchQuery) {
        if (searchQuery.isBlank()) {
            recentSales
        } else {
            val q = searchQuery.trim().lowercase()
            recentSales.filter { log ->
                log.orderId.lowercase().contains(q) ||
                    log.customerName.lowercase().contains(q) ||
                    log.cashierName.lowercase().contains(q) ||
                    log.paymentMethod.lowercase().contains(q) ||
                    log.itemsSummary.lowercase().contains(q)
            }
        }
    }

    val totalRecentSalesAmount = remember(recentSales) {
        recentSales.sumOf { it.grandTotal }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CrispWhite, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .testTag("recent_sales_view")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = "Recent Sales",
                        tint = DeepRoyalBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Recent Sales (Penjualan Terakhir)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    Text(
                        text = "Log transaksi pesanan yang telah selesai",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onExportClick != null) {
                    Button(
                        onClick = onExportClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("recent_sales_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ekspor CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }

                if (onClose != null) {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Summary Metric Badges
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Total Transaksi Selesai", fontSize = 11.sp, color = Color(0xFF64748B))
                    Text(
                        text = "${recentSales.size} Penjualan",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text("Total Omset Tercatat", fontSize = 11.sp, color = DeepRoyalBlue)
                    Text(
                        text = CurrencyFormatter.formatRupiah(totalRecentSalesAmount),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = DeepRoyalBlue
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cari nomor nota, pelanggan, atau metode...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("recent_sales_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Transaction List
        if (filteredSales.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(SoftGrayBg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "Tidak ada transaksi yang cocok" else "Belum ada transaksi penjualan yang tercatat",
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredSales, key = { it.id }) { log ->
                    RecentSaleItemCard(
                        log = log,
                        formattedDate = timeFormat.format(Date(log.timestamp))
                    )
                }
            }
        }

        if (onClose != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Text("Tutup", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun RecentSaleItemCard(
    log: TransactionLog,
    formattedDate: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { expanded = !expanded }
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .testTag("recent_sale_item_${log.orderId}"),
        colors = CardDefaults.cardColors(containerColor = CrispWhite)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Row 1: Order ID, Status, and Grand Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFEFF6FF),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#${log.orderId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepRoyalBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Selesai",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }

                Text(
                    text = CurrencyFormatter.formatRupiah(log.grandTotal),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepRoyalBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Row 2: Customer, Cashier, Timestamp, and Payment Method
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${log.customerName} • Kasir: ${log.cashierName}",
                        fontSize = 11.sp,
                        color = Color(0xFF475569),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = log.paymentMethod,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Summary of items
            if (log.itemsSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.itemsSummary,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = if (expanded) Int.MAX_VALUE else 1
                )
            }

            // Expandable breakdown
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Divider(color = Color(0xFFE2E8F0))
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Subtotal", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(CurrencyFormatter.formatRupiah(log.subtotal), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                    if (log.taxAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("PPN (Pajak)", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(CurrencyFormatter.formatRupiah(log.taxAmount), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (log.discountAmount > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Diskon", fontSize = 11.sp, color = EmeraldGreen)
                            Text("-${CurrencyFormatter.formatRupiah(log.discountAmount)}", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (log.cashReceived > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Tunai Diterima / Kembalian", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text("${CurrencyFormatter.formatRupiah(log.cashReceived)} / ${CurrencyFormatter.formatRupiah(log.changeGiven)}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    if (log.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Catatan: ${log.notes}",
                            fontSize = 11.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }
            }
        }
    }
}

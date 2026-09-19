package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.PurchaseOrder
import com.example.ui.PosViewModel
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.CurrencyFormatter

@Composable
fun InventoryScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val adjustments by viewModel.adjustments.collectAsStateWithLifecycle()
    val purchaseOrders by viewModel.purchaseOrders.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Stok & Opname, 1: Purchase Orders, 2: Riwayat Audit

    var productForAdjustment by remember { mutableStateOf<Product?>(null) }
    var showCreatePODialog by remember { mutableStateOf(false) }

    val lowStockItems = remember(products) { products.filter { it.isLowStock } }

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Manajemen Stok & Opname", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                    Text("Pengawasan real-time inventaris & purchase order", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                if (activeTab == 1) {
                    Button(
                        onClick = { showCreatePODialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Buat PO", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Low Stock Warning Alert Banner
            if (lowStockItems.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Peringatan: ${lowStockItems.size} Produk Mencapai Batas Minimum!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                            Text(
                                text = lowStockItems.joinToString(", ") { "${it.name} (${it.stock})" },
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                                maxLines = 1
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Tabs
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = CrispWhite,
                contentColor = DeepRoyalBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = DeepRoyalBlue
                    )
                }
            ) {
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Stok & Opname", fontSize = 12.sp) })
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Purchase Order (${purchaseOrders.size})", fontSize = 12.sp) })
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Riwayat Audit (${adjustments.size})", fontSize = 12.sp) })
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeTab) {
                0 -> {
                    // Stock list with Opname button
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(products) { prod ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                        Text("SKU: ${prod.sku} • Min: ${prod.minStockAlert} ${prod.unit}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text(
                                            text = if (prod.isOutOfStock) "Stok Habis" else if (prod.isLowStock) "Stok Kritis: ${prod.stock} ${prod.unit}" else "Stok Tersedia: ${prod.stock} ${prod.unit}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (prod.isLowStock) Color(0xFFDC2626) else EmeraldDark
                                        )
                                    }

                                    Button(
                                        onClick = { productForAdjustment = prod },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Opname", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Purchase Orders
                    if (purchaseOrders.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada Purchase Order.", color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(purchaseOrders) { po ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(po.poNumber, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                                Text("Supplier: ${po.supplierName}", fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        if (po.status == "RECEIVED") EmeraldGreen.copy(alpha = 0.15f) else Color(0xFFFEF3C7),
                                                        RoundedCornerShape(4.dp)
                                                    )
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = if (po.status == "RECEIVED") "Sudah Diterima" else "Pending Delivery",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (po.status == "RECEIVED") EmeraldDark else Color(0xFFB45309)
                                                )
                                            }
                                        }

                                        Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 6.dp))

                                        Text("Item: ${po.itemsSummary}", fontSize = 12.sp, color = DarkSlate)
                                        Text("Total Biaya: ${CurrencyFormatter.formatRupiah(po.totalCost)}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepRoyalBlue)

                                        if (po.status == "ORDERED") {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Button(
                                                onClick = { viewModel.markPOReceived(po) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Tandai Barang Masuk Diterima", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Stock Opname Audit Logs
                    if (adjustments.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Belum ada riwayat penyesuaian stok.", color = Color(0xFF64748B))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(adjustments) { adj ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(adj.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            val isPlus = adj.qtyChange > 0
                                            Text(
                                                text = (if (isPlus) "+" else "") + "${adj.qtyChange}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isPlus) EmeraldDark else Color(0xFFDC2626)
                                            )
                                        }
                                        Text("Tipe: ${adj.type} • Oleh: ${adj.staffName}", fontSize = 11.sp, color = Color(0xFF64748B))
                                        Text("Sebelum: ${adj.previousStock} -> Sesudah: ${adj.newStock}", fontSize = 11.sp, color = DarkSlate)
                                        if (adj.reason.isNotBlank()) {
                                            Text("Catatan: ${adj.reason}", fontSize = 10.sp, color = Color(0xFF94A3B8))
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

    // STOCK ADJUSTMENT MODAL
    productForAdjustment?.let { prod ->
        var adjustmentType by remember { mutableStateOf("KOREKSI_OPNAME") }
        var qtyChangeInput by remember { mutableStateOf("5") }
        var isAddition by remember { mutableStateOf(true) }
        var reason by remember { mutableStateOf("Stock opname mingguan") }

        AlertDialog(
            onDismissRequest = { productForAdjustment = null },
            confirmButton = {
                Button(
                    onClick = {
                        val num = qtyChangeInput.toIntOrNull() ?: 0
                        val finalChange = if (isAddition) num else -num
                        viewModel.performStockAdjustment(
                            productId = prod.id,
                            productName = prod.name,
                            type = adjustmentType,
                            qtyChange = finalChange,
                            reason = reason
                        )
                        productForAdjustment = null
                    },
                    enabled = (qtyChangeInput.toIntOrNull() ?: 0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue)
                ) {
                    Text("Terapkan Koreksi Stok")
                }
            },
            dismissButton = {
                Button(onClick = { productForAdjustment = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text("Stock Opname: ${prod.name}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    Text("Stok fisik saat ini di sistem: ${prod.stock} ${prod.unit}", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { isAddition = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isAddition) EmeraldGreen else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Tambah (+)", color = if (isAddition) CrispWhite else DarkSlate, fontSize = 12.sp)
                        }
                        Button(
                            onClick = { isAddition = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isAddition) Color(0xFFDC2626) else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Kurangi (-)", color = if (!isAddition) CrispWhite else DarkSlate, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = qtyChangeInput,
                        onValueChange = { qtyChangeInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Jumlah Perubahan Qty") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Alasan (Misal: Kerusakan/Selisih Fisik)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // CREATE PURCHASE ORDER DIALOG
    if (showCreatePODialog) {
        var poNumber by remember { mutableStateOf("PO-" + (1000..9999).random()) }
        var supplier by remember { mutableStateOf("PT Sumber Pangan Sejahtera") }
        var costInput by remember { mutableStateOf("500000") }
        var summary by remember { mutableStateOf("Restock Kopi Robusta & Sirup") }
        var notes by remember { mutableStateOf("Pengiriman H+2") }

        AlertDialog(
            onDismissRequest = { showCreatePODialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val cost = costInput.toDoubleOrNull() ?: 0.0
                        viewModel.createPurchaseOrder(poNumber.trim(), supplier.trim(), cost, summary.trim(), notes.trim())
                        showCreatePODialog = false
                    },
                    enabled = poNumber.isNotBlank() && supplier.isNotBlank() && (costInput.toDoubleOrNull() ?: 0.0) > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue)
                ) {
                    Text("Buat PO Baru")
                }
            },
            dismissButton = {
                Button(onClick = { showCreatePODialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text("Buat Purchase Order Baru", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(value = poNumber, onValueChange = { poNumber = it }, label = { Text("Nomor PO") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = supplier, onValueChange = { supplier = it }, label = { Text("Nama Supplier / Distributor") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = costInput, onValueChange = { costInput = it.filter { ch -> ch.isDigit() } }, label = { Text("Estimasi Total Biaya (Rp)") }, prefix = { Text("Rp ") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Ringkasan Item") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }
}

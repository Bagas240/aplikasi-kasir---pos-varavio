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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PaymentMethod
import com.example.data.model.StoreProfile
import com.example.data.model.UserRole
import com.example.data.repository.PosRepository
import com.example.ui.PosViewModel
import com.example.ui.components.RecentSalesContent
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.CurrencyFormatter

@Composable
fun AnalyticsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val completedOrders by viewModel.completedOrders.collectAsStateWithLifecycle()
    val recentSales by viewModel.recentSales.collectAsStateWithLifecycle()
    val products by viewModel.products.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Ringkasan Bisnis, 1: Riwayat Transaksi, 2: Profil & Printer

    val totalGrossSales = remember(completedOrders) {
        completedOrders.sumOf { it.grandTotal }
    }

    // Compute Net Profit
    val totalCostOfGoods = remember(completedOrders, products) {
        var cost = 0.0
        for (order in completedOrders) {
            val items = PosRepository.deserializeItems(order.itemsJson, products)
            for (item in items) {
                cost += (item.product.buyPrice * item.quantity)
            }
        }
        cost
    }
    val netProfit = (totalGrossSales - totalCostOfGoods).coerceAtLeast(0.0)

    val avgBasket = if (completedOrders.isNotEmpty()) totalGrossSales / completedOrders.size else 0.0

    // Compute Top Selling Items
    val topItems = remember(completedOrders, products) {
        val countMap = mutableMapOf<String, Int>()
        for (order in completedOrders) {
            val items = PosRepository.deserializeItems(order.itemsJson, products)
            for (item in items) {
                val name = item.product.name
                countMap[name] = (countMap[name] ?: 0) + item.quantity
            }
        }
        countMap.toList().sortedByDescending { it.second }.take(5)
    }

    val maxItemSales = topItems.firstOrNull()?.second ?: 1

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Laporan Keuangan & Toko", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                    Text("Analitik omset, laba rugi & preferensi struk", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = DeepRoyalBlue)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Ringkasan", fontSize = 12.sp) })
                Tab(selected = activeTab == 1, onClick = { activeTab = 1 }, text = { Text("Transaksi (${completedOrders.size})", fontSize = 12.sp) })
                Tab(selected = activeTab == 2, onClick = { activeTab = 2 }, text = { Text("Pengaturan Toko", fontSize = 12.sp) })
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeTab) {
                0 -> {
                    // Summary dashboard
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 2x2 Metric Cards
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrispWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Omset Penjualan", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(totalGrossSales),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DeepRoyalBlue
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrispWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Laba Bersih (Profit)", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = if (currentUser.role == UserRole.OWNER) CurrencyFormatter.formatRupiah(netProfit) else "Terkunci (Owner)",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldDark
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrispWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Total Transaksi", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text("${completedOrders.size} Order", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                }
                            }

                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrispWhite)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Rata-rata Belanja", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text(CurrencyFormatter.formatRupiah(avgBasket), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Top 5 Products Bar Visualizer
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = CrispWhite)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Top 5 Produk Terlaris:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                Spacer(modifier = Modifier.height(8.dp))

                                if (topItems.isEmpty()) {
                                    Text("Belum ada data penjualan tercatat.", fontSize = 12.sp, color = Color(0xFF64748B))
                                } else {
                                    topItems.forEach { (name, qty) ->
                                        val progress = qty.toFloat() / maxItemSales.toFloat()
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkSlate)
                                                Text("$qty Pcs Terjual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DeepRoyalBlue)
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            LinearProgressIndicator(
                                                progress = { progress },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(6.dp)
                                                    .clip(RoundedCornerShape(3.dp)),
                                                color = DeepRoyalBlue,
                                                trackColor = Color(0xFFE2E8F0)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Payment Methods Distribution
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = CrispWhite)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Sebaran Metode Pembayaran:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                Spacer(modifier = Modifier.height(6.dp))

                                val methodCounts = completedOrders.groupBy { it.paymentMethod }
                                if (methodCounts.isEmpty()) {
                                    Text("Belum ada transaksi.", fontSize = 12.sp, color = Color(0xFF64748B))
                                } else {
                                    methodCounts.forEach { (method, list) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(method, fontSize = 12.sp, color = DarkSlate)
                                            Text(
                                                "${list.size}x (${CurrencyFormatter.formatRupiah(list.sumOf { it.grandTotal })})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldDark
                                            )
                                        }
                                        Divider(color = Color(0xFFF1F5F9))
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Recent Sales & Completed Orders Review
                    RecentSalesContent(
                        recentSales = recentSales,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                2 -> {
                    // Store & Printer Settings Form
                    var storeName by remember { mutableStateOf(storeProfile.storeName) }
                    var address by remember { mutableStateOf(storeProfile.address) }
                    var phone by remember { mutableStateOf(storeProfile.phone) }
                    var instagram by remember { mutableStateOf(storeProfile.instagram) }
                    var taxPct by remember { mutableStateOf(storeProfile.taxPercent.toInt().toString()) }
                    var servicePct by remember { mutableStateOf(storeProfile.servicePercent.toInt().toString()) }
                    var footer by remember { mutableStateOf(storeProfile.receiptFooter) }
                    var paperWidth by remember { mutableStateOf(storeProfile.printerPaperWidth) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                            colors = CardDefaults.cardColors(containerColor = CrispWhite)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Pengaturan Header Struk & Toko:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(value = storeName, onValueChange = { storeName = it }, label = { Text("Nama Toko / Brand") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat Toko") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("No. Telepon") }, singleLine = true, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = instagram, onValueChange = { instagram = it }, label = { Text("Instagram / Medsos") }, singleLine = true, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedTextField(value = taxPct, onValueChange = { taxPct = it.filter { ch -> ch.isDigit() } }, label = { Text("PPN (%)") }, suffix = { Text("%") }, singleLine = true, modifier = Modifier.weight(1f))
                                    OutlinedTextField(value = servicePct, onValueChange = { servicePct = it.filter { ch -> ch.isDigit() } }, label = { Text("Service Charge (%)") }, suffix = { Text("%") }, singleLine = true, modifier = Modifier.weight(1f))
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(value = footer, onValueChange = { footer = it }, label = { Text("Catatan Kaki Struk (Footer)") }, maxLines = 2, modifier = Modifier.fillMaxWidth())

                                Spacer(modifier = Modifier.height(10.dp))
                                Text("Lebar Kertas Thermal Printer Default:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = DarkSlate)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("58mm", "80mm").forEach { widthOpt ->
                                        val isSel = paperWidth == widthOpt
                                        Button(
                                            onClick = { paperWidth = widthOpt },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSel) DeepRoyalBlue else Color(0xFFF1F5F9)
                                            ),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(widthOpt, color = if (isSel) CrispWhite else DarkSlate, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.updateStoreProfile(
                                            StoreProfile(
                                                storeName = storeName,
                                                address = address,
                                                phone = phone,
                                                instagram = instagram,
                                                taxPercent = taxPct.toDoubleOrNull() ?: 11.0,
                                                servicePercent = servicePct.toDoubleOrNull() ?: 0.0,
                                                receiptFooter = footer,
                                                printerPaperWidth = paperWidth
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Simpan Pengaturan Toko", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

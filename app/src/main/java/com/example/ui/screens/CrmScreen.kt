package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Customer
import com.example.data.model.CustomerDebt
import com.example.ui.PosViewModel
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.CurrencyFormatter

@Composable
fun CrmScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val debts by viewModel.debts.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Pelanggan, 1: Piutang / Bon
    var searchQuery by remember { mutableStateOf("") }
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    val unpaidDebts = remember(debts) { debts.filter { it.status == "UNPAID" } }
    val totalUnpaidAmount = remember(unpaidDebts) { unpaidDebts.sumOf { it.amount } }
    val totalPoints = remember(customers) { customers.sumOf { it.points } }

    val filteredCustomers = remember(customers, searchQuery) {
        if (searchQuery.isBlank()) customers
        else {
            val q = searchQuery.trim().lowercase()
            customers.filter {
                it.name.lowercase().contains(q) || it.phone.contains(q) || it.email.lowercase().contains(q)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Manajemen Pelanggan & CRM", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                    Text("Database pelanggan, loyalty poin, dan catatan piutang kasir", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Button(
                    onClick = { showAddCustomerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("crm_add_customer_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Pelanggan", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Stats Metric Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Card 1: Total Pelanggan
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(24.dp).background(DeepRoyalBlue.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pelanggan", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${customers.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    }
                }

                // Card 2: Total Poin
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(24.dp).background(Color(0xFFFEF3C7), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Total Poin", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("$totalPoints", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                    }
                }

                // Card 3: Total Piutang
                Card(
                    modifier = Modifier.weight(1.2f),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(24.dp).background(Color(0xFFFEE2E2), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Kas Bon Aktif", fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(CurrencyFormatter.formatRupiah(totalUnpaidAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Tabs
            Card(
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
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
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Text(
                                "Daftar Pelanggan (${customers.size})",
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Text(
                                "Piutang / Kas Bon (${unpaidDebts.size})",
                                fontSize = 12.sp,
                                fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Medium,
                                color = if (unpaidDebts.isNotEmpty()) Color(0xFFDC2626) else DeepRoyalBlue
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeTab) {
                0 -> {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari nama atau no. telepon pelanggan...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepRoyalBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = CrispWhite,
                            unfocusedContainerColor = CrispWhite
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (filteredCustomers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.People, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Tidak ada pelanggan cocok dengan pencarian" else "Belum ada pelanggan terdaftar",
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { showAddCustomerDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("+ Tambah Pelanggan Baru")
                                }
                            }
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                            items(filteredCustomers) { customer ->
                                val initials = customer.name.split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercase() }
                                    .joinToString("")
                                    .ifBlank { "PL" }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                // Initials Avatar
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(DeepRoyalBlue.copy(alpha = 0.12f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = initials,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp,
                                                        color = DeepRoyalBlue
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(10.dp))

                                                Column {
                                                    Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                                    Text(
                                                        text = if (customer.phone.isNotBlank()) customer.phone else "Tanpa No. HP",
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }

                                            // Action buttons: WhatsApp / Call, Edit, Delete
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (customer.phone.isNotBlank()) {
                                                    IconButton(
                                                        onClick = {
                                                            try {
                                                                val cleanPhone = customer.phone.filter { it.isDigit() }
                                                                val formatted = if (cleanPhone.startsWith("0")) "62" + cleanPhone.substring(1) else cleanPhone
                                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=$formatted"))
                                                                context.startActivity(intent)
                                                            } catch (e: Exception) {
                                                                Toast.makeText(context, "Tidak dapat membuka WhatsApp", Toast.LENGTH_SHORT).show()
                                                            }
                                                        },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Chat, contentDescription = "WhatsApp", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { customerToEdit = customer },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VibrantBlue, modifier = Modifier.size(18.dp))
                                                }

                                                IconButton(
                                                    onClick = { customerToDelete = customer },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }

                                        HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                                        // Stats Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Loyalty Points Chip
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("${customer.points} Poin", color = Color(0xFF92400E), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }

                                            // Total Spend Lifetime
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text("Total Belanja", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text(CurrencyFormatter.formatRupiah(customer.totalSpend), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSlate)
                                            }

                                            // Debt Balance
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Saldo Bon / Piutang", fontSize = 10.sp, color = Color(0xFF64748B))
                                                Text(
                                                    text = CurrencyFormatter.formatRupiah(customer.debtBalance),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = if (customer.debtBalance > 0) Color(0xFFDC2626) else EmeraldDark
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Unpaid Debts (Buku Piutang)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Total Debt Summary Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Piutang Belum Terbayar", fontSize = 12.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(totalUnpaidAmount),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFFEE2E2), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("${unpaidDebts.size} Transaksi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (unpaidDebts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Semua piutang pelanggan sudah lunas!", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Tidak ada tagihan atau kas bon yang tertunggak.", color = Color(0xFF64748B), fontSize = 12.sp)
                                }
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(unpaidDebts) { debt ->
                                    val customer = customers.find { it.id == debt.customerId }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(debt.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                                    Text("Order ID: #${debt.orderId} • ${CurrencyFormatter.formatDateShort(debt.createdAt)}", fontSize = 11.sp, color = Color(0xFF64748B))
                                                }
                                                Text(
                                                    text = CurrencyFormatter.formatRupiah(debt.amount),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp,
                                                    color = Color(0xFFDC2626)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Jatuh Tempo: ${CurrencyFormatter.formatDateShort(debt.dueDate)}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.Medium
                                            )

                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = {
                                                    if (customer != null) {
                                                        viewModel.payDebt(debt, customer)
                                                        Toast.makeText(context, "Pelunasan piutang berhasil dicatat!", Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Catat Pelunasan Piutang", fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
    }

    // MODAL: ADD / EDIT CUSTOMER
    if (showAddCustomerDialog || customerToEdit != null) {
        val editing = customerToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var phone by remember { mutableStateOf(editing?.phone ?: "") }
        var email by remember { mutableStateOf(editing?.email ?: "") }
        var address by remember { mutableStateOf(editing?.address ?: "") }
        var formError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = {
                showAddCustomerDialog = false
                customerToEdit = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedName = name.trim()
                        val trimmedPhone = phone.trim()
                        if (trimmedName.isBlank()) {
                            formError = "Nama pelanggan wajib diisi."
                            return@Button
                        }
                        if (trimmedPhone.isBlank()) {
                            formError = "Nomor telepon / WhatsApp wajib diisi."
                            return@Button
                        }
                        val cust = Customer(
                            id = editing?.id ?: 0L,
                            name = trimmedName,
                            phone = trimmedPhone,
                            email = email.trim(),
                            address = address.trim(),
                            points = editing?.points ?: 0,
                            debtBalance = editing?.debtBalance ?: 0.0,
                            totalSpend = editing?.totalSpend ?: 0.0
                        )
                        viewModel.saveCustomer(cust)
                        Toast.makeText(context, "Data pelanggan '${cust.name}' berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        showAddCustomerDialog = false
                        customerToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (editing == null) "Tambah Pelanggan" else "Simpan Perubahan", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddCustomerDialog = false
                    customerToEdit = null
                }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(36.dp).background(DeepRoyalBlue.copy(alpha = 0.12f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = DeepRoyalBlue)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (editing == null) "Tambah Pelanggan Baru" else "Edit Data Pelanggan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkSlate
                    )
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            formError = null
                        },
                        label = { Text("Nama Lengkap") },
                        placeholder = { Text("Contoh: Ibu Rina / Pak Budi") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DeepRoyalBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = {
                            phone = it
                            formError = null
                        },
                        label = { Text("Nomor WhatsApp / HP") },
                        placeholder = { Text("Contoh: 08123456789") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DeepRoyalBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Opsional)") },
                        placeholder = { Text("Contoh: rina@gmail.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DeepRoyalBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat (Opsional)") },
                        placeholder = { Text("Alamat tempat tinggal / kantor") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = DeepRoyalBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (formError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(formError!!, color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }

    // CONFIRM DELETE CUSTOMER DIALOG
    customerToDelete?.let { cust ->
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCustomer(cust)
                        Toast.makeText(context, "Pelanggan '${cust.name}' telah dihapus", Toast.LENGTH_SHORT).show()
                        customerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus", color = CrispWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text("Hapus Pelanggan?", fontWeight = FontWeight.Bold, color = DarkSlate) },
            text = { Text("Apakah Anda yakin ingin menghapus '${cust.name}' dari database CRM toko?", color = DarkSlate) }
        )
    }
}

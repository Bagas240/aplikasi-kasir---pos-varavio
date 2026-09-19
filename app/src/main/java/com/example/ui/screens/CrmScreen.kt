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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.Customer
import com.example.data.model.CustomerDebt
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
fun CrmScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val debts by viewModel.debts.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0: Pelanggan & Loyalty, 1: Piutang / Kas Bon
    var showAddCustomerDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<Customer?>(null) }

    val unpaidDebts = remember(debts) { debts.filter { it.status == "UNPAID" } }
    val totalUnpaidAmount = remember(unpaidDebts) { unpaidDebts.sumOf { it.amount } }

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CRM & Loyalty Pelanggan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                    Text("Poin belanja, buku kas bon & piutang pelanggan", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Button(
                    onClick = { showAddCustomerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pelanggan", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Tab Row
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
                Tab(selected = activeTab == 0, onClick = { activeTab = 0 }, text = { Text("Pelanggan (${customers.size})", fontSize = 12.sp) })
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = {
                        Text(
                            text = "Piutang / Bon (${unpaidDebts.size})",
                            fontSize = 12.sp,
                            color = if (unpaidDebts.isNotEmpty()) Color(0xFFDC2626) else DeepRoyalBlue
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (activeTab) {
                0 -> {
                    // Customer Directory
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        items(customers) { customer ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color(0xFFEFF6FF), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Default.Person, contentDescription = null, tint = DeepRoyalBlue)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                                Text(customer.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                                            }
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Loyalty Points Badge
                                            Box(
                                                modifier = Modifier
                                                    .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(12.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("${customer.points} Poin", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                }
                                            }

                                            IconButton(onClick = { customerToEdit = customer }) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VibrantBlue, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }

                                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Total Belanja Lifetime", fontSize = 10.sp, color = Color(0xFF64748B))
                                            Text(CurrencyFormatter.formatRupiah(customer.totalSpend), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSlate)
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Saldo Piutang (Bon)", fontSize = 10.sp, color = Color(0xFF64748B))
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

                1 -> {
                    // Unpaid Debts (Buku Piutang)
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Total Debt Summary Banner
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Total Piutang Belum Terbayar", fontSize = 11.sp, color = Color(0xFF991B1B))
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(totalUnpaidAmount),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                                Text("${unpaidDebts.size} Transaksi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (unpaidDebts.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Alhamdulillah, semua piutang pelanggan sudah lunas!", color = EmeraldDark, fontWeight = FontWeight.Medium)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                                items(unpaidDebts) { debt ->
                                    val customer = customers.find { it.id == debt.customerId }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp)),
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
                                                    Text(debt.customerName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                                    Text("ID: #${debt.orderId} • ${CurrencyFormatter.formatDateShort(debt.createdAt)}", fontSize = 11.sp, color = Color(0xFF64748B))
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
                                                color = Color(0xFFB45309)
                                            )

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Button(
                                                onClick = {
                                                    if (customer != null) {
                                                        viewModel.payDebt(debt, customer)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(6.dp)
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

    // ADD / EDIT CUSTOMER MODAL
    if (showAddCustomerDialog || customerToEdit != null) {
        val editing = customerToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var phone by remember { mutableStateOf(editing?.phone ?: "") }
        var email by remember { mutableStateOf(editing?.email ?: "") }
        var address by remember { mutableStateOf(editing?.address ?: "") }

        AlertDialog(
            onDismissRequest = {
                showAddCustomerDialog = false
                customerToEdit = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cust = Customer(
                            id = editing?.id ?: 0L,
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            points = editing?.points ?: 0,
                            debtBalance = editing?.debtBalance ?: 0.0,
                            totalSpend = editing?.totalSpend ?: 0.0
                        )
                        viewModel.saveCustomer(cust)
                        showAddCustomerDialog = false
                        customerToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue)
                ) {
                    Text(if (editing == null) "Tambah Pelanggan" else "Simpan")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAddCustomerDialog = false
                    customerToEdit = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text(if (editing == null) "Tambah Pelanggan CRM Baru" else "Edit Data Pelanggan", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nama Lengkap") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Nomor WhatsApp / HP") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email (Opsional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Alamat") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        )
    }
}

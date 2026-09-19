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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import com.example.data.model.Shift
import com.example.ui.PosViewModel
import com.example.ui.components.CashInOutDialog
import com.example.ui.components.CloseShiftDialog
import com.example.ui.components.OpenShiftDialog
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.CurrencyFormatter

@Composable
fun ShiftReportsScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val activeShift = remember(shifts) { shifts.find { it.status == "OPEN" } }
    val pastShifts = remember(shifts) { shifts.filter { it.status == "CLOSED" } }

    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showCashInOutDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Shift Kasir & Cash Drawer", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                    Text("Rekonsiliasi uang fisik kasir & selisih laci", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if (activeShift != null) EmeraldGreen else Color(0xFF94A3B8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (activeShift != null) Icons.Default.LockOpen else Icons.Default.Lock,
                        contentDescription = null,
                        tint = CrispWhite,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Shift Card or Open Shift Action
            if (activeShift != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFBBF7D0), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(EmeraldGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SHIFT SEDANG AKTIF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark)
                                }
                                Text("Kasir: ${activeShift.cashierName}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkSlate)
                                Text("Mulai: ${CurrencyFormatter.formatTime(activeShift.startTime)}", fontSize = 11.sp, color = Color(0xFF64748B))
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("Estimasi Uang di Laci", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text(
                                    text = CurrencyFormatter.formatRupiah(activeShift.expectedCash),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepRoyalBlue
                                )
                            }
                        }

                        Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))

                        // Financial Grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Modal Awal", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(CurrencyFormatter.formatRupiah(activeShift.startingCash), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Column {
                                Text("Penjualan Tunai", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(CurrencyFormatter.formatRupiah(activeShift.cashSales), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldDark)
                            }
                            Column {
                                Text("Penjualan Digital", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(CurrencyFormatter.formatRupiah(activeShift.digitalSales), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = VibrantBlue)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("+ Kas Masuk", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(CurrencyFormatter.formatRupiah(activeShift.cashIn), fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("- Kas Keluar", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(CurrencyFormatter.formatRupiah(activeShift.cashOut), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Color(0xFFDC2626))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: Kas Masuk/Keluar & Tutup Shift
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { showCashInOutDialog = true },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kas Masuk/Keluar", fontSize = 11.sp)
                            }

                            Button(
                                onClick = { showCloseShiftDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tutup Kasir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Kasir Sedang Ditutup", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF991B1B))
                        Text("Buka shift kasir baru untuk mulai mencatat transaksi", fontSize = 12.sp, color = Color(0xFF7F1D1D))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { showOpenShiftDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Buka Shift Baru Sekarang", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Past Shifts Reconciliation History
            Text("Riwayat Rekonsiliasi Shift Kasir:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
            Spacer(modifier = Modifier.height(6.dp))

            if (pastShifts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat shift yang ditutup.", color = Color(0xFF64748B))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    items(pastShifts) { shift ->
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
                                        Text(shift.cashierName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                        Text(
                                            "${CurrencyFormatter.formatDate(shift.startTime)} s/d ${if (shift.endTime != null) CurrencyFormatter.formatTime(shift.endTime) else "-"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    // Selisih Badge
                                    val diff = shift.difference ?: 0.0
                                    val isMatch = diff == 0.0
                                    val isOver = diff > 0
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                if (isMatch) EmeraldGreen.copy(alpha = 0.15f) else if (isOver) Color(0xFFEFF6FF) else Color(0xFFFEE2E2),
                                                RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = if (isMatch) "Cocok (Pas)" else if (isOver) "Lebih: ${CurrencyFormatter.formatRupiah(diff)}" else "Kurang: ${CurrencyFormatter.formatRupiah(diff)}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMatch) EmeraldDark else if (isOver) DeepRoyalBlue else Color(0xFFDC2626)
                                        )
                                    }
                                }

                                Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 6.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Kas Seharusnya: ${CurrencyFormatter.formatRupiah(shift.expectedCash)}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    Text("Uang Fisik: ${CurrencyFormatter.formatRupiah(shift.actualCash ?: 0.0)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // DIALOGS
    if (showOpenShiftDialog) {
        OpenShiftDialog(
            cashierName = currentUser.name,
            onConfirm = { floatAmt ->
                viewModel.openShift(floatAmt)
                showOpenShiftDialog = false
            },
            onDismiss = { showOpenShiftDialog = false }
        )
    }

    if (showCloseShiftDialog && activeShift != null) {
        CloseShiftDialog(
            shift = activeShift,
            onConfirm = { actualCash, notes ->
                viewModel.closeShift(activeShift, actualCash, notes)
                showCloseShiftDialog = false
            },
            onDismiss = { showCloseShiftDialog = false }
        )
    }

    if (showCashInOutDialog) {
        CashInOutDialog(
            onConfirm = { amount, isCashIn, reason ->
                viewModel.logCashInOut(amount, isCashIn, reason)
                showCashInOutDialog = false
            },
            onDismiss = { showCashInOutDialog = false }
        )
    }
}

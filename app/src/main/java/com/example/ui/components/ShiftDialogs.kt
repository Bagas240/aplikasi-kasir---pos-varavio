package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.foundation.clickable
import com.example.data.model.Shift
import com.example.data.model.ShiftSchedule
import com.example.data.model.StaffUser
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyFormatter

@Composable
fun OpenShiftDialog(
    cashierName: String,
    staffUsers: List<StaffUser> = emptyList(),
    shiftSchedules: List<ShiftSchedule> = emptyList(),
    onConfirmShift: (startingFloat: Double, cashierName: String, scheduleName: String, scheduleTime: String) -> Unit,
    onDismiss: () -> Unit
) {
    val defaultSchedules = listOf(
        ShiftSchedule(name = "Shift 1 (Pagi)", startTime = "07:00", endTime = "15:00"),
        ShiftSchedule(name = "Shift 2 (Sore)", startTime = "15:00", endTime = "23:00"),
        ShiftSchedule(name = "Shift 3 (Malam)", startTime = "23:00", endTime = "07:00")
    )
    val effectiveSchedules = if (shiftSchedules.isNotEmpty()) shiftSchedules else defaultSchedules
    var selectedSchedule by remember { mutableStateOf(effectiveSchedules.first()) }

    var selectedCashier by remember { mutableStateOf(cashierName) }
    var floatInput by remember { mutableStateOf("200000") }
    val isValidFloat = (floatInput.toDoubleOrNull() ?: -1.0) >= 0.0 && floatInput.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amount = floatInput.toDoubleOrNull() ?: 0.0
                    onConfirmShift(
                        amount,
                        selectedCashier,
                        selectedSchedule.name,
                        "${selectedSchedule.startTime} - ${selectedSchedule.endTime}"
                    )
                },
                enabled = isValidFloat,
                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Buka Shift Sekarang", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Batal", color = Color(0xFF64748B))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(DeepRoyalBlue.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = DeepRoyalBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Buka Kasir / Open Shift", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkSlate)
                    Text("Pilih Shift & Petugas Kasir", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column {
                // 1. Pilih Shift
                Text("1. Pilih Jadwal Shift:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    effectiveSchedules.take(3).forEach { sched ->
                        val isSelected = sched.id == selectedSchedule.id || (sched.name == selectedSchedule.name)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DeepRoyalBlue else Color(0xFFF1F5F9))
                                .clickable { selectedSchedule = sched }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    sched.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CrispWhite else DarkSlate
                                )
                                Text(
                                    "${sched.startTime}-${sched.endTime}",
                                    fontSize = 9.sp,
                                    color = if (isSelected) CrispWhite.copy(alpha = 0.8f) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Pilih Kasir
                if (staffUsers.isNotEmpty()) {
                    Text("2. Kasir yang Bertugas:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        staffUsers.take(4).forEach { staff ->
                            val isSelected = staff.name == selectedCashier
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) DeepRoyalBlue else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .clickable { selectedCashier = staff.name }
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        staff.name.split(" ").firstOrNull() ?: staff.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) DeepRoyalBlue else DarkSlate
                                    )
                                    Text(
                                        staff.role.label,
                                        fontSize = 8.sp,
                                        color = if (isSelected) DeepRoyalBlue else Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // 3. Modal Kas Awal
                Text(
                    text = "3. Modal Kas Awal di Laci (Cash Float):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkSlate
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = floatInput,
                    onValueChange = { floatInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Modal Awal (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("100000", "200000", "500000").forEach { preset ->
                        Button(
                            onClick = { floatInput = preset },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(CurrencyFormatter.formatRupiah(preset.toDouble()), fontSize = 11.sp, color = DarkSlate)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun OpenShiftDialog(
    cashierName: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    OpenShiftDialog(
        cashierName = cashierName,
        staffUsers = emptyList(),
        shiftSchedules = emptyList(),
        onConfirmShift = { amount, _, _, _ -> onConfirm(amount) },
        onDismiss = onDismiss
    )
}

@Composable
fun CloseShiftDialog(
    shift: Shift,
    onConfirm: (actualCash: Double, notes: String) -> Unit,
    onDismiss: () -> Unit
) {
    var actualCashInput by remember { mutableStateOf(shift.expectedCash.toInt().toString()) }
    var notesInput by remember { mutableStateOf("") }

    val actualCash = actualCashInput.toDoubleOrNull() ?: 0.0
    val difference = actualCash - shift.expectedCash
    val isValidCash = (actualCashInput.toDoubleOrNull() ?: -1.0) >= 0.0 && actualCashInput.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onConfirm(actualCash, notesInput) },
                enabled = isValidCash,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Rekonsiliasi & Tutup Kasir", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("Kembali", color = Color(0xFF64748B))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFFEE2E2), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFDC2626))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Tutup Kasir / Close Shift", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkSlate)
                    Text("Kasir: ${shift.cashierName}", fontSize = 12.sp, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Modal Kas Awal:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(CurrencyFormatter.formatRupiah(shift.startingCash), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("+ Penjualan Tunai:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(CurrencyFormatter.formatRupiah(shift.cashSales), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("+ Kas Masuk:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(CurrencyFormatter.formatRupiah(shift.cashIn), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("- Kas Keluar:", fontSize = 12.sp, color = Color(0xFF64748B))
                            Text(CurrencyFormatter.formatRupiah(shift.cashOut), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        HorizontalDivider(color = Color(0xFFCBD5E1))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Kas Seharusnya:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepRoyalBlue)
                            Text(CurrencyFormatter.formatRupiah(shift.expectedCash), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = DeepRoyalBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = actualCashInput,
                    onValueChange = { actualCashInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Uang Fisik Kasir di Laci (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Variance Card
                val varColor = if (difference == 0.0) EmeraldGreen else if (difference > 0) Color(0xFF2563EB) else Color(0xFFDC2626)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(varColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            text = if (difference == 0.0) "Kas Pas (Cocok!)" else if (difference > 0) "Selisih Lebih:" else "Selisih Kurang:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = varColor
                        )
                        Text(
                            text = CurrencyFormatter.formatRupiah(difference),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = varColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Catatan Shift / Keterangan Selisih") },
                    placeholder = { Text("Contoh: Kas cocok, koin lengkap.") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        }
    )
}

@Composable
fun CashInOutDialog(
    onConfirm: (amount: Double, isCashIn: Boolean, reason: String) -> Unit,
    onDismiss: () -> Unit
) {
    var isCashIn by remember { mutableStateOf(true) }
    var amountInput by remember { mutableStateOf("") }
    var reasonInput by remember { mutableStateOf("") }
    val isValidCashInOut = (amountInput.toDoubleOrNull() ?: 0.0) > 0.0 && reasonInput.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && reasonInput.isNotBlank()) {
                        onConfirm(amt, isCashIn, reasonInput)
                    }
                },
                enabled = isValidCashInOut,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCashIn) EmeraldGreen else Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isCashIn) "Simpan Kas Masuk" else "Simpan Kas Keluar", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                Text("Batal", color = Color(0xFF64748B))
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = DeepRoyalBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kas Masuk / Kas Keluar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkSlate)
            }
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { isCashIn = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCashIn) EmeraldGreen else Color(0xFFF1F5F9)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kas Masuk (+)", color = if (isCashIn) CrispWhite else DarkSlate, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { isCashIn = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!isCashIn) Color(0xFFDC2626) else Color(0xFFF1F5F9)
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Kas Keluar (-)", color = if (!isCashIn) CrispWhite else DarkSlate, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Nominal (Rp)") },
                    prefix = { Text("Rp ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Alasan / Keperluan") },
                    placeholder = { Text(if (isCashIn) "Contoh: Tambah modal koin" else "Contoh: Beli es batu & plastik") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}

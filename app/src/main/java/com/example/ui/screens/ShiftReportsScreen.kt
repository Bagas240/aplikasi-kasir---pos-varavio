package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Shift
import com.example.data.model.ShiftSchedule
import com.example.data.model.StaffUser
import com.example.data.model.TransactionLog
import com.example.data.model.UserRole
import com.example.ui.PosViewModel
import com.example.ui.components.AddEditShiftScheduleDialog
import com.example.ui.components.AddEditStaffUserDialog
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
    val staffUsers by viewModel.staffUsers.collectAsStateWithLifecycle()
    val shiftSchedules by viewModel.shiftSchedules.collectAsStateWithLifecycle()
    val recentSales by viewModel.recentSales.collectAsStateWithLifecycle()

    val activeShift = remember(shifts) { shifts.find { it.status == "OPEN" } }
    val pastShifts = remember(shifts) { shifts.filter { it.status == "CLOSED" } }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog controllers
    var showOpenShiftDialog by remember { mutableStateOf(false) }
    var showCloseShiftDialog by remember { mutableStateOf(false) }
    var showCashInOutDialog by remember { mutableStateOf(false) }

    var scheduleToEdit by remember { mutableStateOf<ShiftSchedule?>(null) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToDelete by remember { mutableStateOf<ShiftSchedule?>(null) }

    var userToEdit by remember { mutableStateOf<StaffUser?>(null) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<StaffUser?>(null) }

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Shift & Manajemen Pengguna",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = DarkSlate
                    )
                    Text(
                        text = "Pengaturan Shift, Tim Kasir, dan Audit Kesalahan Transaksi",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
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

            Spacer(modifier = Modifier.height(10.dp))

            // 4 Navigation Tabs
            val tabs = listOf(
                Pair("Kasir Aktif", Icons.Default.AccountBalanceWallet),
                Pair("Jadwal Shift", Icons.Default.Schedule),
                Pair("Pengguna & PIN (${staffUsers.size})", Icons.Default.People),
                Pair("Audit Kasir", Icons.Default.Badge)
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp)
                            }
                        },
                        selectedContentColor = DeepRoyalBlue,
                        unselectedContentColor = Color(0xFF64748B)
                    )
                }
            }

            Divider(color = Color(0xFFE2E8F0), modifier = Modifier.padding(bottom = 10.dp))

            // TAB CONTENT
            when (selectedTab) {
                0 -> ActiveShiftContent(
                    activeShift = activeShift,
                    pastShifts = pastShifts,
                    onOpenShiftClick = { showOpenShiftDialog = true },
                    onCloseShiftClick = { showCloseShiftDialog = true },
                    onCashInOutClick = { showCashInOutDialog = true }
                )
                1 -> ShiftScheduleManagementContent(
                    schedules = shiftSchedules,
                    onAddClick = { showAddScheduleDialog = true },
                    onEditClick = { scheduleToEdit = it },
                    onDeleteClick = { scheduleToDelete = it }
                )
                2 -> StaffUserManagementContent(
                    currentUser = currentUser,
                    staffUsers = staffUsers,
                    onAddUserClick = { showAddUserDialog = true },
                    onEditUserClick = { userToEdit = it },
                    onDeleteUserClick = { userToDelete = it },
                    onSwitchUser = { viewModel.switchUser(it) }
                )
                3 -> CashierAuditContent(
                    recentSales = recentSales,
                    staffUsers = staffUsers
                )
            }
        }
    }

    // DIALOGS
    if (showOpenShiftDialog) {
        OpenShiftDialog(
            cashierName = currentUser.name,
            staffUsers = staffUsers,
            shiftSchedules = shiftSchedules,
            onConfirmShift = { floatAmt, cashier, schedName, schedTime ->
                viewModel.openShift(
                    startingFloat = floatAmt,
                    cashierName = cashier,
                    shiftScheduleName = schedName,
                    shiftScheduleTime = schedTime
                )
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

    if (showAddScheduleDialog) {
        AddEditShiftScheduleDialog(
            onSave = {
                viewModel.saveShiftSchedule(it)
                showAddScheduleDialog = false
            },
            onDismiss = { showAddScheduleDialog = false }
        )
    }

    if (scheduleToEdit != null) {
        AddEditShiftScheduleDialog(
            initialSchedule = scheduleToEdit,
            onSave = {
                viewModel.saveShiftSchedule(it)
                scheduleToEdit = null
            },
            onDismiss = { scheduleToEdit = null }
        )
    }

    if (scheduleToDelete != null) {
        AlertDialog(
            onDismissRequest = { scheduleToDelete = null },
            title = { Text("Hapus Jadwal Shift?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus '${scheduleToDelete?.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        scheduleToDelete?.let { viewModel.deleteShiftSchedule(it) }
                        scheduleToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { scheduleToDelete = null }) { Text("Batal") }
            }
        )
    }

    if (showAddUserDialog) {
        AddEditStaffUserDialog(
            onSave = {
                viewModel.saveStaffUser(it)
                showAddUserDialog = false
            },
            onDismiss = { showAddUserDialog = false }
        )
    }

    if (userToEdit != null) {
        AddEditStaffUserDialog(
            initialUser = userToEdit,
            onSave = {
                viewModel.saveStaffUser(it)
                userToEdit = null
            },
            onDismiss = { userToEdit = null }
        )
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Hapus Pengguna?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus akun '${userToDelete?.name}' (${userToDelete?.role?.label})?") },
            confirmButton = {
                Button(
                    onClick = {
                        userToDelete?.let { viewModel.deleteStaffUser(it) }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Hapus Pengguna", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("Batal") }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 0: KASIR AKTIF & CASH DRAWER
// -------------------------------------------------------------
@Composable
private fun ActiveShiftContent(
    activeShift: Shift?,
    pastShifts: List<Shift>,
    onOpenShiftClick: () -> Unit,
    onCloseShiftClick: () -> Unit,
    onCashInOutClick: () -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
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
                                    Text(
                                        text = activeShift.shiftScheduleName.ifBlank { "SHIFT AKTIF" },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = EmeraldDark
                                    )
                                    if (activeShift.shiftScheduleTime.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "(${activeShift.shiftScheduleTime})",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Kasir: ${activeShift.cashierName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DarkSlate
                                )
                                Text(
                                    text = "Dibuka: ${CurrencyFormatter.formatTime(activeShift.startTime)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
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
                                onClick = onCashInOutClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kas Masuk/Keluar", fontSize = 11.sp)
                            }

                            Button(
                                onClick = onCloseShiftClick,
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
                        Text("Buka shift kasir baru untuk memilih jadwal shift & kasir bertugas", fontSize = 12.sp, color = Color(0xFF7F1D1D))
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenShiftClick,
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
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Riwayat Rekonsiliasi Shift Kasir:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
        }

        if (pastShifts.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada riwayat shift yang ditutup.", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        } else {
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(shift.cashierName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                    if (shift.shiftScheduleName.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(DeepRoyalBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(shift.shiftScheduleName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = DeepRoyalBlue)
                                        }
                                    }
                                }
                                Text(
                                    "${CurrencyFormatter.formatDate(shift.startTime)} s/d ${if (shift.endTime != null) CurrencyFormatter.formatTime(shift.endTime) else "-"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

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
                                    text = if (isMatch) "Cocok (Pas)" else if (isOver) "+${CurrencyFormatter.formatRupiah(diff)}" else "-${CurrencyFormatter.formatRupiah(-diff)}",
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

// -------------------------------------------------------------
// TAB 1: PENGATURAN JADWAL SHIFT (SHIFT 1, SHIFT 2, SAMPAI JAM BERAPA)
// -------------------------------------------------------------
@Composable
private fun ShiftScheduleManagementContent(
    schedules: List<ShiftSchedule>,
    onAddClick: () -> Unit,
    onEditClick: (ShiftSchedule) -> Unit,
    onDeleteClick: (ShiftSchedule) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            // Explanation Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pembagian Jam Kerja Shift Kasir", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepRoyalBlue)
                        Text(
                            text = "Atur Shift 1, Shift 2, dst. Anda dapat menentukan 'sampai jam berapa' shift berlangsung untuk kasir.",
                            fontSize = 11.sp,
                            color = Color(0xFF334155),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daftar Jadwal Shift (${schedules.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkSlate
                )
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Shift", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (schedules.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Belum ada jadwal shift. Klik tombol Tambah Shift di atas.", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        } else {
            items(schedules) { schedule ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(DeepRoyalBlue.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(schedule.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "${schedule.startTime} s/d ${schedule.endTime}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark
                                        )
                                    }
                                    if (schedule.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            schedule.notes,
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = { onEditClick(schedule) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VibrantBlue, modifier = Modifier.size(18.dp))
                            }
                            IconButton(onClick = { onDeleteClick(schedule) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: KELOLA PENGGUNA & PASSWORD (OWNER, MANAJER, KASIR 1, KASIR 2)
// -------------------------------------------------------------
@Composable
private fun StaffUserManagementContent(
    currentUser: StaffUser,
    staffUsers: List<StaffUser>,
    onAddUserClick: () -> Unit,
    onEditUserClick: (StaffUser) -> Unit,
    onDeleteUserClick: (StaffUser) -> Unit,
    onSwitchUser: (StaffUser) -> Unit
) {
    var revealedUserPins by remember { mutableStateOf(setOf<String>()) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            // Count Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Akun Pengguna", fontSize = 11.sp, color = Color(0xFF64748B))
                        Text(
                            text = "${staffUsers.size} Pengguna Terdaftar",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = DarkSlate
                        )
                        val ownerCount = staffUsers.count { it.role == UserRole.OWNER }
                        val managerCount = staffUsers.count { it.role == UserRole.MANAGER }
                        val cashierCount = staffUsers.count { it.role == UserRole.CASHIER }
                        Text(
                            text = "$ownerCount Owner • $managerCount Manajer • $cashierCount Kasir",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Button(
                        onClick = onAddUserClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(staffUsers) { user ->
            val isCurrentActive = user.id == currentUser.id
            val isPinVisible = revealedUserPins.contains(user.id)
            val roleColor = when (user.role) {
                UserRole.OWNER -> DeepRoyalBlue
                UserRole.MANAGER -> Color(0xFFD97706)
                UserRole.CASHIER -> EmeraldGreen
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isCurrentActive) 1.5.dp else 1.dp,
                        color = if (isCurrentActive) DeepRoyalBlue else Color(0xFFE2E8F0),
                        shape = RoundedCornerShape(10.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrentActive) Color(0xFFF0FDF4) else CrispWhite
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(roleColor.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (user.role == UserRole.OWNER) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(user.role.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = roleColor)
                                    }
                                    if (isCurrentActive) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(EmeraldGreen, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text("AKTIF", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = CrispWhite)
                                        }
                                    }
                                }
                                Text("ID: ${user.id} ${if (user.phone.isNotBlank()) "• ${user.phone}" else ""}", fontSize = 10.sp, color = Color(0xFF64748B))
                            }
                        }

                        Row {
                            IconButton(onClick = { onEditUserClick(user) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VibrantBlue, modifier = Modifier.size(18.dp))
                            }
                            if (staffUsers.size > 1) {
                                IconButton(onClick = { onDeleteUserClick(user) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    // Password / PIN Area & Switch Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Password / PIN: ", fontSize = 11.sp, color = Color(0xFF64748B))
                            Text(
                                text = if (isPinVisible) user.pin else "••••",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = DarkSlate
                            )
                            IconButton(
                                onClick = {
                                    revealedUserPins = if (isPinVisible) {
                                        revealedUserPins - user.id
                                    } else {
                                        revealedUserPins + user.id
                                    }
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Tampilkan PIN",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (!isCurrentActive) {
                            OutlinedButton(
                                onClick = { onSwitchUser(user) },
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(28.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text("Beralih ke Akun Ini", fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: AUDIT KASIR & TRANSAKSI (LACAK SIAPA YANG NGASIRIN)
// -------------------------------------------------------------
@Composable
private fun CashierAuditContent(
    recentSales: List<TransactionLog>,
    staffUsers: List<StaffUser>
) {
    var selectedCashierFilter by remember { mutableStateOf("Semua Kasir") }
    var searchQuery by remember { mutableStateOf("") }

    val cashierNames = remember(staffUsers, recentSales) {
        listOf("Semua Kasir") + (staffUsers.map { it.name } + recentSales.map { it.cashierName }).distinct()
    }

    val filteredOrders = remember(recentSales, selectedCashierFilter, searchQuery) {
        recentSales.filter { order ->
            val matchCashier = selectedCashierFilter == "Semua Kasir" || order.cashierName == selectedCashierFilter
            val matchSearch = searchQuery.isBlank() ||
                    order.orderId.contains(searchQuery, ignoreCase = true) ||
                    order.customerName.contains(searchQuery, ignoreCase = true)
            matchCashier && matchSearch
        }
    }

    val totalTurnover = remember(filteredOrders) { filteredOrders.sumOf { it.grandTotal } }
    val totalCashOrders = remember(filteredOrders) { filteredOrders.count { it.paymentMethod == "TUNAI" || it.paymentMethod == "CASH" } }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxSize()) {
        item {
            // Explanation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Audit Jejak Kasir & Transaksi", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                    Text(
                        text = "Setiap nota transaksi mencatat siapa kasir yang melayani dan shift berapa. Memudahkan pengecekan jika ada kekeliruan kasir.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            // Filter by Cashier Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(cashierNames) { cashier ->
                    val isSelected = cashier == selectedCashierFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCashierFilter = cashier },
                        label = { Text(cashier, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepRoyalBlue,
                            selectedLabelColor = CrispWhite
                        )
                    )
                }
            }
        }

        item {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Transaksi", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("${filteredOrders.size} Nota", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                    }
                    Column {
                        Text("Transaksi Tunai", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text("$totalCashOrders Nota", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = EmeraldDark)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Omset Diproses", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(CurrencyFormatter.formatRupiah(totalTurnover), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DeepRoyalBlue)
                    }
                }
            }
        }

        item {
            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nomor nota atau nama pelanggan...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantBlue, modifier = Modifier.size(18.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )
        }

        if (filteredOrders.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("Tidak ditemukan transaksi untuk kasir ini.", color = Color(0xFF64748B), fontSize = 12.sp)
                }
            }
        } else {
            items(filteredOrders) { order ->
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
                                Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                Text(
                                    "${CurrencyFormatter.formatDate(order.timestamp)} • ${CurrencyFormatter.formatTime(order.timestamp)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Text(
                                text = CurrencyFormatter.formatRupiah(order.grandTotal),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = DeepRoyalBlue
                            )
                        }

                        Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 6.dp))

                        // Cashier and Shift Attribution
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(DeepRoyalBlue.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Kasir: ${order.cashierName} (${order.cashierRole})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepRoyalBlue
                                    )
                                }
                                if (order.shiftName.isNotBlank()) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldGreen.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = order.shiftName,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "Metode: ${order.paymentMethod}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
        }
    }
}

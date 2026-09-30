package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.example.data.model.ShiftSchedule
import com.example.data.model.StaffUser
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen

@Composable
fun AddEditShiftScheduleDialog(
    initialSchedule: ShiftSchedule? = null,
    staffUsers: List<StaffUser> = emptyList(),
    onSave: (ShiftSchedule) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialSchedule?.name ?: "") }
    var startTime by remember { mutableStateOf(initialSchedule?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(initialSchedule?.endTime ?: "16:00") }
    var assignedCashierName by remember { mutableStateOf(initialSchedule?.assignedCashierName ?: "") }
    var notes by remember { mutableStateOf(initialSchedule?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialSchedule != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Nama shift wajib diisi (misal: Shift 1 / Pagi)"
                        return@Button
                    }
                    if (startTime.isBlank() || endTime.isBlank()) {
                        errorMessage = "Jam mulai dan jam selesai harus diatur (misal: 08:00 s/d 16:00)"
                        return@Button
                    }
                    val schedule = (initialSchedule ?: ShiftSchedule(
                        name = name.trim(),
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        assignedCashierName = assignedCashierName.trim(),
                        notes = notes.trim()
                    )).copy(
                        name = name.trim(),
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        assignedCashierName = assignedCashierName.trim(),
                        notes = notes.trim()
                    )
                    onSave(schedule)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "Simpan Perubahan" else "Tambah Shift", fontWeight = FontWeight.Bold)
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
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = DeepRoyalBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isEditing) "Edit Jadwal Shift" else "Tambah Jadwal Shift",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkSlate
                    )
                    Text("Atur nama, rentang jam kerja & petugas kasir", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Quick Name Presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Shift 1", "Shift 2", "Shift 3", "Shift 4").forEach { preset ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (name.startsWith(preset)) DeepRoyalBlue.copy(alpha = 0.12f) else Color(0xFFF1F5F9))
                                .clickable {
                                    name = "$preset (${if (preset == "Shift 1") "Pagi" else if (preset == "Shift 2") "Siang/Sore" else if (preset == "Shift 3") "Malam" else "Lembur"})"
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(preset, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (name.startsWith(preset)) DeepRoyalBlue else DarkSlate)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nama Shift") },
                    placeholder = { Text("Contoh: Shift 1 / Shift Pagi / Shift Siang") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = {
                            startTime = it
                            errorMessage = null
                        },
                        label = { Text("Mulai Jam") },
                        placeholder = { Text("08:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = endTime,
                        onValueChange = {
                            endTime = it
                            errorMessage = null
                        },
                        label = { Text("Sampai Jam") },
                        placeholder = { Text("16:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Cashier Selection for this Shift
                Text("Petugas Kasir Bertanggung Jawab:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                Spacer(modifier = Modifier.height(4.dp))
                if (staffUsers.isNotEmpty()) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            val isAll = assignedCashierName.isBlank()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isAll) DeepRoyalBlue else Color(0xFFF1F5F9))
                                    .clickable { assignedCashierName = "" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text("Semua Kasir", fontSize = 11.sp, color = if (isAll) CrispWhite else DarkSlate, fontWeight = FontWeight.Bold)
                            }
                        }
                        items(staffUsers) { staff ->
                            val isSelected = assignedCashierName == staff.name
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DeepRoyalBlue else Color(0xFFF1F5F9))
                                    .clickable { assignedCashierName = staff.name }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(staff.name, fontSize = 11.sp, color = if (isSelected) CrispWhite else DarkSlate, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = assignedCashierName,
                        onValueChange = { assignedCashierName = it },
                        label = { Text("Nama Kasir") },
                        placeholder = { Text("Contoh: Kasir 1 / Budi (Bisa ditambah di Pengaturan)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Misal: Serah terima uang kas di laci") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        errorMessage!!,
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    )
}

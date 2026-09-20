package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.ShiftSchedule
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue

@Composable
fun AddEditShiftScheduleDialog(
    initialSchedule: ShiftSchedule? = null,
    onSave: (ShiftSchedule) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialSchedule?.name ?: "") }
    var startTime by remember { mutableStateOf(initialSchedule?.startTime ?: "07:00") }
    var endTime by remember { mutableStateOf(initialSchedule?.endTime ?: "15:00") }
    var notes by remember { mutableStateOf(initialSchedule?.notes ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialSchedule != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Nama shift wajib diisi (misal: Shift 1 (Pagi))"
                        return@Button
                    }
                    if (startTime.isBlank() || endTime.isBlank()) {
                        errorMessage = "Jam mulai dan jam selesai harus diatur (misal: 07:00 dan 15:00)"
                        return@Button
                    }
                    val schedule = (initialSchedule ?: ShiftSchedule(
                        name = name.trim(),
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
                        notes = notes.trim()
                    )).copy(
                        name = name.trim(),
                        startTime = startTime.trim(),
                        endTime = endTime.trim(),
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
                    Text("Atur pembagian jam kerja kasir", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nama Shift") },
                    placeholder = { Text("Contoh: Shift 1 (Pagi) / Shift 2 (Sore)") },
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
                        placeholder = { Text("07:00") },
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
                        placeholder = { Text("15:00") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Presets
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        Triple("Shift 1 (Pagi)", "07:00", "15:00"),
                        Triple("Shift 2 (Sore)", "15:00", "23:00"),
                        Triple("Shift 3 (Malam)", "23:00", "07:00")
                    ).forEach { (presetName, start, end) ->
                        Button(
                            onClick = {
                                name = presetName
                                startTime = start
                                endTime = end
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(start.take(2) + "-" + end.take(2), fontSize = 10.sp, color = DarkSlate)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan (Opsional)") },
                    placeholder = { Text("Misal: Termasuk 30 menit serah terima kasir") },
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

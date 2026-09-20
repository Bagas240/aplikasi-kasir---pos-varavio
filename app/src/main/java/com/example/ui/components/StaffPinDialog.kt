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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StaffUser
import com.example.data.model.UserRole
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VibrantBlue

@Composable
fun StaffPinDialog(
    currentUser: StaffUser,
    staffUsers: List<StaffUser> = emptyList(),
    onUserSwitched: (StaffUser) -> Unit,
    onLogout: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val effectiveUsers = if (staffUsers.isNotEmpty()) staffUsers else listOf(
        StaffUser("U-001", "Bagas (Owner)", UserRole.OWNER, "1234"),
        StaffUser("U-002", "Dian (Manajer)", UserRole.MANAGER, "2222"),
        StaffUser("U-003", "Rian (Kasir 1)", UserRole.CASHIER, "0000"),
        StaffUser("U-004", "Siti (Kasir 2)", UserRole.CASHIER, "1111")
    )

    var selectedTargetUser by remember(effectiveUsers) {
        mutableStateOf(effectiveUsers.firstOrNull { it.id != currentUser.id } ?: effectiveUsers.first())
    }
    var enteredPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (enteredPin == selectedTargetUser.pin) {
                        onUserSwitched(selectedTargetUser)
                    } else {
                        errorMessage = "Password / PIN salah untuk ${selectedTargetUser.name}!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Login Staff", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (onLogout != null) {
                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                    ) {
                        Text("Keluar Akun", color = Color(0xFFDC2626))
                    }
                }
                Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
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
                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = DeepRoyalBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Ganti Akun Kasir / Role", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkSlate)
                    Text("Saat ini: ${currentUser.name} (${currentUser.role.label})", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column {
                Text("Pilih Akun Pengguna (${effectiveUsers.size} Terdaftar):", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkSlate)
                Spacer(modifier = Modifier.height(6.dp))

                effectiveUsers.forEach { staff ->
                    val isSelected = staff.id == selectedTargetUser.id
                    val roleColor = when (staff.role) {
                        UserRole.OWNER -> DeepRoyalBlue
                        UserRole.MANAGER -> Color(0xFFD97706) // Amber
                        UserRole.CASHIER -> EmeraldGreen
                    }
                    val roleDesc = when (staff.role) {
                        UserRole.OWNER -> "Akses Penuh (Owner/Pemilik)"
                        UserRole.MANAGER -> "Akses Manajerial & Laporan"
                        UserRole.CASHIER -> "Operasional Kasir & Penjualan"
                    }

                    Card(
                        onClick = {
                            selectedTargetUser = staff
                            enteredPin = ""
                            errorMessage = null
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (staff.role == UserRole.OWNER) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(roleColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(staff.role.label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = roleColor)
                                        }
                                    }
                                    Text(roleDesc, fontSize = 10.sp, color = Color(0xFF64748B))
                                }
                            }
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(DeepRoyalBlue, CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = {
                        enteredPin = it.take(6).filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Masukkan PIN 4-Digit") },
                    placeholder = { Text("PIN untuk ${selectedTargetUser.name}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(errorMessage!!, color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: Role Kasir tidak dapat melihat laba bersih atau mengedit harga modal (HPP).",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 14.sp
                )
            }
        }
    )
}

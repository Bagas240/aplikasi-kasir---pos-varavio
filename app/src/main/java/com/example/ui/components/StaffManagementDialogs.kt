package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StaffUser
import com.example.data.model.UserRole
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen

@Composable
fun AddEditStaffUserDialog(
    initialUser: StaffUser? = null,
    onSave: (StaffUser) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialUser?.name ?: "") }
    var role by remember { mutableStateOf(initialUser?.role ?: UserRole.CASHIER) }
    var pin by remember { mutableStateOf(initialUser?.pin ?: "") }
    var phone by remember { mutableStateOf(initialUser?.phone ?: "") }
    var isPinVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val isEditing = initialUser != null

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Nama pengguna wajib diisi (misal: Kasir 1 / Kasir 2 / Manajer)"
                        return@Button
                    }
                    if (pin.length < 4) {
                        errorMessage = "Password / PIN wajib minimal 4 digit angka!"
                        return@Button
                    }
                    val user = (initialUser ?: StaffUser(
                        id = "",
                        name = name.trim(),
                        role = role,
                        pin = pin.trim(),
                        phone = phone.trim()
                    )).copy(
                        name = name.trim(),
                        role = role,
                        pin = pin.trim(),
                        phone = phone.trim()
                    )
                    onSave(user)
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isEditing) "Simpan Pengguna" else "Tambah Pengguna", fontWeight = FontWeight.Bold)
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
                    Icon(
                        if (role == UserRole.OWNER) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = null,
                        tint = DeepRoyalBlue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isEditing) "Edit Data Pengguna" else "Tambah Pengguna Baru",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DarkSlate
                    )
                    Text("Pengaturan hak akses & password akun", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Nama Akun
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Nama Pengguna / Kasir") },
                    placeholder = { Text("Contoh: Kasir 1 / Kasir 2 / Manajer") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Role Selector
                Text("Pilih Hak Akses / Role:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    UserRole.values().forEach { roleOption ->
                        val isSelected = role == roleOption
                        val roleColor = when (roleOption) {
                            UserRole.OWNER -> DeepRoyalBlue
                            UserRole.MANAGER -> Color(0xFFD97706)
                            UserRole.CASHIER -> EmeraldGreen
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) roleColor else Color(0xFFF1F5F9))
                                .clickable { role = roleOption }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = roleOption.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) CrispWhite else DarkSlate
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Password / PIN
                OutlinedTextField(
                    value = pin,
                    onValueChange = {
                        pin = it.take(6).filter { ch -> ch.isDigit() }
                        errorMessage = null
                    },
                    label = { Text("Password / PIN Akses (4-6 Angka)") },
                    placeholder = { Text("Misal: 1234") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPinVisible = !isPinVisible }) {
                            Icon(
                                if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Lihat PIN",
                                tint = Color(0xFF64748B)
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Phone (optional)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("No. HP / WhatsApp (Opsional)") },
                    placeholder = { Text("0812xxxx") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
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

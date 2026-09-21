package com.example.ui.screens.onboarding

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.components.VoravioLogo
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuthPreferences
import com.example.data.model.UserRole
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue

@Composable
fun AuthScreen(
    authPreferences: AuthPreferences,
    onAuthSuccess: (username: String, name: String, role: UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasExistingAccount = authPreferences.username.isNotBlank()
    var selectedTab by remember { mutableIntStateOf(if (hasExistingAccount) 0 else 1) }

    // Login Form State (Username + PIN)
    var loginUsername by remember { mutableStateOf(authPreferences.username) }
    var loginPin by remember { mutableStateOf("") }
    var showLoginPin by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Quick Setup Form State (Nama Toko + Username + PIN)
    var setupStoreName by remember { mutableStateOf(authPreferences.storeName.ifBlank { "Voravio Mart" }) }
    var setupUsername by remember { mutableStateOf(authPreferences.username.ifBlank { "admin" }) }
    var setupPin by remember { mutableStateOf(if (hasExistingAccount) authPreferences.pin else "1234") }
    var showSetupPin by remember { mutableStateOf(false) }
    var setupError by remember { mutableStateOf<String?>(null) }

    // Recovery Dialog State
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var recoveryUsernameInput by remember { mutableStateOf("") }
    var recoveryNewPin by remember { mutableStateOf("") }
    var recoveryMessage by remember { mutableStateOf<String?>(null) }
    var recoveryError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftGrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo & Title
            VoravioLogo(
                size = 64.dp,
                onDarkBackground = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Voravio POS",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkSlate
            )

            Text(
                text = "Sistem Kasir & Manajemen Toko Pintar",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tab Selector: Masuk / Buka Toko Baru
            Card(
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CrispWhite,
                    contentColor = DeepRoyalBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            loginError = null
                        },
                        text = {
                            Text(
                                text = "Masuk Kasir",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) DeepRoyalBlue else Color(0xFF64748B)
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            setupError = null
                        },
                        text = {
                            Text(
                                text = "Setup Toko & Akun",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 1) DeepRoyalBlue else Color(0xFF64748B)
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // ==========================================
                // LOGIN WITH USERNAME & PIN ONLY
                // ==========================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Masuk ke Voravio POS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username Field
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = {
                                loginUsername = it
                                loginError = null
                            },
                            label = { Text("Username") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = VibrantBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // PIN Field (Acts as Password)
                        OutlinedTextField(
                            value = loginPin,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(6)
                                loginPin = digitsOnly
                                loginError = null
                            },
                            label = { Text("PIN Keamanan (4-6 Angka)") },
                            placeholder = { Text("Contoh: 1234") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = VibrantBlue) },
                            trailingIcon = {
                                IconButton(onClick = { showLoginPin = !showLoginPin }) {
                                    Icon(
                                        imageVector = if (showLoginPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle PIN Visibility"
                                    )
                                }
                            },
                            visualTransformation = if (showLoginPin) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        if (loginError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = loginError!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Forgot PIN / Reset Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    recoveryUsernameInput = loginUsername
                                    recoveryMessage = null
                                    recoveryError = null
                                    showRecoveryDialog = true
                                }
                            ) {
                                Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(14.dp), tint = VibrantBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lupa PIN?",
                                    color = VibrantBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val trimmedUser = loginUsername.trim()
                                val trimmedPin = loginPin.trim()

                                if (trimmedUser.isBlank() || trimmedPin.isBlank()) {
                                    loginError = "Username dan PIN tidak boleh kosong."
                                    return@Button
                                }

                                if (trimmedPin.length < 4) {
                                    loginError = "PIN minimal 4 digit angka."
                                    return@Button
                                }

                                val isValid = authPreferences.validateLoginWithPin(trimmedUser, trimmedPin)
                                if (isValid) {
                                    authPreferences.isLoggedIn = true
                                    authPreferences.username = trimmedUser
                                    onAuthSuccess(trimmedUser, authPreferences.fullName.ifBlank { trimmedUser }, authPreferences.role)
                                } else {
                                    loginError = "Username atau PIN salah. Periksa kembali atau gunakan opsi 'Lupa PIN'."
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_submit_button")
                        ) {
                            Text("Masuk ke Kasir", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrispWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                // ==========================================
                // SETUP TOKO & AKUN (NAMA TOKO + USERNAME + PIN)
                // ==========================================
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mulai Cepat Toko Baru",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkSlate
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Notice banner explaining simple PIN auth
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEFF6FF))
                                .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = VibrantBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Sistem menggunakan PIN sebagai kata sandi tunggal untuk akses yang lebih cepat & praktis.",
                                    fontSize = 11.sp,
                                    color = DeepRoyalBlue,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 1. Nama Toko
                        OutlinedTextField(
                            value = setupStoreName,
                            onValueChange = {
                                setupStoreName = it
                                setupError = null
                            },
                            label = { Text("1. Nama Toko") },
                            placeholder = { Text("Contoh: Voravio Mart") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, tint = VibrantBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("store_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Username Kasir / Pemilik
                        OutlinedTextField(
                            value = setupUsername,
                            onValueChange = {
                                setupUsername = it.replace(" ", "")
                                setupError = null
                            },
                            label = { Text("2. Username") },
                            placeholder = { Text("Contoh: admin atau kasir") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = VibrantBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. PIN Keamanan
                        OutlinedTextField(
                            value = setupPin,
                            onValueChange = { input ->
                                val digitsOnly = input.filter { it.isDigit() }.take(6)
                                setupPin = digitsOnly
                                setupError = null
                            },
                            label = { Text("3. PIN Keamanan (4-6 Angka)") },
                            placeholder = { Text("Contoh: 1234") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = VibrantBlue) },
                            trailingIcon = {
                                IconButton(onClick = { showSetupPin = !showSetupPin }) {
                                    Icon(
                                        imageVector = if (showSetupPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle PIN"
                                    )
                                }
                            },
                            visualTransformation = if (showSetupPin) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_password_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = VibrantBlue,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            )
                        )

                        if (setupError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = setupError!!,
                                color = Color(0xFFDC2626),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val sName = setupStoreName.trim()
                                val uName = setupUsername.trim()
                                val pCode = setupPin.trim()

                                if (sName.isBlank()) {
                                    setupError = "Nama Toko wajib diisi."
                                    return@Button
                                }
                                if (uName.isBlank()) {
                                    setupError = "Username wajib diisi."
                                    return@Button
                                }
                                if (pCode.length < 4) {
                                    setupError = "PIN minimal 4 digit angka."
                                    return@Button
                                }

                                // Setup store and account with PIN only
                                authPreferences.setupInitialAccountAndStore(
                                    store = sName,
                                    user = uName,
                                    userPin = pCode
                                )

                                onAuthSuccess(uName, uName, UserRole.OWNER)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("register_submit_button")
                        ) {
                            Text("Mulai Aplikasi Sekarang", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrispWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // RECOVERY DIALOG FOR PIN RESET
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        val inputUser = recoveryUsernameInput.trim()
                        val newPin = recoveryNewPin.trim()

                        if (inputUser.isBlank() || newPin.isBlank()) {
                            recoveryError = "Username dan PIN baru wajib diisi."
                            return@Button
                        }
                        if (newPin.length < 4) {
                            recoveryError = "PIN baru minimal 4 angka."
                            return@Button
                        }

                        val registeredUser = authPreferences.username
                        if (registeredUser.isNotBlank() && !registeredUser.equals(inputUser, ignoreCase = true)) {
                            recoveryError = "Username tidak cocok dengan akun terdaftar."
                            return@Button
                        }

                        // Reset PIN
                        authPreferences.pin = newPin
                        authPreferences.passwordHash = newPin
                        loginPin = newPin
                        recoveryMessage = "PIN berhasil diubah! Silakan masuk dengan PIN baru."
                        recoveryError = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue)
                ) {
                    Text("Perbarui PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecoveryDialog = false }) {
                    Text("Tutup", color = DarkSlate)
                }
            },
            title = {
                Text("Reset PIN Akun", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DarkSlate)
            },
            text = {
                Column {
                    Text(
                        text = "Masukkan Username yang terdaftar dan tentukan PIN baru:",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = recoveryUsernameInput,
                        onValueChange = {
                            recoveryUsernameInput = it
                            recoveryError = null
                        },
                        label = { Text("Username Akun") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = recoveryNewPin,
                        onValueChange = { input ->
                            recoveryNewPin = input.filter { it.isDigit() }.take(6)
                            recoveryError = null
                        },
                        label = { Text("PIN Baru (4-6 Angka)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (recoveryError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(recoveryError!!, color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (recoveryMessage != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(recoveryMessage!!, color = EmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

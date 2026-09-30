package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.AuthPreferences
import com.example.data.model.ShiftSchedule
import com.example.data.model.StaffUser
import com.example.data.model.StoreProfile
import com.example.data.model.UserRole
import com.example.ui.PosViewModel
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.MediaHelper
import java.io.File
import java.util.UUID

@Composable
fun ProfileSettingsDialog(
    viewModel: PosViewModel,
    storeProfile: StoreProfile,
    currentUser: StaffUser,
    staffUsers: List<StaffUser>,
    shiftSchedules: List<ShiftSchedule>,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val authPreferences = remember { AuthPreferences(context) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Toko, 1: QRIS, 2: Kasir/Staff, 3: Shift

    // Store Form State
    var storeName by remember { mutableStateOf(storeProfile.storeName) }
    var storeAddress by remember { mutableStateOf(storeProfile.address) }
    var storePhone by remember { mutableStateOf(storeProfile.phone) }
    var storeLogoUri by remember { mutableStateOf(storeProfile.logoUri) }
    var storeQrisUri by remember { mutableStateOf(storeProfile.qrisImageUri) }
    var receiptHeader by remember { mutableStateOf(storeProfile.receiptHeader) }
    var receiptFooter by remember { mutableStateOf(storeProfile.receiptFooter) }
    var printerPaperWidth by remember { mutableStateOf(storeProfile.printerPaperWidth) }

    // Dialog state for Staff Management
    var showAddStaffDialog by remember { mutableStateOf(false) }
    var staffToEdit by remember { mutableStateOf<StaffUser?>(null) }
    var staffToDelete by remember { mutableStateOf<StaffUser?>(null) }

    // Dialog state for Shift Schedule Management
    var showAddScheduleDialog by remember { mutableStateOf(false) }
    var scheduleToEdit by remember { mutableStateOf<ShiftSchedule?>(null) }

    // Photo Launchers for Store Logo
    val logoCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val path = MediaHelper.saveBitmapToInternalStorage(context, bitmap, "store_logo")
            if (path != null) {
                storeLogoUri = path
                val updated = storeProfile.copy(logoUri = path)
                viewModel.updateStoreProfile(updated)
                Toast.makeText(context, "Foto toko berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val logoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = MediaHelper.copyUriToInternalStorage(context, uri, "store_logo")
            if (path != null) {
                storeLogoUri = path
                val updated = storeProfile.copy(logoUri = path)
                viewModel.updateStoreProfile(updated)
                Toast.makeText(context, "Foto toko berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Photo Launchers for Store QRIS
    val qrisCameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            val path = MediaHelper.saveBitmapToInternalStorage(context, bitmap, "store_qris")
            if (path != null) {
                storeQrisUri = path
                viewModel.updateStoreQrisImage(path)
                Toast.makeText(context, "Foto QRIS Toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    val qrisPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            val path = MediaHelper.copyUriToInternalStorage(context, uri, "store_qris")
            if (path != null) {
                storeQrisUri = path
                viewModel.updateStoreQrisImage(path)
                Toast.makeText(context, "Foto QRIS Toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            color = SoftGrayBg
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Bar
                Surface(
                    color = DeepRoyalBlue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(CrispWhite.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Profil & Pengaturan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = CrispWhite)
                                Text("Toko, QRIS, Kasir, dan Jadwal Shift", fontSize = 11.sp, color = CrispWhite.copy(alpha = 0.85f))
                            }
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CrispWhite)
                        }
                    }
                }

                // Tabs Navigation
                val tabs = listOf(
                    Triple(0, "Toko & Bisnis", Icons.Default.Store),
                    Triple(1, "Foto QRIS", Icons.Default.QrCode),
                    Triple(2, "Kasir", Icons.Default.People),
                    Triple(3, "Jadwal Shift", Icons.Default.Schedule)
                )

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CrispWhite,
                    edgePadding = 12.dp,
                    divider = {}
                ) {
                    tabs.forEach { (index, title, icon) ->
                        val isSelected = selectedTab == index
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (isSelected) DeepRoyalBlue else Color(0xFF64748B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) DeepRoyalBlue else Color(0xFF64748B)
                                    )
                                }
                            }
                        )
                    }
                }

                HorizontalDivider(color = CardBorder)

                // Tab Content
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (selectedTab) {
                        0 -> {
                            // TAB 0: Profil Toko & Bisnis
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text("1. Foto / Logo Toko", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(72.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(Color(0xFFEFF6FF))
                                                    .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!storeLogoUri.isNullOrBlank() && File(storeLogoUri!!).exists()) {
                                                    AsyncImage(
                                                        model = File(storeLogoUri!!),
                                                        contentDescription = "Logo Toko",
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else {
                                                    Icon(Icons.Default.Store, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(32.dp))
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(14.dp))

                                            Column {
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedButton(
                                                        onClick = { logoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepRoyalBlue)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Galeri", fontSize = 11.sp, color = DeepRoyalBlue, fontWeight = FontWeight.Bold)
                                                    }

                                                    OutlinedButton(
                                                        onClick = { logoCameraLauncher.launch(null) },
                                                        shape = RoundedCornerShape(8.dp)
                                                    ) {
                                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepRoyalBlue)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Kamera", fontSize = 11.sp, color = DeepRoyalBlue, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                if (!storeLogoUri.isNullOrBlank()) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Hapus Foto",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFEF4444),
                                                        fontWeight = FontWeight.SemiBold,
                                                        modifier = Modifier.clickable {
                                                            storeLogoUri = null
                                                            viewModel.updateStoreProfile(storeProfile.copy(logoUri = null))
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        // Store Name
                                        OutlinedTextField(
                                            value = storeName,
                                            onValueChange = { storeName = it },
                                            label = { Text("Nama Toko") },
                                            placeholder = { Text("Contoh: VORAVIO MART") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Store Address
                                        OutlinedTextField(
                                            value = storeAddress,
                                            onValueChange = { storeAddress = it },
                                            label = { Text("Alamat Toko") },
                                            placeholder = { Text("Alamat lengkap toko...") },
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Store Phone
                                        OutlinedTextField(
                                            value = storePhone,
                                            onValueChange = { storePhone = it },
                                            label = { Text("Nomor Telepon / WhatsApp") },
                                            placeholder = { Text("0812xxxx") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // Paper size 58mm vs 80mm
                                        Text("Lebar Kertas Printer Struk:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            listOf("58mm", "80mm").forEach { widthOpt ->
                                                val isSel = printerPaperWidth == widthOpt
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (isSel) DeepRoyalBlue else Color(0xFFF1F5F9))
                                                        .clickable { printerPaperWidth = widthOpt }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Kertas $widthOpt (Thermal)",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSel) CrispWhite else DarkSlate
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = {
                                                val updated = storeProfile.copy(
                                                    storeName = storeName.trim(),
                                                    address = storeAddress.trim(),
                                                    phone = storePhone.trim(),
                                                    logoUri = storeLogoUri,
                                                    printerPaperWidth = printerPaperWidth
                                                )
                                                viewModel.updateStoreProfile(updated)
                                                Toast.makeText(context, "Profil toko berhasil disimpan!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth().height(46.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Simpan Informasi Toko", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Logout / Kunci Aplikasi
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Keluar Akun / Kunci Kasir", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFDC2626))
                                            Text("Kembali ke layar login", fontSize = 11.sp, color = Color(0xFF991B1B))
                                        }
                                        Button(
                                            onClick = onLogout,
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Icon(Icons.Default.ExitToApp, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Keluar", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            // TAB 1: Foto QRIS Toko (Dedicated Settings Upload)
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Unggah Foto QRIS Toko", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkSlate)
                                        Text(
                                            text = "Foto QRIS yang Anda unggah di sini akan langsung otomatis muncul saat pelanggan membayar via QRIS di kasir.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // QRIS Preview Box
                                        Box(
                                            modifier = Modifier
                                                .size(240.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFFF8FAFC))
                                                .border(2.dp, if (!storeQrisUri.isNullOrBlank()) EmeraldGreen else Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (!storeQrisUri.isNullOrBlank() && File(storeQrisUri!!).exists()) {
                                                AsyncImage(
                                                    model = File(storeQrisUri!!),
                                                    contentDescription = "Foto QRIS Toko",
                                                    contentScale = ContentScale.Fit,
                                                    modifier = Modifier.fillMaxSize().padding(8.dp)
                                                )
                                            } else {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(12.dp)) {
                                                    Icon(Icons.Default.QrCode, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(54.dp))
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text("Belum Ada Foto QRIS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DarkSlate)
                                                    Text("Pilih foto QRIS dari galeri atau foto langsung", fontSize = 10.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            Button(
                                                onClick = { qrisPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                                                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Pilih dari Galeri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = { qrisCameraLauncher.launch(null) },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Buka Kamera", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }

                                        if (!storeQrisUri.isNullOrBlank()) {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = "Hapus Foto QRIS",
                                                color = Color(0xFFEF4444),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.clickable {
                                                    storeQrisUri = null
                                                    viewModel.updateStoreQrisImage(null)
                                                    Toast.makeText(context, "Foto QRIS toko dihapus", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        2 -> {
                            // TAB 2: Akun Kasir & Karyawan
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Daftar Akun Kasir & Staf", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                        Text("Buat akun Kasir 1, Kasir 2, atau Manajer", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }

                                    Button(
                                        onClick = { showAddStaffDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tambah Kasir", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val staffList = if (staffUsers.isNotEmpty()) staffUsers else listOf(currentUser)

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(staffList) { staff ->
                                        val isCurrent = staff.id == currentUser.id
                                        val roleColor = when (staff.role) {
                                            UserRole.OWNER -> DeepRoyalBlue
                                            UserRole.MANAGER -> Color(0xFFD97706)
                                            UserRole.CASHIER -> EmeraldGreen
                                        }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = if (isCurrent) Color(0xFFEFF6FF) else CrispWhite),
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) DeepRoyalBlue else CardBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .background(roleColor.copy(alpha = 0.15f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Person, contentDescription = null, tint = roleColor, modifier = Modifier.size(20.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(staff.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                                            if (isCurrent) {
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Box(
                                                                    modifier = Modifier
                                                                        .background(DeepRoyalBlue, RoundedCornerShape(4.dp))
                                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                                ) {
                                                                    Text("AKTIF", color = CrispWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                                }
                                                            }
                                                        }
                                                        Text(
                                                            text = "${staff.role.label} • PIN: **** • ${if (staff.phone.isNotBlank()) staff.phone else "Tanpa No. HP"}",
                                                            fontSize = 11.sp,
                                                            color = Color(0xFF64748B)
                                                        )
                                                    }
                                                }

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (!isCurrent) {
                                                        Button(
                                                            onClick = {
                                                                viewModel.switchUser(staff)
                                                                Toast.makeText(context, "Beralih ke akun: ${staff.name}", Toast.LENGTH_SHORT).show()
                                                            },
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = DarkSlate),
                                                            shape = RoundedCornerShape(6.dp),
                                                            modifier = Modifier.height(32.dp),
                                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                                        ) {
                                                            Text("Gunakan", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                                                        }
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                    }

                                                    IconButton(
                                                        onClick = { staffToEdit = staff },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                                    }

                                                    if (staff.role != UserRole.OWNER && staff.id != currentUser.id) {
                                                        IconButton(
                                                            onClick = { staffToDelete = staff },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        3 -> {
                            // TAB 3: Shift Dinamis
                            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Jadwal Shift Kerja Kasir", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                        Text("Atur nama & jam shift secara dinamis", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }

                                    Button(
                                        onClick = { showAddScheduleDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Tambah Shift", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val defaultSchedules = listOf(
                                    ShiftSchedule(name = "Shift 1 (Pagi)", startTime = "07:00", endTime = "15:00", notes = "Shift pagi operasional utama"),
                                    ShiftSchedule(name = "Shift 2 (Sore)", startTime = "15:00", endTime = "23:00", notes = "Shift sore hingga malam"),
                                    ShiftSchedule(name = "Shift 3 (Malam)", startTime = "23:00", endTime = "07:00", notes = "Shift malam / 24 jam")
                                )
                                val schedules = if (shiftSchedules.isNotEmpty()) shiftSchedules else defaultSchedules

                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(schedules) { sched ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(containerColor = CrispWhite),
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(38.dp)
                                                            .background(Color(0xFFEFF6FF), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Default.Schedule, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(20.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Column {
                                                        Text(sched.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                                                        Text("Jam Operasional: ${sched.startTime} - ${sched.endTime}", fontSize = 11.sp, color = Color(0xFF64748B))
                                                        if (sched.notes.isNotBlank()) {
                                                            Text(sched.notes, fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                        }
                                                    }
                                                }

                                                IconButton(
                                                    onClick = { scheduleToEdit = sched },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Shift", tint = DeepRoyalBlue, modifier = Modifier.size(18.dp))
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
    }

    // Modal Add / Edit Staff User
    if (showAddStaffDialog || staffToEdit != null) {
        val target = staffToEdit
        AddEditStaffUserDialog(
            initialUser = target,
            onSave = { savedUser ->
                val userWithId = if (savedUser.id.isBlank()) {
                    savedUser.copy(id = "U-" + UUID.randomUUID().toString().take(6).uppercase())
                } else savedUser

                if (target == null) {
                    viewModel.addStaffUser(userWithId)
                    Toast.makeText(context, "Akun kasir '${userWithId.name}' berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateStaffUser(userWithId)
                    Toast.makeText(context, "Data pengguna '${userWithId.name}' berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                }
                showAddStaffDialog = false
                staffToEdit = null
            },
            onDismiss = {
                showAddStaffDialog = false
                staffToEdit = null
            }
        )
    }

    // Confirm Delete Staff Dialog
    staffToDelete?.let { staff ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { staffToDelete = null },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStaffUser(staff)
                        Toast.makeText(context, "Akun '${staff.name}' berhasil dihapus", Toast.LENGTH_SHORT).show()
                        staffToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hapus Kasir", color = CrispWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { staffToDelete = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text("Hapus Akun Kasir", fontWeight = FontWeight.Bold, color = DarkSlate) },
            text = { Text("Apakah Anda yakin ingin menghapus akun '${staff.name}'? Akun ini tidak akan bisa login lagi.", color = DarkSlate) }
        )
    }

    // Modal Add / Edit Shift Schedule
    if (showAddScheduleDialog || scheduleToEdit != null) {
        val targetSched = scheduleToEdit
        AddEditShiftScheduleDialog(
            initialSchedule = targetSched,
            staffUsers = staffUsers,
            onSave = { savedSched ->
                if (targetSched == null) {
                    viewModel.addShiftSchedule(savedSched)
                    Toast.makeText(context, "Jadwal shift '${savedSched.name}' berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.updateShiftSchedule(savedSched)
                    Toast.makeText(context, "Jadwal shift '${savedSched.name}' berhasil diubah!", Toast.LENGTH_SHORT).show()
                }
                showAddScheduleDialog = false
                scheduleToEdit = null
            },
            onDismiss = {
                showAddScheduleDialog = false
                scheduleToEdit = null
            }
        )
    }
}

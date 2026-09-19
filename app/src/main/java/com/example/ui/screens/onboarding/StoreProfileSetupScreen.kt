package com.example.ui.screens.onboarding

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.AuthPreferences
import com.example.data.model.StoreProfile
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.MediaHelper
import java.io.File

@Composable
fun StoreProfileSetupScreen(
    authPreferences: AuthPreferences,
    onProfileSaved: (StoreProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var storeName by remember { mutableStateOf(authPreferences.storeName.ifBlank { "SENTOSA RETAIL & POS" }) }
    var storeAddress by remember { mutableStateOf(authPreferences.storeAddress.ifBlank { "Jl. Thamrin No. 88, Jakarta Pusat" }) }
    var storePhone by remember { mutableStateOf(authPreferences.storePhone.ifBlank { "+62 812-3456-7890" }) }
    var storeLogoUri by remember { mutableStateOf(authPreferences.storeLogoUri) }
    var storeQrisUri by remember { mutableStateOf(authPreferences.storeQrisImageUri) }
    var receiptFooter by remember { mutableStateOf("Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar.") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Camera launcher for taking logo photo
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = MediaHelper.saveBitmapToInternalStorage(context, bitmap, "store_logo")
            if (savedPath != null) {
                storeLogoUri = savedPath
                authPreferences.storeLogoUri = savedPath
            }
        }
    }

    // Photo picker launcher for logo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedPath = MediaHelper.copyUriToInternalStorage(context, uri, "store_logo")
            if (copiedPath != null) {
                storeLogoUri = copiedPath
                authPreferences.storeLogoUri = copiedPath
            }
        }
    }

    // Camera launcher for taking QRIS photo
    val qrisCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = MediaHelper.saveBitmapToInternalStorage(context, bitmap, "store_qris")
            if (savedPath != null) {
                storeQrisUri = savedPath
                authPreferences.storeQrisImageUri = savedPath
            }
        }
    }

    // Photo picker launcher for QRIS
    val qrisPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedPath = MediaHelper.copyUriToInternalStorage(context, uri, "store_qris")
            if (copiedPath != null) {
                storeQrisUri = copiedPath
                authPreferences.storeQrisImageUri = copiedPath
            }
        }
    }

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
            // Step Header
            Text(
                text = "LANGKAH 3 DARI 4",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = VibrantBlue,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Setup Profil Toko / Usaha",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkSlate
            )

            Text(
                text = "Informasi ini akan tertera pada struk cetak kasir dan identitas toko",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Logo Upload Card with Live Media Attachment
            Card(
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Logo / Foto Usaha Anda",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkSlate
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Circular Logo Preview
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEFF6FF))
                            .border(2.dp, Color(0xFFBFDBFE), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!storeLogoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = File(storeLogoUri!!),
                                contentDescription = "Logo Toko",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = DeepRoyalBlue,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions: Camera & Gallery Pickers
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { cameraLauncher.launch(null) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("store_logo_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepRoyalBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ambil Kamera", fontSize = 12.sp, color = DeepRoyalBlue)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("store_logo_gallery_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = VibrantBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Galeri", fontSize = 12.sp, color = VibrantBlue)
                        }

                        if (!storeLogoUri.isNullOrBlank()) {
                            IconButton(onClick = {
                                storeLogoUri = null
                                authPreferences.storeLogoUri = null
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Logo", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // QRIS Store Image Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Foto / Kode QRIS Pembayaran Toko",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkSlate
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Foto QRIS ini akan otomatis dimunculkan saat kasir memilih metode pembayaran QRIS di transaksi.",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // QRIS Preview Box
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!storeQrisUri.isNullOrBlank()) {
                            AsyncImage(
                                model = File(storeQrisUri!!),
                                contentDescription = "QRIS Toko",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = null,
                                    modifier = Modifier.size(44.dp),
                                    tint = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Belum Ada QRIS",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Upload Buttons for QRIS
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { qrisCameraLauncher.launch(null) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("store_qris_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepRoyalBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Foto QRIS", fontSize = 12.sp, color = DeepRoyalBlue)
                        }

                        OutlinedButton(
                            onClick = {
                                qrisPhotoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("store_qris_gallery_button")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp), tint = VibrantBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Galeri", fontSize = 12.sp, color = VibrantBlue)
                        }

                        if (!storeQrisUri.isNullOrBlank()) {
                            IconButton(onClick = {
                                storeQrisUri = null
                                authPreferences.storeQrisImageUri = null
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus QRIS", tint = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Fields Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Detail Informasi Bisnis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = DarkSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Store Name Field
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = {
                            storeName = it
                            errorMessage = null
                        },
                        label = { Text("Nama Toko / Bisnis") },
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

                    // Store Address Field
                    OutlinedTextField(
                        value = storeAddress,
                        onValueChange = {
                            storeAddress = it
                            errorMessage = null
                        },
                        label = { Text("Alamat Lengkap Toko") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = VibrantBlue) },
                        singleLine = false,
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_address_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone / WhatsApp Field
                    OutlinedTextField(
                        value = storePhone,
                        onValueChange = {
                            storePhone = it
                            errorMessage = null
                        },
                        label = { Text("Nomor Telepon / WhatsApp") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = VibrantBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("store_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantBlue,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Receipt Footer Field
                    OutlinedTextField(
                        value = receiptFooter,
                        onValueChange = { receiptFooter = it },
                        label = { Text("Catatan Kaki Struk (Footer)") },
                        singleLine = false,
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            val trimmedName = storeName.trim()
                            val trimmedAddr = storeAddress.trim()
                            val trimmedPhone = storePhone.trim()

                            if (trimmedName.isBlank()) {
                                errorMessage = "Nama toko tidak boleh kosong."
                                return@Button
                            }

                            authPreferences.saveStoreProfile(
                                name = trimmedName,
                                address = trimmedAddr,
                                phone = trimmedPhone,
                                logoUri = storeLogoUri,
                                qrisUri = storeQrisUri
                            )

                            val profile = StoreProfile(
                                storeName = trimmedName,
                                address = trimmedAddr,
                                phone = trimmedPhone,
                                logoUri = storeLogoUri,
                                qrisImageUri = storeQrisUri,
                                receiptFooter = receiptFooter
                            )
                            onProfileSaved(profile)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_store_profile_button")
                    ) {
                        Text("Lanjut ke Panduan Interaktif", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = CrispWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

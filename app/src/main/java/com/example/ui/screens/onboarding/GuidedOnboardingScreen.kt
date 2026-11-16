package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessType
import com.example.ui.PosTab
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue

data class TutorialStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val description: String,
    val tips: List<String>,
    val targetTab: PosTab,
    val actionLabel: String
)

@Composable
fun GuidedOnboardingScreen(
    businessType: BusinessType = BusinessType.OFFLINE,
    onCompleteTutorial: (targetTab: PosTab?) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = remember(businessType) {
        when (businessType) {
            BusinessType.RESTAURANT -> listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Kelola Daftar Menu Resto & Cafe",
                    subtitle = "Input makanan, minuman, foto menu & stok bahan",
                    icon = Icons.Default.RestaurantMenu,
                    iconColor = DeepRoyalBlue,
                    description = "Susun buku menu digital Anda dengan foto asli, harga jual, HPP modal, dan kategori makanan/minuman tanpa kerumitan barcode.",
                    tips = listOf(
                        "Tambahkan foto menu langsung dari kamera atau galeri.",
                        "Kelompokkan berdasarkan kategori seperti Kopi, Makanan Berat, Snack.",
                        "Stok porsi berkurang otomatis setiap transaksi selesai."
                    ),
                    targetTab = PosTab.CATALOG,
                    actionLabel = "Buka Daftar Menu"
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Generate QR Code per Meja",
                    subtitle = "Cetak stand QR meja untuk pemesanan mandiri tamu",
                    icon = Icons.Default.QrCode,
                    iconColor = Color(0xFFD97706),
                    description = "Setiap meja memiliki QR Code unik (Meja 1, Meja 2, VIP, dll). Saat tamu memesan dari QR, kasir langsung mengetahui nomor mejanya.",
                    tips = listOf(
                        "Pilih nomor meja lalu cetak atau simpan gambar stand QR.",
                        "Atur jumlah meja dan info Wi-Fi restoran di menu Pengaturan.",
                        "Pantau status meja kosong atau terisi secara real-time."
                    ),
                    targetTab = PosTab.BARCODE_ENGINE,
                    actionLabel = "Buka Generator QR Meja"
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Kasir & Manajemen Pesanan Meja",
                    subtitle = "Simpan tagihan meja aktif, catat request dapur & bayar",
                    icon = Icons.Default.Restaurant,
                    iconColor = EmeraldGreen,
                    description = "Pilih meja di kasir untuk menambah menu, menyimpan pesanan sementara (open bill), atau memproses pembayaran saat tamu selesai.",
                    tips = listOf(
                        "Beralih antar meja secara instan tanpa kehilangan pesanan.",
                        "Tambahkan catatan dapur per item (misal: Less Sugar / Extra Pedas).",
                        "Cetak struk meja lengkap dengan nomor meja & rincian biaya."
                    ),
                    targetTab = PosTab.CASHIER,
                    actionLabel = "Buka Kasir & Meja"
                )
            )
            BusinessType.ONLINE -> listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Input Produk Toko Online",
                    subtitle = "Lengkapi katalog dengan foto produk, SKU & stok gudang",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = DeepRoyalBlue,
                    description = "Kelola stok produk olshop secara akurat agar tidak terjadi overselling saat pesanan ramai masuk.",
                    tips = listOf(
                        "Tambahkan foto produk, harga jual & batas stok minimum.",
                        "Atur harga grosir otomatis untuk pembeli reseller.",
                        "Kelola stok masuk dan keluar lewat menu Stok & Opname."
                    ),
                    targetTab = PosTab.CATALOG,
                    actionLabel = "Buka Katalog Produk"
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Cetak Label & Resi Pengiriman",
                    subtitle = "Buat label kemasan & cek kode SKU untuk packing cepat",
                    icon = Icons.Default.QrCode,
                    iconColor = VibrantBlue,
                    description = "Generate label barcode/QR paket atau SKU produk untuk mempercepat proses sortir dan pengiriman kurir.",
                    tips = listOf(
                        "Pilih kurir default toko online Anda di menu Pengaturan.",
                        "Cetak label ukuran 58mm, 80mm, atau simpan sebagai gambar."
                    ),
                    targetTab = PosTab.BARCODE_ENGINE,
                    actionLabel = "Buka Pusat Label"
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Proses Order & Invoice Online",
                    subtitle = "Catat alamat pembeli, ekspedisi, ongkir & cetak nota",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = EmeraldGreen,
                    description = "Buat pesanan online lengkap dengan info penerima, kurir ekspedisi, ongkos kirim, serta pesan khusus unboxing di nota.",
                    tips = listOf(
                        "Dukung pembayaran QRIS Toko, Transfer Bank, dan COD.",
                        "Bagikan struk/invoice digital langsung via WhatsApp ke pembeli."
                    ),
                    targetTab = PosTab.CASHIER,
                    actionLabel = "Buka Halaman Pesanan"
                )
            )
            BusinessType.OFFLINE -> listOf(
                TutorialStep(
                    stepNumber = 1,
                    title = "Tambah Produk Pertama Anda",
                    subtitle = "Input data riil, foto produk, harga modal, harga jual & stok",
                    icon = Icons.Default.ShoppingBag,
                    iconColor = DeepRoyalBlue,
                    description = "Input barang dagangan Anda secara presisi. Anda dapat melampirkan foto nyata menggunakan kamera smartphone atau memilih dari galeri lokal.",
                    tips = listOf(
                        "Gunakan tombol Kamera untuk memotret fisik produk langsung.",
                        "Isi Harga Beli (HPP) dan Harga Jual untuk menghitung margin keuntungan otomatis.",
                        "SKU dan Barcode dapat di-generate otomatis atau menggunakan kode pabrik."
                    ),
                    targetTab = PosTab.CATALOG,
                    actionLabel = "Buka Katalog & Tambah Produk"
                ),
                TutorialStep(
                    stepNumber = 2,
                    title = "Generator & Cetak Label Barcode",
                    subtitle = "Generate Code128 / EAN-13 & stream ke printer Bluetooth",
                    icon = Icons.Default.QrCode,
                    iconColor = VibrantBlue,
                    description = "Buat label barcode vektor beresolusi tajam untuk ditempelkan pada kemasan produk atau rak toko. Mendukung preset kertas termal 58mm dan 80mm.",
                    tips = listOf(
                        "Pilih produk dari katalog untuk langsung mengisikan data barcode.",
                        "Atur format barcode: Code 128 (alfanumerik) atau EAN-13 (standar ritel).",
                        "Tekan 'Cetak Printer' atau simpan label sebagai gambar."
                    ),
                    targetTab = PosTab.BARCODE_ENGINE,
                    actionLabel = "Buka Barcode Generator"
                ),
                TutorialStep(
                    stepNumber = 3,
                    title = "Scan Barcode Kamera untuk Kasir Cepat",
                    subtitle = "Kasir real-time tanpa alat scanner tambahan",
                    icon = Icons.Default.QrCodeScanner,
                    iconColor = EmeraldGreen,
                    description = "Gunakan kamera bawaan perangkat sebagai barcode scanner berkecepatan tinggi. Cukup arahkan kamera ke barcode produk, keranjang kasir akan terisi otomatis.",
                    tips = listOf(
                        "Buka Kasir lalu tekan tombol Scan untuk mengaktifkan kamera.",
                        "Sistem otomatis mengenali produk dan menambahkan kuantitas.",
                        "Dukung pembayaran Tunai dan QRIS Toko serta manajemen shift kasir."
                    ),
                    targetTab = PosTab.CASHIER,
                    actionLabel = "Buka Kasir & Mulai Berjualan"
                )
            )
        }
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = steps[currentStepIndex]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SoftGrayBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PANDUAN MEMULAI SISTEM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepRoyalBlue,
                    letterSpacing = 1.sp
                )

                TextButton(onClick = onSkip) {
                    Text(
                        text = "Lewati Panduan",
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Step Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                steps.forEachIndexed { index, _ ->
                    val isActive = index == currentStepIndex
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isActive) 28.dp else 10.dp)
                            .clip(CircleShape)
                            .background(if (isActive) DeepRoyalBlue else Color(0xFFCBD5E1))
                    )
                    if (index < steps.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Step Content Animated Card
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    if (targetState.stepNumber > initialState.stepNumber) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                },
                label = "stepAnimation",
                modifier = Modifier.weight(1f)
            ) { step ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CrispWhite),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Step Icon
                        Box(
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .background(step.iconColor.copy(alpha = 0.12f))
                                .border(2.dp, step.iconColor.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = null,
                                tint = step.iconColor,
                                modifier = Modifier.size(38.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Langkah ${step.stepNumber} dari ${steps.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = step.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = step.subtitle,
                            fontSize = 13.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Description Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(SoftGrayBg)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = step.description,
                                fontSize = 13.sp,
                                color = DarkSlate,
                                lineHeight = 18.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Tips Checklist
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Tips & Fitur Praktis:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )

                            step.tips.forEach { tip ->
                                Row(
                                    verticalAlignment = Alignment.Top,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = EmeraldGreen,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(top = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = tip,
                                        fontSize = 12.sp,
                                        color = Color(0xFF475569),
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Direct Action CTA for this step
                        OutlinedButton(
                            onClick = { onCompleteTutorial(step.targetTab) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                        ) {
                            Icon(step.icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepRoyalBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(step.actionLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeepRoyalBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Navigation Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStepIndex > 0) {
                    OutlinedButton(
                        onClick = { currentStepIndex-- },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Sebelumnya", fontSize = 13.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (currentStepIndex < steps.size - 1) {
                    Button(
                        onClick = { currentStepIndex++ },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue, contentColor = CrispWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("onboarding_next_button")
                    ) {
                        Text("Langkah Berikutnya", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CrispWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Button(
                        onClick = { onCompleteTutorial(PosTab.CASHIER) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen, contentColor = CrispWhite),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("onboarding_finish_button")
                    ) {
                        Text("Mulai Berjualan Sekarang!", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = CrispWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.Default.PointOfSale, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

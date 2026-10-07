package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import com.example.util.PosPrinterManager
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BusinessType
import com.example.data.model.CartItem
import com.example.data.model.OrderEntity
import com.example.data.model.StoreProfile
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DangerLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.util.CurrencyFormatter
import com.example.util.EscPosPrinterHelper

@Composable
fun ThermalReceiptDialog(
    order: OrderEntity,
    items: List<CartItem>,
    store: StoreProfile,
    onDismiss: () -> Unit,
    onPrintSuccess: (String) -> Unit,
    onUpdateOrderNote: ((String) -> Unit)? = null
) {
    val context = LocalContext.current

    // Live state for customized message on receipt
    var customMessage by remember(order.orderId) { mutableStateOf(order.orderNote) }

    val currentOrder = remember(order, customMessage) {
        order.copy(orderNote = customMessage)
    }

    val receiptText = remember(currentOrder, items, store, customMessage) {
        EscPosPrinterHelper.formatReceiptText(
            order = currentOrder,
            items = items,
            store = store,
            is80mm = store.printerPaperWidth == "80mm",
            customReceiptNote = customMessage
        )
    }

    val isOnline = store.businessType == BusinessType.ONLINE

    val quickTemplates = remember(store.storeName, store.instagram, isOnline) {
        if (isOnline) {
            listOf(
                "📦 Terima kasih sudah order di toko kami! Paket segera dikirim, mohon video unboxing ya kak ✨",
                "💖 Semoga suka dengan pesanannya! Ditunggu repeat order berikutnya kak~",
                "🚚 Paket dikemas dengan bubble wrap aman & rapi. Selamat sampai tujuan!",
                "⭐ Jangan lupa review bintang 5 dan tag IG kami ya! Terima kasih banyak ❤️",
                "🎁 Ada bonus spesial di dalam paket! Terima kasih telah mendukung olshop kami 🙏"
            )
        } else {
            listOf(
                "Terima kasih banyak, semoga harinya menyenangkan! 😊",
                "Selamat menikmati! Ditunggu kedatangannya kembali ya ✨",
                "Tunjukkan struk ini untuk diskon 10% di kunjungan berikutnya! 🎉",
                if (store.instagram.isNotBlank()) "Tag foto belanjaanmu ke IG ${store.instagram}! ⭐" else "Terima kasih sudah berbelanja di toko kami! ❤️",
                "Senang melayani Anda hari ini! Sehat dan sukses selalu ya 🙏"
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .clip(RoundedCornerShape(16.dp))
            .testTag("thermal_receipt_dialog"),
        confirmButton = {},
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge & Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(EmeraldGreen.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isOnline) "Pesanan Online Siap!" else "Transaksi Berhasil!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = DarkSlate
                            )
                            Text(
                                text = if (isOnline) {
                                    val safeCust = if (order.customerName.isBlank() || order.customerName.contains("Walk-In", ignoreCase = true)) "Pelanggan Online" else order.customerName
                                    "No: ${order.orderId} • Penerima: $safeCust"
                                } else {
                                    "No: ${order.orderId} • ${order.customerName}"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // INTERACTIVE SECTION: Custom Message for Customer on Receipt
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = DeepRoyalBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline) "Pesan Khusus di Invoice / Label" else "Pesan Khusus di Struk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = DeepRoyalBlue
                                )
                            }
                            Surface(
                                color = Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "Bisa Custom Bebas",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepRoyalBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isOnline) {
                                "Ketik pesan hangat, ucapan terima kasih paket, atau instruksi unboxing untuk pembeli online:"
                            } else {
                                "Ketik pesan hangat, sapaan personal, atau info promo agar hubungan dengan customer semakin dekat:"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = customMessage,
                            onValueChange = { newText ->
                                customMessage = newText
                                onUpdateOrderNote?.invoke(newText)
                            },
                            placeholder = {
                                Text(
                                    if (isOnline) {
                                        "Ketik pesan bebas di sini (misal: Terima kasih Kak Sarah, paket segera meluncur! Ditunggu bintang 5 ya ❤️)"
                                    } else {
                                        "Ketik pesan bebas di sini (misal: Terima kasih Kak Sarah, semoga harinya menyenangkan! ❤️)"
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            },
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("receipt_custom_message_input")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Template Shortcut Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickTemplates.forEach { template ->
                                FilterChip(
                                    selected = customMessage == template,
                                    onClick = {
                                        customMessage = template
                                        onUpdateOrderNote?.invoke(template)
                                    },
                                    label = {
                                        Text(
                                            text = template.take(32) + if (template.length > 32) "…" else "",
                                            fontSize = 10.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepRoyalBlue,
                                        selectedLabelColor = CrispWhite,
                                        containerColor = CrispWhite
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                            if (customMessage.isNotBlank()) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        customMessage = ""
                                        onUpdateOrderNote?.invoke("")
                                    },
                                    label = {
                                        Text("Hapus Pesan", fontSize = 10.sp, color = Color(0xFFDC2626))
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xFFFEE2E2))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prominent Refund Notice Banner for Online Store
                if (isOnline) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, DangerRed, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = DangerLight),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "!!PERHATIAN JANGAN SAMPAI HILANG STRUK INI DAN SERTAKAN VIDEO UNBOXING KETIKA INGIN REFUND!!",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.5.sp,
                                color = DangerRed,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Realistic Paper Receipt View
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFF8)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Store Header
                        Text(
                            text = store.storeName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DarkSlate,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = store.address,
                            fontSize = 11.sp,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Telp: ${store.phone}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        if (store.instagram.isNotBlank()) {
                            Text(
                                text = store.instagram,
                                fontSize = 10.sp,
                                color = DeepRoyalBlue
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFF94A3B8), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Receipt Details Monospace with custom message rendered live
                        Text(
                            text = receiptText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            color = Color(0xFF1E293B),
                            lineHeight = 15.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: System Print, Download, Share, Bluetooth
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. System Print (Printer Android & Simpan PDF)
                    Button(
                        onClick = {
                            PosPrinterManager.printReceiptToSystem(
                                context = context,
                                order = currentOrder,
                                items = items,
                                store = store,
                                is80mm = store.printerPaperWidth == "80mm",
                                customReceiptNote = customMessage
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Printer / PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrispWhite)
                    }

                    // 2. Download / Simpan Gambar Struk
                    Button(
                        onClick = {
                            PosPrinterManager.downloadReceipt(
                                context = context,
                                order = currentOrder,
                                items = items,
                                store = store,
                                is80mm = store.printerPaperWidth == "80mm",
                                customReceiptNote = customMessage
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download Struk", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = CrispWhite)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 3. Share WhatsApp / Teks
                    OutlinedButton(
                        onClick = {
                            EscPosPrinterHelper.shareReceipt(context, receiptText, order.orderId)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = DarkSlate, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim WhatsApp", color = DarkSlate, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    }

                    // 4. Bluetooth Thermal Stream
                    OutlinedButton(
                        onClick = {
                            val bytes = EscPosPrinterHelper.generateEscPosBytes(
                                order = currentOrder,
                                items = items,
                                store = store,
                                is80mm = store.printerPaperWidth == "80mm",
                                customReceiptNote = customMessage
                            )
                            onPrintSuccess("Stream ESC/POS (${bytes.size} bytes) terkirim ke Thermal!")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Thermal Bluetooth", color = DeepRoyalBlue, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = DarkSlate),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Selesai / Transaksi Baru", color = DarkSlate, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    )
}


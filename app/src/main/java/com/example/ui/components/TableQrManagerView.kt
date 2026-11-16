package com.example.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.PosTab
import com.example.ui.PosViewModel
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.util.BarcodeGenerator
import com.example.util.CurrencyFormatter
import com.example.util.PosPrinterManager
import com.google.zxing.BarcodeFormat as ZxingFormat

@Composable
fun TableQrManagerView(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val draftOrders by viewModel.draftOrders.collectAsStateWithLifecycle()
    val selectedTable by viewModel.selectedTable.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val tableList = remember(storeProfile.totalTables) {
        viewModel.getRestaurantTables(storeProfile.totalTables)
    }

    var activeTableForQr by remember(selectedTable, tableList) {
        mutableStateOf(if (tableList.contains(selectedTable)) selectedTable else tableList.first())
    }

    val qrUrl = remember(activeTableForQr, storeProfile.storeName) {
        val sanitizedStore = storeProfile.storeName.trim().replace(" ", "-").lowercase().ifBlank { "resto" }
        val sanitizedTable = activeTableForQr.trim().replace(" ", "-").lowercase()
        "https://menu.voravio.com/resto/$sanitizedStore?table=$sanitizedTable"
    }

    var qrBitmap by remember(qrUrl) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(qrUrl) {
        qrBitmap = try {
            BarcodeGenerator.generateBarcodeBitmapWithZxing(
                contents = qrUrl,
                format = ZxingFormat.QR_CODE,
                width = 512,
                height = 512
            )
        } catch (_: Exception) {
            null
        }
    }

    val activeDraft = draftOrders.find { it.draftTag.equals(activeTableForQr, ignoreCase = true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftGrayBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "QR Meja Restoran",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate
                )
                Text(
                    text = "Generate QR pemesanan mandiri & pantau nomor meja",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFEF3C7)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = "Mode Restoran",
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(tableList) { table ->
                val isSelected = table.equals(activeTableForQr, ignoreCase = true)
                val isOccupied = draftOrders.any { it.draftTag.equals(table, ignoreCase = true) }

                Surface(
                    onClick = {
                        activeTableForQr = table
                    },
                    shape = RoundedCornerShape(20.dp),
                    color = when {
                        isSelected -> DeepRoyalBlue
                        isOccupied -> Color(0xFFFEF3C7)
                        else -> CrispWhite
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = when {
                            isSelected -> DeepRoyalBlue
                            isOccupied -> Color(0xFFF59E0B)
                            else -> Color(0xFFE2E8F0)
                        }
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> CrispWhite
                                        isOccupied -> Color(0xFFF59E0B)
                                        else -> EmeraldGreen
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = table,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isSelected -> CrispWhite
                                isOccupied -> Color(0xFF92400E)
                                else -> DarkSlate
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Minimalist QR Stand Card Preview
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CrispWhite),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = storeProfile.storeName.ifBlank { "RESTORAN & CAFE" }.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepRoyalBlue,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = activeTableForQr.uppercase(),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkSlate
                )

                Text(
                    text = if (activeDraft != null) {
                        "Sedang Terisi • Tagihan ${CurrencyFormatter.formatRupiah(activeDraft.grandTotal)}"
                    } else {
                        "Scan QR untuk Pesan Mandiri dari Meja"
                    },
                    fontSize = 12.sp,
                    color = if (activeDraft != null) Color(0xFFB45309) else Color(0xFF64748B),
                    fontWeight = if (activeDraft != null) FontWeight.SemiBold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (qrBitmap != null) {
                        Image(
                            bitmap = qrBitmap!!.asImageBitmap(),
                            contentDescription = "QR $activeTableForQr",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Pesanan dari QR ini otomatis menandai nomor meja: $activeTableForQr",
                    fontSize = 11.sp,
                    color = Color(0xFF475569),
                    textAlign = TextAlign.Center
                )

                if (storeProfile.wifiName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val wifiLabel = if (storeProfile.wifiPassword.isNotBlank()) {
                                "Wi-Fi: ${storeProfile.wifiName} • Pass: ${storeProfile.wifiPassword}"
                            } else {
                                "Free Wi-Fi: ${storeProfile.wifiName}"
                            }
                            Text(
                                text = wifiLabel,
                                fontSize = 11.sp,
                                color = DarkSlate,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PosPrinterManager.printTableQrStand(context, storeProfile, activeTableForQr, qrUrl)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cetak Stand", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.switchTable(activeTableForQr)
                            viewModel.setTab(PosTab.CASHIER)
                            Toast.makeText(context, "Membuka kasir $activeTableForQr", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(48.dp)
                    ) {
                        Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepRoyalBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buka Kasir", fontSize = 12.sp, color = DeepRoyalBlue, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            PosPrinterManager.downloadTableQrStand(context, storeProfile, activeTableForQr, qrUrl)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan PNG", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Menu ${storeProfile.storeName.ifBlank { "Restoran" }} ($activeTableForQr): $qrUrl"
                                )
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Bagikan Link $activeTableForQr"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bagikan Link", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Status Denah Meja (${tableList.size})",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = DarkSlate
        )
        Text(
            text = "Ketuk meja untuk generate QR atau membuka pesanan",
            fontSize = 11.sp,
            color = Color(0xFF64748B)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Clean non-nested-scrolling 2-column grid of tables
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tableList.chunked(2).forEach { rowTables ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowTables.forEach { table ->
                        val draft = draftOrders.find { it.draftTag.equals(table, ignoreCase = true) }
                        val isOccupied = draft != null
                        val isCurrent = table.equals(activeTableForQr, ignoreCase = true)

                        Card(
                            onClick = { activeTableForQr = table },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFFEFF6FF) else CrispWhite
                            ),
                            border = BorderStroke(
                                width = if (isCurrent) 1.5.dp else 1.dp,
                                color = when {
                                    isCurrent -> DeepRoyalBlue
                                    isOccupied -> Color(0xFFF59E0B)
                                    else -> Color(0xFFE2E8F0)
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = table,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = DarkSlate
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isOccupied) Color(0xFFF59E0B) else EmeraldGreen)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                if (isOccupied && draft != null) {
                                    Text(
                                        text = "Terisi",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = CurrencyFormatter.formatRupiah(draft.grandTotal),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepRoyalBlue
                                    )
                                } else {
                                    Text(
                                        text = "Kosong",
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                    if (rowTables.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

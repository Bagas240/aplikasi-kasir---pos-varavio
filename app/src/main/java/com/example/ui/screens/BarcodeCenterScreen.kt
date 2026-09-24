package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import android.widget.Toast
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.ui.PosViewModel
import com.example.ui.components.A4StickerSheetPreview
import com.example.ui.components.BarcodeDisplay
import com.example.ui.components.BarcodeScannerView
import com.example.ui.components.SingleBarcodeTagCanvas
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.BarcodeFormat
import com.example.util.BarcodeGeneratorService
import com.example.util.CurrencyFormatter

@Composable
fun BarcodeCenterScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val selectedProduct by viewModel.selectedProductForBarcode.collectAsStateWithLifecycle()
    val barcodeFormat by viewModel.barcodeFormat.collectAsStateWithLifecycle()
    val customBarcodeCode by viewModel.customBarcodeCode.collectAsStateWithLifecycle()
    val printLayoutPreset by viewModel.printLayoutPreset.collectAsStateWithLifecycle()
    val printMessage by viewModel.printSimulationMessage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var printQty by remember { mutableStateOf(1) }
    var isCameraScannerOpen by remember { mutableStateOf(false) }

    // Auto-select first product if none selected
    val activeProduct = selectedProduct ?: products.firstOrNull() ?: Product(
        name = "Sample Item",
        sku = "SMP-001",
        barcode = "899275321001",
        category = "Retail",
        buyPrice = 10000.0,
        sellPrice = 20000.0,
        stock = 50
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SoftGrayBg)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Dynamic Barcode & Label Engine",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkSlate
                )
                Text(
                    text = "Generator barcode standar & cetak label thermal ESC/POS",
                    fontSize = 12.sp,
                    color = Color(0xFF64748B)
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepRoyalBlue, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(24.dp))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // CameraX Scanner Overlay for Product Barcode Identification
        if (isCameraScannerOpen) {
            BarcodeScannerView(
                products = products,
                onScan = { scannedCode ->
                    viewModel.setCustomBarcodeCode(scannedCode)
                    val matched = com.example.util.BarcodeItemMatcher.matchProduct(scannedCode, products)
                    if (matched != null) {
                        viewModel.selectProductForBarcode(matched)
                    }
                    isCameraScannerOpen = false
                },
                onClose = { isCameraScannerOpen = false },
                modifier = Modifier.padding(bottom = 14.dp)
            )
        }

        // Step 1: Select Product from Catalog
        Text("1. Pilih Produk dari Katalog:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            items(products) { prod ->
                val isSelected = prod.id == activeProduct.id
                Card(
                    modifier = Modifier
                        .width(150.dp)
                        .clickable { viewModel.selectProductForBarcode(prod) }
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) DeepRoyalBlue else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFEFF6FF) else CrispWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(prod.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
                        Text("SKU: ${prod.sku}", fontSize = 10.sp, color = Color(0xFF64748B))
                        Text(CurrencyFormatter.formatRupiah(prod.sellPrice), fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = DeepRoyalBlue)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 2: Barcode Format & Custom Code
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrispWhite),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("2. Konfigurasi Barcode & Simbologi:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    BarcodeFormat.values().forEach { fmt ->
                        val isSel = fmt == barcodeFormat
                        Button(
                            onClick = { viewModel.setBarcodeFormat(fmt) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) DeepRoyalBlue else Color(0xFFF1F5F9)
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = when (fmt) {
                                    BarcodeFormat.CODE_128 -> "Code 128"
                                    BarcodeFormat.EAN_13 -> "EAN-13"
                                    BarcodeFormat.QR_CODE -> "QR 2D"
                                },
                                fontSize = 11.sp,
                                color = if (isSel) CrispWhite else DarkSlate,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Custom code input with randomize button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customBarcodeCode.ifBlank { activeProduct.barcode.ifBlank { activeProduct.sku } },
                        onValueChange = { viewModel.setCustomBarcodeCode(it) },
                        label = { Text("Kode Barcode / Alphanumeric") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            val generated = if (barcodeFormat == BarcodeFormat.EAN_13) {
                                BarcodeGeneratorService.instance.generateEan13("899")
                            } else {
                                BarcodeGeneratorService.instance.generateCode128(prefix = "POS")
                            }
                            viewModel.setCustomBarcodeCode(generated)
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Autorenew, contentDescription = "Acak", tint = DeepRoyalBlue)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { isCameraScannerOpen = !isCameraScannerOpen },
                        modifier = Modifier
                            .size(50.dp)
                            .background(if (isCameraScannerOpen) EmeraldGreen else Color(0xFFEFF6FF), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = "Scan dengan Kamera",
                            tint = if (isCameraScannerOpen) CrispWhite else DeepRoyalBlue
                        )
                    }
                }

                // Generate from Product ID with ZXing
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Generate Barcode dari Product ID (ZXing)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = DarkSlate
                                )
                                Text(
                                    text = if (activeProduct.barcode.isBlank())
                                        "Produk tidak memiliki barcode pabrik. Buat barcode otomatis dari ID #${activeProduct.id}"
                                    else
                                        "ID Produk: #${activeProduct.id} • Format: ${barcodeFormat.label}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    val generatedCode = BarcodeGeneratorService.instance.generateBarcodeStringFromProductId(
                                        productId = activeProduct.id,
                                        format = barcodeFormat
                                    )
                                    viewModel.setCustomBarcodeCode(generatedCode)
                                    viewModel.saveProduct(activeProduct.copy(barcode = generatedCode))
                                    Toast.makeText(context, "Barcode ZXing dibuat dari ID: $generatedCode", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("generate_barcode_from_id_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Generate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 3: Print Layout Presets
        Text("3. Pilih Layout Cetak & Ukuran Label:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
        Spacer(modifier = Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf(
                "58mm" to "58mm Thermal Label",
                "80mm" to "80mm Thermal Label",
                "A4_GRID" to "A4 Lembar Stiker (30x)"
            ).forEach { (presetKey, presetLabel) ->
                val isSel = presetKey == printLayoutPreset
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setPrintLayoutPreset(presetKey) }
                        .border(
                            width = if (isSel) 2.dp else 1.dp,
                            color = if (isSel) DeepRoyalBlue else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) Color(0xFFEFF6FF) else CrispWhite
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(presetLabel, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 4: Live Label Tag Canvas Preview
        Text("4. Preview Fisik Label Tag:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (printLayoutPreset == "A4_GRID") {
                A4StickerSheetPreview(
                    store = storeProfile,
                    product = activeProduct,
                    barcodeString = customBarcodeCode.ifBlank { activeProduct.barcode.ifBlank { activeProduct.sku } },
                    format = barcodeFormat
                )
            } else {
                SingleBarcodeTagCanvas(
                    store = storeProfile,
                    product = activeProduct,
                    barcodeString = customBarcodeCode.ifBlank { activeProduct.barcode.ifBlank { activeProduct.sku } },
                    format = barcodeFormat,
                    is80mm = printLayoutPreset == "80mm"
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 5: Print Actions & Quantity
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CrispWhite),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jumlah Salinan Cetak:", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = DarkSlate)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (printQty > 1) printQty-- },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.size(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("-", color = DarkSlate, fontWeight = FontWeight.Bold)
                        }
                        Text(printQty.toString(), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 12.dp))
                        Button(
                            onClick = { printQty++ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                            modifier = Modifier.size(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text("+", color = DarkSlate, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.printBarcodeLabel() },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim Cetak Label ($printQty Lembar)", fontWeight = FontWeight.Bold)
                }

                // Feedback snackbar
                printMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(msg, fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

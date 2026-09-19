package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.util.BarcodeFormat
import com.example.util.BarcodeGenerator
import com.example.util.CurrencyFormatter

@Composable
fun SingleBarcodeTagCanvas(
    store: StoreProfile,
    product: Product,
    barcodeString: String,
    format: BarcodeFormat,
    is80mm: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tagWidth = if (is80mm) 320.dp else 240.dp
    val codeToRender = barcodeString.ifBlank { product.barcode.ifBlank { product.sku } }

    val barcodeModules = remember(codeToRender, format) {
        if (format == BarcodeFormat.EAN_13) {
            BarcodeGenerator.encodeEan13(codeToRender)
        } else if (format == BarcodeFormat.CODE_128) {
            BarcodeGenerator.encodeCode128(codeToRender)
        } else {
            BooleanArray(0)
        }
    }

    val qrMatrix = remember(codeToRender, format) {
        if (format == BarcodeFormat.QR_CODE) {
            BarcodeGenerator.encodeQrMatrix(codeToRender)
        } else {
            emptyArray()
        }
    }

    Card(
        modifier = modifier
            .width(tagWidth)
            .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = CrispWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Store Brand
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Store,
                    contentDescription = null,
                    tint = DeepRoyalBlue,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = store.storeName.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepRoyalBlue,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Product Name
            Text(
                text = product.name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = DarkSlate,
                textAlign = TextAlign.Center,
                maxLines = 2
            )

            // Price in large IDR
            Text(
                text = CurrencyFormatter.formatRupiah(product.sellPrice),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF047857) // Dark Emerald
            )

            Spacer(modifier = Modifier.height(6.dp))

            // High-contrast Barcode Vector Rendering via BarcodeDisplay
            BarcodeDisplay(
                barcodeValue = codeToRender,
                format = format,
                modifier = Modifier.fillMaxWidth(),
                showText = true,
                canvasHeight = if (format == BarcodeFormat.QR_CODE) 80.dp else 48.dp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SKU: ${product.sku} • ${product.category}",
                fontSize = 9.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun A4StickerSheetPreview(
    store: StoreProfile,
    product: Product,
    barcodeString: String,
    format: BarcodeFormat,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A4 Sticker Sheet Grid Layout (3x10 = 30 Tags)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkSlate
                )
                Box(
                    modifier = Modifier
                        .background(DeepRoyalBlue, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("30 Labels/Page", color = CrispWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Simulated A4 sheet representation
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CrispWhite)
                    .border(1.dp, Color(0xFFE2E8F0))
                    .padding(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(4) { rowIdx ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            repeat(3) { colIdx ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .border(0.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(4.dp))
                                        .background(Color(0xFFFAFAFA))
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = product.name.take(14),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkSlate,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = CurrencyFormatter.formatRupiah(product.sellPrice),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF047857)
                                        )
                                        // Miniature barcode bars via BarcodeDisplay
                                        BarcodeDisplay(
                                            barcodeValue = barcodeString.ifBlank { product.barcode }.take(10),
                                            format = format,
                                            showText = false,
                                            canvasHeight = 14.dp,
                                            modifier = Modifier.fillMaxWidth(0.9f)
                                        )
                                        Text(
                                            text = barcodeString.ifBlank { product.barcode }.take(10),
                                            fontSize = 6.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF475569)
                                        )
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

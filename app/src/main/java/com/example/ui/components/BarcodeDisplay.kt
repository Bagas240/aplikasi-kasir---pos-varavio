package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.BarcodeFormat
import com.example.util.BarcodeGenerator
import com.example.util.BarcodeGeneratorService

/**
 * Composable component that renders EAN-13 and Code128 barcode strings as high-resolution
 * vector graphics on a Jetpack Compose Canvas, optimized for print preview and label sheets.
 *
 * @param barcodeValue The raw code string or identifier to encode (e.g. 13-digit EAN-13 or alphanumeric SKU).
 * @param format The symbology standard (EAN_13, CODE_128, or QR_CODE).
 * @param modifier Composable layout modifier.
 * @param showText Whether to render the human-readable text below the bars.
 * @param barColor Vector bar color (defaults to solid black for maximum thermal print scan contrast).
 * @param backgroundColor Canvas background color (defaults to crisp white).
 * @param canvasHeight Height of the barcode canvas portion.
 * @param service BarcodeGeneratorService instance used to calculate check digits and module bit arrays.
 */
@Composable
fun BarcodeDisplay(
    barcodeValue: String,
    format: BarcodeFormat = BarcodeFormat.CODE_128,
    modifier: Modifier = Modifier,
    showText: Boolean = true,
    barColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    canvasHeight: Dp = 52.dp,
    service: BarcodeGeneratorService = remember { BarcodeGeneratorService.instance }
) {
    val cleanCode = remember(barcodeValue, format) {
        when (format) {
            BarcodeFormat.EAN_13 -> {
                val digits = barcodeValue.filter { it.isDigit() }
                if (digits.length == 13 && service.validateEan13(digits)) {
                    digits
                } else if (digits.length >= 12) {
                    service.generateEan13FromDigits(digits.take(12))
                } else if (digits.isNotEmpty()) {
                    service.generateEan13FromDigits(digits)
                } else {
                    service.generateEan13("899")
                }
            }
            BarcodeFormat.CODE_128 -> {
                barcodeValue.ifBlank { "PRD-000001" }
            }
            BarcodeFormat.QR_CODE -> {
                barcodeValue.ifBlank { "FORAPOS-CODE" }
            }
        }
    }

    val barcodeModules = remember(cleanCode, format) {
        when (format) {
            BarcodeFormat.EAN_13 -> service.encodeEan13(cleanCode)
            BarcodeFormat.CODE_128 -> service.encodeCode128(cleanCode)
            BarcodeFormat.QR_CODE -> BooleanArray(0)
        }
    }

    val qrMatrix = remember(cleanCode, format) {
        if (format == BarcodeFormat.QR_CODE) {
            BarcodeGenerator.encodeQrMatrix(cleanCode)
        } else {
            emptyArray()
        }
    }

    val humanReadableText = remember(cleanCode, format) {
        service.formatHumanReadable(cleanCode, format)
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("barcode_display"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (format == BarcodeFormat.QR_CODE) {
            // 2D QR Matrix Rendering
            Canvas(
                modifier = Modifier
                    .size(canvasHeight * 1.8f)
                    .background(backgroundColor)
                    .testTag("barcode_canvas_qr")
            ) {
                BarcodeGenerator.drawQrOnCanvas(
                    drawScope = this,
                    matrix = qrMatrix,
                    canvasWidth = size.width,
                    canvasHeight = size.height,
                    moduleColor = barColor
                )
            }
        } else {
            // 1D Linear Barcode Canvas Rendering
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(canvasHeight)
                    .background(backgroundColor)
                    .testTag("barcode_canvas_1d")
            ) {
                if (barcodeModules.isNotEmpty()) {
                    val moduleWidth = size.width / barcodeModules.size
                    for (i in barcodeModules.indices) {
                        if (barcodeModules[i]) {
                            drawRect(
                                color = barColor,
                                topLeft = Offset(i * moduleWidth, 0f),
                                size = Size(moduleWidth, size.height)
                            )
                        }
                    }
                }
            }
        }

        if (showText) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = humanReadableText,
                fontSize = if (format == BarcodeFormat.EAN_13) 12.sp else 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.SemiBold,
                color = barColor,
                textAlign = TextAlign.Center,
                letterSpacing = if (format == BarcodeFormat.EAN_13) 1.5.sp else 1.0.sp,
                modifier = Modifier.testTag("barcode_text")
            )
        }
    }
}

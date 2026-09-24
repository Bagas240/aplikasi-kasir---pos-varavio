package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import com.example.data.model.Product
import com.example.util.BarcodeItemMatcher
import com.example.util.BarcodeScanResult
import com.example.util.CurrencyFormatter
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

/**
 * CameraView component using CameraX and MLKit barcode scanning to capture product codes.
 *
 * @param onBarcodeScanned Callback invoked whenever a valid barcode / product code is captured.
 * @param modifier Composable modifier.
 * @param lastScannedCode Optional previously captured product code to display.
 * @param onClose Optional callback invoked when the user taps the close button.
 * @param sampleCodes Optional list of quick-test product codes (e.g. for emulator testing without physical barcode).
 */
@Composable
fun CameraView(
    onBarcodeScanned: (String) -> Unit,
    modifier: Modifier = Modifier,
    lastScannedCode: String? = null,
    onClose: (() -> Unit)? = null,
    sampleCodes: List<String> = emptyList(),
    products: List<Product> = emptyList(),
    onBarcodeResult: ((BarcodeScanResult) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    var useFrontCamera by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var cameraControlRef by remember { mutableStateOf<CameraControl?>(null) }
    var manualInputCode by remember { mutableStateOf("") }
    var showManualField by remember { mutableStateOf(false) }

    // Real-time captured feedback state
    var activeCapturedCode by remember { mutableStateOf(lastScannedCode ?: "") }
    var mappedProduct by remember {
        mutableStateOf(
            if (!lastScannedCode.isNullOrBlank() && products.isNotEmpty()) {
                BarcodeItemMatcher.matchProduct(lastScannedCode, products)
            } else null
        )
    }
    var lastScannedTimestamp by remember { mutableLongStateOf(0L) }
    var showCapturedBadge by remember { mutableStateOf(false) }

    // Keep activeCapturedCode in sync if parent passes an updated one
    LaunchedEffect(lastScannedCode) {
        if (!lastScannedCode.isNullOrBlank()) {
            activeCapturedCode = lastScannedCode
            mappedProduct = if (products.isNotEmpty()) BarcodeItemMatcher.matchProduct(lastScannedCode, products) else null
            showCapturedBadge = true
        }
    }

    // Auto-dismiss the visual flash overlay badge after 1.5 seconds
    LaunchedEffect(showCapturedBadge) {
        if (showCapturedBadge) {
            delay(1500)
            showCapturedBadge = false
        }
    }

    val barcodeScanner = remember {
        val barcodeOptions = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
            .build()
        BarcodeScanning.getClient(barcodeOptions)
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            try {
                barcodeScanner.close()
            } catch (_: Exception) {}
            cameraExecutor.shutdown()
        }
    }

    // Viewfinder laser beam animation
    val infiniteTransition = rememberInfiniteTransition(label = "laser_transition")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 190f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("camera_view_component"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // HEADER BAR: Title, status indicator, torch & switch controls, close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFF2563EB).copy(alpha = 0.25f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Camera Scanner",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CameraX Barcode Scanner",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ML Kit Optical Recognition",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Flashlight / Torch Toggle
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControlRef?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("camera_torch_button")
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Torch Toggle",
                            tint = if (isTorchOn) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Camera Switcher (Back / Front)
                    IconButton(
                        onClick = { useFrontCamera = !useFrontCamera },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("camera_switch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Manual Code Input Toggle
                    IconButton(
                        onClick = { showManualField = !showManualField },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("camera_manual_toggle_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "Manual Code",
                            tint = if (showManualField) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Close Button
                    if (onClose != null) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(34.dp)
                                .testTag("camera_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Scanner",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // CAMERA PREVIEW & VIEWFINDER OVERLAY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()

                                    // Preview use case with low-end optimized resolution (720p)
                                    val preview = Preview.Builder()
                                        .setTargetResolution(android.util.Size(1280, 720))
                                        .build().also {
                                            it.surfaceProvider = previewView.surfaceProvider
                                        }

                                    // ImageAnalysis use case with 720p target & latest backpressure
                                    @OptIn(ExperimentalGetImage::class)
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setTargetResolution(android.util.Size(1280, 720))
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    var lastFrameAnalysisTimestamp = 0L

                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        val now = System.currentTimeMillis()
                                        // Throttle analysis on low-end CPUs: at most 1 frame per 200ms
                                        if (now - lastFrameAnalysisTimestamp < 200L) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }
                                        lastFrameAnalysisTimestamp = now

                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val inputImage = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            barcodeScanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val rawValue = barcode.rawValue?.trim() ?: continue
                                                        if (rawValue.isBlank()) continue

                                                        // Debounce: allow scan if code changed or 1.2s passed
                                                        if (rawValue != activeCapturedCode || now - lastScannedTimestamp > 1200L) {
                                                            activeCapturedCode = rawValue
                                                            lastScannedTimestamp = now
                                                            val matched = if (products.isNotEmpty()) BarcodeItemMatcher.matchProduct(rawValue, products) else null
                                                            mappedProduct = matched
                                                            showCapturedBadge = true
                                                            val formatName = BarcodeItemMatcher.getBarcodeFormatName(barcode.format)
                                                            onBarcodeResult?.invoke(
                                                                BarcodeScanResult(
                                                                    rawCode = rawValue,
                                                                    format = barcode.format,
                                                                    formatName = formatName,
                                                                    matchedProduct = matched,
                                                                    timestamp = now
                                                                )
                                                            )
                                                            onBarcodeScanned(rawValue)
                                                        }
                                                    }
                                                }
                                                .addOnCompleteListener {
                                                    imageProxy.close()
                                                }
                                        } else {
                                            imageProxy.close()
                                        }
                                    }

                                    val cameraSelector = if (useFrontCamera) {
                                        CameraSelector.DEFAULT_FRONT_CAMERA
                                    } else {
                                        CameraSelector.DEFAULT_BACK_CAMERA
                                    }

                                    cameraProvider.unbindAll()
                                    val camera = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        cameraSelector,
                                        preview,
                                        imageAnalysis
                                    )
                                    cameraControlRef = camera.cameraControl
                                } catch (_: Exception) {
                                    // Graceful fallback for headless or emulator setups without physical camera
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("camera_preview_view")
                    )
                } else {
                    // PERMISSION REQUEST PROMPT
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(16.dp)
                            .testTag("camera_permission_request_box")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Izin Kamera Diperlukan",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Berikan izin kamera untuk memindai barcode produk secara instan",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_camera_permission_button")
                        ) {
                            Text("Izinkan Kamera", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                // VIEWFINDER RETICLE & LASER (when permission granted)
                if (hasCameraPermission) {
                    Box(
                        modifier = Modifier
                            .size(width = 240.dp, height = 150.dp)
                            .border(
                                width = 1.5.dp,
                                color = if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .testTag("camera_viewfinder_reticle")
                    ) {
                        // Corner brackets
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(18.dp)
                                .border(
                                    width = 3.dp,
                                    color = if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(topStart = 10.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(18.dp)
                                .border(
                                    width = 3.dp,
                                    color = if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(topEnd = 10.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .size(18.dp)
                                .border(
                                    width = 3.dp,
                                    color = if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(bottomStart = 10.dp)
                                )
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(18.dp)
                                .border(
                                    width = 3.dp,
                                    color = if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8),
                                    shape = RoundedCornerShape(bottomEnd = 10.dp)
                                )
                        )

                        // Animated Laser Line
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .offset(y = (laserY % 148).dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8),
                                            if (showCapturedBadge) Color(0xFF34D399) else Color(0xFF60A5FA),
                                            if (showCapturedBadge) Color(0xFF10B981) else Color(0xFF38BDF8),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    // CAPTURED PRODUCT CODE FLASH BADGE IN VIEWFINDER
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp)
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showCapturedBadge && activeCapturedCode.isNotBlank(),
                            enter = fadeIn() + slideInVertically { it / 2 },
                            exit = fadeOut() + slideOutVertically { it / 2 }
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (mappedProduct != null) Color(0xEE064E3B) else Color(0xEE1E293B),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (mappedProduct != null) Color(0xFF10B981) else Color(0xFF38BDF8)
                                ),
                                modifier = Modifier.testTag("viewfinder_captured_code_badge")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (mappedProduct != null) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = if (mappedProduct != null) Color(0xFF34D399) else Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = mappedProduct?.name ?: "Kode: $activeCapturedCode",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (mappedProduct != null) {
                                                "${CurrencyFormatter.formatRupiah(mappedProduct!!.sellPrice)} • Stok: ${mappedProduct!!.stock}"
                                            } else {
                                                "Belum Terdaftar di Katalog"
                                            },
                                            color = if (mappedProduct != null) Color(0xFF34D399) else Color(0xFF94A3B8),
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // OPTIONAL MANUAL PRODUCT CODE INPUT DRAWER
            AnimatedVisibility(visible = showManualField) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualInputCode,
                            onValueChange = { manualInputCode = it },
                            placeholder = { Text("Ketik SKU / Barcode produk...", fontSize = 12.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Ascii,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (manualInputCode.isNotBlank()) {
                                        val code = manualInputCode.trim()
                                        activeCapturedCode = code
                                        val matched = if (products.isNotEmpty()) BarcodeItemMatcher.matchProduct(code, products) else null
                                        mappedProduct = matched
                                        showCapturedBadge = true
                                        onBarcodeResult?.invoke(BarcodeScanResult(code, matchedProduct = matched))
                                        onBarcodeScanned(code)
                                        manualInputCode = ""
                                    }
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF334155),
                                cursorColor = Color(0xFF38BDF8)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("camera_manual_code_input")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (manualInputCode.isNotBlank()) {
                                    val code = manualInputCode.trim()
                                    activeCapturedCode = code
                                    val matched = if (products.isNotEmpty()) BarcodeItemMatcher.matchProduct(code, products) else null
                                    mappedProduct = matched
                                    showCapturedBadge = true
                                    onBarcodeResult?.invoke(BarcodeScanResult(code, matchedProduct = matched))
                                    onBarcodeScanned(code)
                                    manualInputCode = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("camera_manual_code_submit")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Submit Code",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // QUICK TEST PRESET CODES (Emulator & Demo Support)
            if (sampleCodes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(sampleCodes) { code ->
                        Surface(
                            onClick = {
                                activeCapturedCode = code
                                val matched = if (products.isNotEmpty()) BarcodeItemMatcher.matchProduct(code, products) else null
                                mappedProduct = matched
                                showCapturedBadge = true
                                onBarcodeResult?.invoke(BarcodeScanResult(code, matchedProduct = matched))
                                onBarcodeScanned(code)
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF1E293B),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                        ) {
                            Text(
                                text = "Scan: $code",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // FOOTER: Currently Captured Code Display
            if (activeCapturedCode.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                HorizontalDivider(color = Color(0xFF1E293B))
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("camera_captured_code_footer"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Item Terdeteksi:",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mappedProduct?.name ?: activeCapturedCode,
                            color = if (mappedProduct != null) Color(0xFF34D399) else Color(0xFF38BDF8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (mappedProduct != null) "Stok: ${mappedProduct!!.stock}" else "Belum Terdaftar",
                        color = if (mappedProduct != null) Color(0xFF10B981) else Color(0xFFF59E0B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

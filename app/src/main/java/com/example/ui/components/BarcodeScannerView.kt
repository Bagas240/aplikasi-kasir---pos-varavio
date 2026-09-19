package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.data.model.Product
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.VibrantBlue
import com.example.util.CurrencyFormatter
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import java.util.concurrent.Executors

@Composable
fun BarcodeScannerView(
    products: List<Product>,
    onScan: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
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

    // Camera settings state
    var useFrontCamera by remember { mutableStateOf(false) }
    var isTorchOn by remember { mutableStateOf(false) }
    var continuousMode by remember { mutableStateOf(true) }
    var manualInput by remember { mutableStateOf("") }

    // Last identified barcode & matched product
    var lastScannedCode by remember { mutableStateOf("") }
    var identifiedProduct by remember { mutableStateOf<Product?>(null) }
    var lastDetectionTimestamp by remember { mutableLongStateOf(0L) }
    var isSuccessHighlight by remember { mutableStateOf(false) }

    var cameraControlRef by remember { mutableStateOf<CameraControl?>(null) }

    // Executor for ML Kit image analysis
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    // Reset success highlight after 1.2 seconds
    LaunchedEffect(isSuccessHighlight) {
        if (isSuccessHighlight) {
            delay(1200)
            isSuccessHighlight = false
        }
    }

    // Animated laser beam transition
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .testTag("barcode_scanner_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar with Quick Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .background(VibrantBlue.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "CameraX Scanner",
                            tint = VibrantBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "CameraX Barcode Scanner",
                            color = CrispWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (continuousMode) "Mode: Multi-Scan Otomatis" else "Mode: Scan Tunggal",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Torch / Flashlight Button
                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControlRef?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = if (isTorchOn) Color(0xFFFBBF24) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Flip Camera (Front/Back)
                    IconButton(
                        onClick = {
                            useFrontCamera = !useFrontCamera
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Switch Camera",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Multi-Scan Toggle Button
                    Button(
                        onClick = { continuousMode = !continuousMode },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (continuousMode) EmeraldGreen else Color(0xFF334155)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(if (continuousMode) "Multi ON" else "Single", fontSize = 10.sp, color = CrispWhite)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Close Scanner
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = CrispWhite)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Camera Viewport & Real-Time Product Identification Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
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

                                    // Preview Use Case
                                    val preview = Preview.Builder().build().also {
                                        it.surfaceProvider = previewView.surfaceProvider
                                    }

                                    // ML Kit Barcode Scanner Setup
                                    val barcodeOptions = BarcodeScannerOptions.Builder()
                                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                        .build()
                                    val barcodeScanner = BarcodeScanning.getClient(barcodeOptions)

                                    // ImageAnalysis Use Case
                                    @OptIn(ExperimentalGetImage::class)
                                    val imageAnalysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()

                                    imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                        val mediaImage = imageProxy.image
                                        if (mediaImage != null) {
                                            val inputImage = InputImage.fromMediaImage(
                                                mediaImage,
                                                imageProxy.imageInfo.rotationDegrees
                                            )
                                            barcodeScanner.process(inputImage)
                                                .addOnSuccessListener { barcodes ->
                                                    for (barcode in barcodes) {
                                                        val rawValue = barcode.rawValue ?: continue
                                                        if (rawValue.isBlank()) continue

                                                        val now = System.currentTimeMillis()
                                                        // Debounce: allow scan if code changed or 1.5s passed
                                                        if (rawValue != lastScannedCode || now - lastDetectionTimestamp > 1500L) {
                                                            lastScannedCode = rawValue
                                                            lastDetectionTimestamp = now
                                                            isSuccessHighlight = true

                                                            // Identify product from catalog
                                                            val matched = products.find {
                                                                it.barcode.equals(rawValue, ignoreCase = true) ||
                                                                        it.sku.equals(rawValue, ignoreCase = true)
                                                            }
                                                            identifiedProduct = matched

                                                            // Send scanned ID to transaction state manager
                                                            onScan(rawValue)

                                                            if (!continuousMode) {
                                                                // Single scan complete
                                                                break
                                                            }
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
                                } catch (e: Exception) {
                                    // Fallback for emulator environment without physical camera hardware
                                }
                            }, ContextCompat.getMainExecutor(ctx))

                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Akses Kamera Diperlukan untuk Scanner",
                            color = CrispWhite,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(containerColor = VibrantBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Izinkan Kamera", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // High-Tech Viewfinder Reticle with Target Corners
                val reticleColor = if (isSuccessHighlight) EmeraldGreen else VibrantBlue
                Box(
                    modifier = Modifier
                        .size(230.dp, 140.dp)
                        .border(2.dp, reticleColor.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                ) {
                    // Corner targeting brackets
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(16.dp)
                            .border(3.dp, reticleColor, RoundedCornerShape(topStart = 12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .border(3.dp, reticleColor, RoundedCornerShape(topEnd = 12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .size(16.dp)
                            .border(3.dp, reticleColor, RoundedCornerShape(bottomStart = 12.dp))
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .border(3.dp, reticleColor, RoundedCornerShape(bottomEnd = 12.dp))
                    )

                    // Animated Laser Beam (Solid, No Gradient)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp)
                            .offset(y = (laserOffset * 0.7f).dp)
                            .background(if (isSuccessHighlight) EmeraldGreen else Color(0xFFEF4444))
                    )
                }

                // REAL-TIME PRODUCT IDENTIFICATION OVERLAY BADGE
                if (lastScannedCode.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Surface(
                        color = if (identifiedProduct != null) Color(0xEE064E3B) else Color(0xEE1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (identifiedProduct != null) EmeraldGreen else VibrantBlue
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = if (identifiedProduct != null) Icons.Default.CheckCircle else Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = if (identifiedProduct != null) EmeraldGreen else Color(0xFFFBBF24),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = identifiedProduct?.name ?: "Barcode: $lastScannedCode",
                                        color = CrispWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (identifiedProduct != null) {
                                            "${CurrencyFormatter.formatRupiah(identifiedProduct!!.sellPrice)} • Stok: ${identifiedProduct!!.stock} • Ditambahkan (+1)"
                                        } else {
                                            "Terkirim ke Keranjang • (Produk Baru / Belum Terdaftar)"
                                        },
                                        color = if (identifiedProduct != null) EmeraldGreen else Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Manual Barcode / SKU Input Row (Seamless fallback for physical barcode scanners / emulator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("Ketik SKU / Barcode manual...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("scanner_manual_input"),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        val input = manualInput.trim()
                        if (input.isNotBlank()) {
                            lastScannedCode = input
                            val matched = products.find {
                                it.barcode.equals(input, ignoreCase = true) ||
                                        it.sku.equals(input, ignoreCase = true)
                            }
                            identifiedProduct = matched
                            isSuccessHighlight = true
                            onScan(input)
                            manualInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("scanner_manual_submit_btn")
                ) {
                    Text("Input", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 1-Tap Quick Barcode Test Badges from active catalog
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory2,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Quick 1-Tap Scan Barcode Produk:",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(products.take(8)) { prod ->
                    val codeToScan = prod.barcode.ifBlank { prod.sku }
                    Button(
                        onClick = {
                            lastScannedCode = codeToScan
                            identifiedProduct = prod
                            isSuccessHighlight = true
                            onScan(codeToScan)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${prod.name.take(12)} (${codeToScan.takeLast(4)})",
                            fontSize = 10.sp,
                            color = CrispWhite
                        )
                    }
                }
            }
        }
    }
}

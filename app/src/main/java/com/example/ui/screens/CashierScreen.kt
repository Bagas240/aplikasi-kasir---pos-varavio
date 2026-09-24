package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CartItem
import com.example.data.model.Customer
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.ui.PosViewModel
import com.example.ui.components.BarcodeScannerView
import com.example.ui.components.CameraView
import com.example.ui.components.ExportSalesReportDialog
import com.example.ui.components.PaymentCheckoutBottomSheet
import com.example.ui.components.RecentSalesDialog
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.BarcodeGenerator
import com.example.util.CurrencyFormatter
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.io.File
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCustomer by viewModel.selectedCustomer.collectAsStateWithLifecycle()
    val customers by viewModel.customers.collectAsStateWithLifecycle()
    val draftOrders by viewModel.draftOrders.collectAsStateWithLifecycle()
    val isScannerActive by viewModel.isScannerActive.collectAsStateWithLifecycle()
    val scanFeedback by viewModel.scanFeedback.collectAsStateWithLifecycle()
    val capturedProductCodes by viewModel.capturedProductCodes.collectAsStateWithLifecycle()
    val lastScannedBarcode by viewModel.lastScannedBarcode.collectAsStateWithLifecycle()
    val taxEnabled by viewModel.taxEnabled.collectAsStateWithLifecycle()
    val serviceEnabled by viewModel.serviceEnabled.collectAsStateWithLifecycle()
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()
    val recentSales by viewModel.recentSales.collectAsStateWithLifecycle()
    val completedOrders by viewModel.completedOrders.collectAsStateWithLifecycle()

    LaunchedEffect(scanFeedback) {
        if (scanFeedback != null) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearScanFeedback()
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }
    var showCartSheet by remember { mutableStateOf(false) }
    var showCheckoutDialog by remember { mutableStateOf(false) }
    var showDraftDialog by remember { mutableStateOf(false) }
    var showCustomerPicker by remember { mutableStateOf(false) }
    var holdNoteInput by remember { mutableStateOf("") }
    var showHoldPrompt by remember { mutableStateOf(false) }
    var showRecentSalesDialog by remember { mutableStateOf(false) }
    var showExportCsvDialog by remember { mutableStateOf(false) }

    // Item note / discount modal state
    var itemToEdit by remember { mutableStateOf<CartItem?>(null) }

    val categories = remember(products) {
        listOf("Semua") + products.map { it.category }.distinct()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { prod ->
            val matchCategory = selectedCategory == "Semua" || prod.category == selectedCategory
            val matchSearch = searchQuery.isBlank() ||
                    prod.name.contains(searchQuery, ignoreCase = true) ||
                    prod.sku.contains(searchQuery, ignoreCase = true) ||
                    prod.barcode.contains(searchQuery, ignoreCase = true)
            matchCategory && matchSearch
        }
    }

    val cartQtyMap = remember(cartItems) { cartItems.associate { it.product.id to it.quantity } }
    val cartItemMap = remember(cartItems) { cartItems.associateBy { it.product.id } }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTabletOrLandscape = isLandscape || screenWidthDp >= 650

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        if (isTabletOrLandscape) {
            // ENTERPRISE SPLIT-SCREEN DESKTOP POS VIEW (LANDSCAPE / TABLET)
            Row(modifier = Modifier.fillMaxSize()) {
                // LEFT PANEL (64% width): Search, Scanner, Category Chips, Expansive Product Grid
                Column(
                    modifier = Modifier
                        .weight(0.64f)
                        .fillMaxHeight()
                ) {
                    // Live CameraView Scanner Collapsible Drawer
                    AnimatedVisibility(visible = isScannerActive) {
                        CameraView(
                            onBarcodeScanned = { barcode -> viewModel.onScanBarcode(barcode) },
                            lastScannedCode = lastScannedBarcode,
                            onClose = { viewModel.toggleScanner(false) },
                            sampleCodes = products.take(5).map { it.barcode },
                            products = products,
                            onBarcodeResult = { result -> viewModel.onScanBarcodeResult(result) },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }

                    // Search Bar & Scanner Toggle Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari Produk, SKU, Barcode...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantBlue) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("catalog_search_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Scanner Toggle Button
                        Button(
                            onClick = { viewModel.toggleScanner(!isScannerActive) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isScannerActive) EmeraldGreen else DeepRoyalBlue
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("scanner_toggle_button")
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isScannerActive) "Tutup" else "Scan", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        // Draft / Hold Orders badge button
                        if (draftOrders.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showDraftDialog = true },
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                            ) {
                                Box(contentAlignment = Alignment.TopEnd) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Drafts", tint = Color(0xFFD97706))
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(Color(0xFFDC2626), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(draftOrders.size.toString(), color = CrispWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Recent Sales Button (Review Completed Orders)
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { showRecentSalesDialog = true },
                            modifier = Modifier
                                .size(50.dp)
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                                .testTag("cashier_recent_sales_button")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = "Recent Sales", tint = DeepRoyalBlue)
                                if (recentSales.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .background(DeepRoyalBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(recentSales.size.coerceAtMost(99).toString(), color = CrispWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Captured Product Codes Display on Checkout Screen
                    if (capturedProductCodes.isNotEmpty()) {
                        CapturedProductCodesSection(
                            capturedCodes = capturedProductCodes,
                            lastScannedBarcode = lastScannedBarcode,
                            onCodeClick = { code -> viewModel.onScanBarcode(code) },
                            onRemoveCode = { code -> viewModel.removeCapturedCode(code) },
                            onClearAll = { viewModel.clearCapturedCodes() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // Real-time Barcode Scan Notification Banner
                    AnimatedVisibility(visible = scanFeedback != null) {
                        scanFeedback?.let { feedback ->
                            val isFound = feedback.product != null
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 3.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = if (isFound) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isFound) EmeraldGreen else Color(0xFFEF4444)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isFound) Icons.Default.Check else Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = if (isFound) EmeraldGreen else Color(0xFFDC2626),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isFound) "${feedback.product!!.name} (+1) • ${CurrencyFormatter.formatRupiah(feedback.product.sellPrice)}" else "Barcode: ${feedback.barcode} belum terdaftar",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isFound) Color(0xFF065F46) else Color(0xFF991B1B)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.clearScanFeedback() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B), modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Category Chips Row
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = cat == selectedCategory
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepRoyalBlue,
                                    selectedLabelColor = CrispWhite,
                                    containerColor = CrispWhite,
                                    labelColor = DarkSlate
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Expansive Products Grid (3 to 5 columns based on screen width)
                    val gridCols = if (screenWidthDp >= 1050) 5 else if (screenWidthDp >= 800) 4 else 3
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridCols),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            val qty = cartQtyMap[product.id] ?: 0
                            ProductGridCard(
                                product = product,
                                cartQuantity = qty,
                                onAddToCart = { viewModel.addToCart(product) },
                                onMinus = {
                                    cartItemMap[product.id]?.let { item ->
                                        viewModel.updateCartItemQuantity(item, -1)
                                    }
                                }
                            )
                        }
                    }
                }

                // Vertical Divider
                Divider(modifier = Modifier.fillMaxHeight().width(1.dp), color = Color(0xFFE2E8F0))

                // RIGHT PANEL (36% width): Persistent Shopping Cart & Checkout Sidebar
                Surface(
                    color = CrispWhite,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .weight(0.36f)
                        .fillMaxHeight()
                ) {
                    CartSidebarView(
                        cartItems = cartItems,
                        selectedCustomer = selectedCustomer,
                        taxEnabled = taxEnabled,
                        serviceEnabled = serviceEnabled,
                        storeProfile = storeProfile,
                        viewModel = viewModel,
                        onSelectCustomer = { showCustomerPicker = true },
                        onHoldOrder = { showHoldPrompt = true },
                        onClearCart = { viewModel.clearCart() },
                        onEditItem = { itemToEdit = it },
                        onProceedPayment = { showCheckoutDialog = true }
                    )
                }
            }
        } else {
            // PORTRAIT MODE (Single-Handed Mobile View)
            Column(modifier = Modifier.fillMaxSize()) {
                // Live CameraView Scanner Collapsible Drawer
                AnimatedVisibility(visible = isScannerActive) {
                    CameraView(
                        onBarcodeScanned = { barcode -> viewModel.onScanBarcode(barcode) },
                        lastScannedCode = lastScannedBarcode,
                        onClose = { viewModel.toggleScanner(false) },
                        sampleCodes = products.take(5).map { it.barcode },
                        products = products,
                        onBarcodeResult = { result -> viewModel.onScanBarcodeResult(result) },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                // Search Bar & Scanner Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Cari Produk, SKU, Barcode...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantBlue) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("catalog_search_input")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Scanner Toggle Button
                    Button(
                        onClick = { viewModel.toggleScanner(!isScannerActive) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isScannerActive) EmeraldGreen else DeepRoyalBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("scanner_toggle_button")
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner", modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isScannerActive) "Tutup" else "Scan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Draft / Hold Orders badge button
                    if (draftOrders.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { showDraftDialog = true },
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFFFEF3C7), RoundedCornerShape(10.dp))
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(Icons.Default.BookmarkBorder, contentDescription = "Drafts", tint = Color(0xFFD97706))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(Color(0xFFDC2626), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(draftOrders.size.toString(), color = CrispWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Recent Sales Button (Review Completed Orders)
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = { showRecentSalesDialog = true },
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp))
                            .testTag("cashier_portrait_recent_sales_button")
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = "Recent Sales", tint = DeepRoyalBlue)
                            if (recentSales.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(DeepRoyalBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(recentSales.size.coerceAtMost(99).toString(), color = CrispWhite, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Captured Product Codes Display on Checkout Screen
                if (capturedProductCodes.isNotEmpty()) {
                    CapturedProductCodesSection(
                        capturedCodes = capturedProductCodes,
                        lastScannedBarcode = lastScannedBarcode,
                        onCodeClick = { code -> viewModel.onScanBarcode(code) },
                        onRemoveCode = { code -> viewModel.removeCapturedCode(code) },
                        onClearAll = { viewModel.clearCapturedCodes() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                // Real-time Barcode Scan Notification Banner
                AnimatedVisibility(visible = scanFeedback != null) {
                    scanFeedback?.let { feedback ->
                        val isFound = feedback.product != null
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFound) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFound) EmeraldGreen else Color(0xFFEF4444)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (isFound) Icons.Default.Check else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isFound) EmeraldGreen else Color(0xFFDC2626),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isFound) "${feedback.product!!.name} (+1)" else "Barcode: ${feedback.barcode}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isFound) Color(0xFF065F46) else Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = if (isFound) {
                                                "Ditambahkan ke transaksi • ${CurrencyFormatter.formatRupiah(feedback.product.sellPrice)}"
                                            } else {
                                                "Produk belum terdaftar dalam katalog POS"
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { viewModel.clearScanFeedback() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Category Chips Row
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = cat == selectedCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepRoyalBlue,
                                selectedLabelColor = CrispWhite,
                                containerColor = CrispWhite,
                                labelColor = DarkSlate
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Products Grid (2 columns for portrait mobile)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        val qty = cartQtyMap[product.id] ?: 0
                        ProductGridCard(
                            product = product,
                            cartQuantity = qty,
                            onAddToCart = { viewModel.addToCart(product) },
                            onMinus = {
                                cartItemMap[product.id]?.let { item ->
                                    viewModel.updateCartItemQuantity(item, -1)
                                }
                            }
                        )
                    }
                }

                // Bottom Floating Cart Bar
                if (cartItems.isNotEmpty()) {
                    Surface(
                        color = CrispWhite,
                        shadowElevation = 12.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier
                                    .clickable { showCartSheet = true }
                                    .weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(DeepRoyalBlue, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = cartItems.sumOf { it.quantity }.toString(),
                                            color = CrispWhite,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Keranjang Belanja",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = CurrencyFormatter.formatRupiah(viewModel.cartGrandTotal),
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DarkSlate
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { showCartSheet = true },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(46.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Detail", fontSize = 12.sp)
                                }

                                Button(
                                    onClick = { showCheckoutDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(46.dp)
                                        .testTag("pay_checkout_button")
                                ) {
                                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bayar", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CART MODAL BOTTOM SHEET
    if (showCartSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() },
            containerColor = CrispWhite
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Sheet Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Keranjang Belanja", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                        Text(
                            text = if (selectedCustomer != null) "Pelanggan: ${selectedCustomer!!.name} (${selectedCustomer!!.points} poin)" else "Pelanggan: Walk-In",
                            fontSize = 12.sp,
                            color = VibrantBlue
                        )
                    }
                    Row {
                        IconButton(onClick = { showCustomerPicker = true }) {
                            Icon(Icons.Default.Person, contentDescription = "Pilih Pelanggan", tint = DeepRoyalBlue)
                        }
                        IconButton(onClick = { showHoldPrompt = true }) {
                            Icon(Icons.Default.BookmarkBorder, contentDescription = "Simpan Draft", tint = Color(0xFFD97706))
                        }
                        IconButton(onClick = { viewModel.clearCart() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Kosongkan", tint = Color(0xFFDC2626))
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Cart items list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .height(240.dp)
                ) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onPlus = { viewModel.updateCartItemQuantity(item, 1) },
                            onMinus = { viewModel.updateCartItemQuantity(item, -1) },
                            onEdit = { itemToEdit = item }
                        )
                        Divider(color = Color(0xFFF1F5F9))
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 6.dp))

                // Toggles for Tax & Service
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PPN / Pajak (${storeProfile.taxPercent.toInt()}%)", fontSize = 13.sp, color = DarkSlate)
                    Switch(
                        checked = taxEnabled,
                        onCheckedChange = { viewModel.toggleTax(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepRoyalBlue)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Service Charge (${storeProfile.servicePercent.toInt()}%)", fontSize = 13.sp, color = DarkSlate)
                    Switch(
                        checked = serviceEnabled,
                        onCheckedChange = { viewModel.toggleService(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = DeepRoyalBlue)
                    )
                }

                // Summary Totals
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontSize = 13.sp, color = Color(0xFF64748B))
                    Text(CurrencyFormatter.formatRupiah(viewModel.cartSubtotal), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                if (viewModel.cartDiscountAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Diskon & Poin", fontSize = 13.sp, color = EmeraldDark)
                        Text("-${CurrencyFormatter.formatRupiah(viewModel.cartDiscountAmount)}", fontSize = 13.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                    }
                }
                if (viewModel.cartTaxAmount > 0) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Pajak", fontSize = 13.sp, color = Color(0xFF64748B))
                        Text(CurrencyFormatter.formatRupiah(viewModel.cartTaxAmount), fontSize = 13.sp)
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Tagihan", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                    Text(CurrencyFormatter.formatRupiah(viewModel.cartGrandTotal), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = DeepRoyalBlue)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        showCartSheet = false
                        showCheckoutDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Lanjut ke Pembayaran (${CurrencyFormatter.formatRupiah(viewModel.cartGrandTotal)})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // CHECKOUT MULTI-GATEWAY MODAL BOTTOM SHEET
    if (showCheckoutDialog) {
        PaymentCheckoutBottomSheet(
            grandTotal = viewModel.cartGrandTotal,
            itemsCount = cartItems.sumOf { it.quantity },
            selectedCustomer = selectedCustomer,
            storeProfile = storeProfile,
            onUpdateQrisImage = { uri -> viewModel.updateStoreQrisImage(uri) },
            onProcessPayment = { method, cashPaid, splitMethod2, splitAmt1, splitAmt2 ->
                showCheckoutDialog = false
                viewModel.processCheckout(method, cashPaid, splitMethod2, splitAmt1, splitAmt2)
            },
            onDismiss = { showCheckoutDialog = false }
        )
    }

    // DRAFT / HOLD ORDERS DIALOG
    if (showDraftDialog) {
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showDraftDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Tutup", color = DarkSlate)
                }
            },
            title = { Text("Daftar Pesanan Disimpan (Hold Draft)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                if (draftOrders.isEmpty()) {
                    Text("Tidak ada pesanan yang di-hold saat ini.")
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(draftOrders) { draft ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(draft.draftTag, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(CurrencyFormatter.formatRupiah(draft.grandTotal), color = EmeraldDark, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text(CurrencyFormatter.formatTime(draft.timestamp), color = Color(0xFF94A3B8), fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.restoreDraft(draft)
                                            showDraftDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text("Buka", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // HOLD CART PROMPT
    if (showHoldPrompt) {
        AlertDialog(
            onDismissRequest = { showHoldPrompt = false },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.holdOrder(holdNoteInput)
                        holdNoteInput = ""
                        showHoldPrompt = false
                        showCartSheet = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706))
                ) {
                    Text("Hold Pesanan")
                }
            },
            dismissButton = {
                Button(onClick = { showHoldPrompt = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text("Simpan Draft Pesanan (Hold)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    Text("Beri label untuk pesanan ini (misal: Meja 4, Budi Bungkus):", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = holdNoteInput,
                        onValueChange = { holdNoteInput = it },
                        placeholder = { Text("Contoh: Meja 4") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // CUSTOMER PICKER MODAL
    if (showCustomerPicker) {
        AlertDialog(
            onDismissRequest = { showCustomerPicker = false },
            confirmButton = {},
            dismissButton = {
                Button(onClick = { showCustomerPicker = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Tutup", color = DarkSlate)
                }
            },
            title = { Text("Pilih Pelanggan CRM", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column {
                    // Option: Walk-In (None)
                    Card(
                        onClick = {
                            viewModel.selectCustomer(null)
                            showCustomerPicker = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Text("Walk-In (Tanpa Nama)", modifier = Modifier.padding(10.dp), fontWeight = FontWeight.Medium)
                    }

                    LazyColumn(modifier = Modifier.height(220.dp)) {
                        items(customers) { cust ->
                            Card(
                                onClick = {
                                    viewModel.selectCustomer(cust)
                                    showCustomerPicker = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCustomer?.id == cust.id) Color(0xFFEFF6FF) else CrispWhite
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(cust.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(cust.phone, fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("${cust.points} Poin", color = EmeraldDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )
    }

    // EDIT ITEM NOTE / DISCOUNT DIALOG
    itemToEdit?.let { item ->
        var noteInput by remember { mutableStateOf(item.itemNote) }
        var discountPctInput by remember { mutableStateOf(if (item.discountPercent > 0) item.discountPercent.toInt().toString() else "") }
        var discountFixedInput by remember { mutableStateOf(if (item.discountFixed > 0) item.discountFixed.toInt().toString() else "") }

        AlertDialog(
            onDismissRequest = { itemToEdit = null },
            confirmButton = {
                Button(
                    onClick = {
                        val pct = (discountPctInput.toDoubleOrNull() ?: 0.0).coerceIn(0.0, 100.0)
                        val fixed = (discountFixedInput.toDoubleOrNull() ?: 0.0).coerceAtLeast(0.0)
                        viewModel.updateItemDiscount(item, pct, fixed)
                        viewModel.updateItemNote(item, noteInput.trim())
                        itemToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                Button(onClick = { itemToEdit = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text("Detail Item: ${item.displayName}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column {
                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        label = { Text("Catatan Item (Order Note)") },
                        placeholder = { Text("Contoh: Less Ice / Extra Pedas") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountPctInput,
                        onValueChange = { discountPctInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Diskon Persen (%)") },
                        suffix = { Text("%") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = discountFixedInput,
                        onValueChange = { discountFixedInput = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Diskon Nominal Tetap (Rp)") },
                        prefix = { Text("Rp ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )
    }

    // RECENT SALES REVIEW DIALOG
    if (showRecentSalesDialog) {
        RecentSalesDialog(
            recentSales = recentSales,
            onDismiss = { showRecentSalesDialog = false },
            onExportClick = {
                showRecentSalesDialog = false
                showExportCsvDialog = true
            }
        )
    }

    if (showExportCsvDialog) {
        ExportSalesReportDialog(
            orders = completedOrders,
            products = products,
            storeProfile = storeProfile,
            onDismiss = { showExportCsvDialog = false }
        )
    }
}

@Composable
fun CartSidebarView(
    cartItems: List<CartItem>,
    selectedCustomer: Customer?,
    taxEnabled: Boolean,
    serviceEnabled: Boolean,
    storeProfile: com.example.data.model.StoreProfile,
    viewModel: PosViewModel,
    onSelectCustomer: () -> Unit,
    onHoldOrder: () -> Unit,
    onClearCart: () -> Unit,
    onEditItem: (CartItem) -> Unit,
    onProceedPayment: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Sidebar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFFEFF6FF), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Keranjang Kasir", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DarkSlate)
                    Text("${cartItems.sumOf { it.quantity }} Produk Ditambahkan", fontSize = 11.sp, color = Color(0xFF64748B))
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onHoldOrder,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.BookmarkBorder,
                        contentDescription = "Tahan Order",
                        tint = if (cartItems.isNotEmpty()) Color(0xFFD97706) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = onClearCart,
                    enabled = cartItems.isNotEmpty(),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Kosongkan",
                        tint = if (cartItems.isNotEmpty()) Color(0xFFDC2626) else Color(0xFFCBD5E1),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Customer CRM Selector Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectCustomer() },
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = DeepRoyalBlue, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (selectedCustomer != null) selectedCustomer.name else "Pelanggan: Walk-In",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlate
                    )
                }
                Text(
                    text = if (selectedCustomer != null) "${selectedCustomer.points} Poin" else "+ CRM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = VibrantBlue
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

        // Scrollable Cart Items
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color(0xFFCBD5E1), modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Keranjang Belanja Kosong", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF64748B))
                    Text("Pilih produk di sebelah kiri atau scan barcode", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(cartItems) { item ->
                    CartItemRow(
                        item = item,
                        onPlus = { viewModel.updateCartItemQuantity(item, 1) },
                        onMinus = { viewModel.updateCartItemQuantity(item, -1) },
                        onEdit = { onEditItem(item) }
                    )
                    Divider(color = Color(0xFFF1F5F9))
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFE2E8F0))

        // Modifiers & Price Summary
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PPN (${storeProfile.taxPercent.toInt()}%)", fontSize = 11.sp, color = Color(0xFF64748B))
                Switch(
                    checked = taxEnabled,
                    onCheckedChange = { viewModel.toggleTax(it) },
                    modifier = Modifier.height(24.dp),
                    colors = SwitchDefaults.colors(checkedThumbColor = DeepRoyalBlue)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Service (${storeProfile.servicePercent.toInt()}%)", fontSize = 11.sp, color = Color(0xFF64748B))
                Switch(
                    checked = serviceEnabled,
                    onCheckedChange = { viewModel.toggleService(it) },
                    modifier = Modifier.height(24.dp),
                    colors = SwitchDefaults.colors(checkedThumbColor = DeepRoyalBlue)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", fontSize = 12.sp, color = Color(0xFF64748B))
                Text(CurrencyFormatter.formatRupiah(viewModel.cartSubtotal), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            if (viewModel.cartDiscountAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Diskon & Poin", fontSize = 12.sp, color = EmeraldDark)
                    Text("-${CurrencyFormatter.formatRupiah(viewModel.cartDiscountAmount)}", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                }
            }

            if (viewModel.cartTaxAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pajak", fontSize = 12.sp, color = Color(0xFF64748B))
                    Text(CurrencyFormatter.formatRupiah(viewModel.cartTaxAmount), fontSize = 12.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total Tagihan", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                Text(
                    text = CurrencyFormatter.formatRupiah(viewModel.cartGrandTotal),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepRoyalBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Primary Payment Button
            Button(
                onClick = onProceedPayment,
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pay_checkout_button_sidebar"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (cartItems.isEmpty()) "Keranjang Kosong" else "Bayar ${CurrencyFormatter.formatRupiah(viewModel.cartGrandTotal)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    cartQuantity: Int,
    onAddToCart: () -> Unit,
    onMinus: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onAddToCart() }
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CrispWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Thumbnail / Category Color Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(product.iconColor).copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUri.isNotBlank()) {
                    AsyncImage(
                        model = File(product.imageUri),
                        contentDescription = product.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = product.name.take(2).uppercase(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CrispWhite
                    )
                }

                // Stock Badge
                val (stockBg, stockColor, stockText) = when {
                    product.isOutOfStock -> Triple(Color(0xFFEF4444), CrispWhite, "Habis")
                    product.isLowStock -> Triple(Color(0xFFF59E0B), CrispWhite, "Sisa ${product.stock}")
                    else -> Triple(Color(0xFF0F172A).copy(alpha = 0.7f), CrispWhite, "Stok ${product.stock}")
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(stockBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text(stockText, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = stockColor)
                }

                // Wholesale badge if active
                if (product.hasWholesale) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(4.dp)
                            .background(DeepRoyalBlue, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("Grosir ≥${product.wholesaleMinQty}", fontSize = 8.sp, color = CrispWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & SKU
            Text(
                text = product.name,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DarkSlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "SKU: ${product.sku}",
                fontSize = 10.sp,
                color = Color(0xFF94A3B8)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Selling Price & Quantity Control
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = CurrencyFormatter.formatRupiah(product.sellPrice),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = DeepRoyalBlue
                )

                if (cartQuantity > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                            .border(0.5.dp, VibrantBlue, RoundedCornerShape(6.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onMinus() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp), tint = DeepRoyalBlue)
                        }
                        Text(
                            text = cartQuantity.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepRoyalBlue,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clickable { onAddToCart() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp), tint = DeepRoyalBlue)
                        }
                    }
                } else {
                    IconButton(
                        onClick = onAddToCart,
                        modifier = Modifier
                            .size(28.dp)
                            .background(DeepRoyalBlue, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Beli", tint = CrispWhite, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onPlus: () -> Unit,
    onMinus: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${item.quantity}x @${CurrencyFormatter.formatRupiah(item.unitPrice)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
                if (item.discountAmount > 0) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(-${CurrencyFormatter.formatRupiah(item.discountAmount)})",
                        fontSize = 10.sp,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (item.itemNote.isNotBlank()) {
                Text("* ${item.itemNote}", fontSize = 10.sp, color = Color(0xFFD97706))
            }
        }

        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.EditNote, contentDescription = "Edit", tint = DeepRoyalBlue, modifier = Modifier.size(18.dp))
        }

        Spacer(modifier = Modifier.width(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
        ) {
            IconButton(onClick = onMinus, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Remove, contentDescription = "Kurang", modifier = Modifier.size(14.dp))
            }
            Text(item.quantity.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = onPlus, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Add, contentDescription = "Tambah", modifier = Modifier.size(14.dp))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = CurrencyFormatter.formatRupiah(item.totalPrice),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = DarkSlate,
            modifier = Modifier.width(75.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun CheckoutGatewayDialog(
    grandTotal: Double,
    selectedCustomer: Customer?,
    onProcessPayment: (method: PaymentMethod, cashPaid: Double, splitMethod2: String, splitAmt1: Double, splitAmt2: Double) -> Unit,
    onDismiss: () -> Unit
) {
    PaymentCheckoutBottomSheet(
        grandTotal = grandTotal,
        selectedCustomer = selectedCustomer,
        onProcessPayment = onProcessPayment,
        onDismiss = onDismiss
    )
}

/**
 * Visual strip displaying captured product codes directly on the main checkout screen.
 */
@Composable
fun CapturedProductCodesSection(
    capturedCodes: List<PosViewModel.CapturedProductCode>,
    lastScannedBarcode: String,
    onCodeClick: (String) -> Unit,
    onRemoveCode: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("captured_product_codes_section"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(VibrantBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = VibrantBlue,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Kode Produk Terpindai",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkSlate
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = VibrantBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "${capturedCodes.size} Kode",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                TextButton(
                    onClick = onClearAll,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("clear_captured_codes_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Riwayat",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Bersihkan",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("captured_product_codes_list")
            ) {
                items(capturedCodes, key = { it.code + it.timestamp }) { item ->
                    val isLatest = item.code == lastScannedBarcode
                    val hasProduct = item.product != null

                    Surface(
                        onClick = { onCodeClick(item.code) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLatest) Color(0xFFEFF6FF) else CrispWhite,
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isLatest) 1.5.dp else 1.dp,
                            color = if (isLatest) VibrantBlue else Color(0xFFE2E8F0)
                        ),
                        shadowElevation = if (isLatest) 2.dp else 0.5.dp,
                        modifier = Modifier.testTag("captured_code_chip_${item.code}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.code,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                        color = if (isLatest) VibrantBlue else DarkSlate
                                    )
                                    if (isLatest) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = EmeraldGreen
                                        ) {
                                            Text(
                                                text = "BARU",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = CrispWhite,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }

                                if (hasProduct) {
                                    val prod = item.product!!
                                    Text(
                                        text = "${prod.name} • ${CurrencyFormatter.formatRupiah(prod.sellPrice)}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF059669),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "Produk belum terdaftar",
                                        fontSize = 10.sp,
                                        color = Color(0xFFDC2626)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            IconButton(
                                onClick = { onRemoveCode(item.code) },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Hapus Kode",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

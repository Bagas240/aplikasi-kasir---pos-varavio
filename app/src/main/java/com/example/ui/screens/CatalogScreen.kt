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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.graphics.Bitmap
import android.net.Uri
import coil.compose.AsyncImage
import com.example.util.MediaHelper
import java.io.File
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Product
import com.example.data.model.UserRole
import com.example.ui.PosTab
import com.example.ui.PosViewModel
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.CurrencyFormatter

@Composable
fun CatalogScreen(
    viewModel: PosViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products else {
            products.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.sku.contains(searchQuery, ignoreCase = true) ||
                        it.barcode.contains(searchQuery, ignoreCase = true) ||
                        it.category.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().background(SoftGrayBg)) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Katalog & Master Produk", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DarkSlate)
                    Text("Total: ${products.size} produk terdaftar", fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari berdasarkan nama, SKU, atau kategori...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantBlue) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Products List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                items(filtered, key = { it.id }) { product ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = CrispWhite),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(product.iconColor)),
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
                                            Text(product.name.take(2).uppercase(), fontWeight = FontWeight.Bold, color = CrispWhite, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DarkSlate)
                                        Text("SKU: ${product.sku} • Barcode: ${product.barcode.ifBlank { "-" }}", fontSize = 11.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Row {
                                    IconButton(onClick = {
                                        viewModel.selectProductForBarcode(product)
                                        viewModel.setTab(PosTab.BARCODE_ENGINE)
                                    }) {
                                        Icon(Icons.Default.QrCode, contentDescription = "Cetak Barcode", tint = DeepRoyalBlue)
                                    }
                                    IconButton(onClick = { productToEdit = product }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = VibrantBlue)
                                    }
                                    if (currentUser.role == UserRole.OWNER) {
                                        IconButton(onClick = { viewModel.deleteProduct(product) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }

                            Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Harga Jual", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Text(CurrencyFormatter.formatRupiah(product.sellPrice), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DeepRoyalBlue)
                                }

                                if (currentUser.role == UserRole.OWNER) {
                                    Column {
                                        Text("Harga Modal (HPP)", fontSize = 10.sp, color = Color(0xFF64748B))
                                        Text(CurrencyFormatter.formatRupiah(product.buyPrice), fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = DarkSlate)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text("Margin +${product.marginPercent.toInt()}%", color = EmeraldDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Sisa Stok", fontSize = 10.sp, color = Color(0xFF64748B))
                                    Text(
                                        text = "${product.stock} ${product.unit}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (product.isLowStock) Color(0xFFDC2626) else DarkSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ADD / EDIT PRODUCT DIALOG
    if (showAddDialog || productToEdit != null) {
        val context = LocalContext.current
        val editing = productToEdit
        var name by remember { mutableStateOf(editing?.name ?: "") }
        var sku by remember { mutableStateOf(editing?.sku ?: ("SKU-" + (1000..9999).random())) }
        var barcode by remember { mutableStateOf(editing?.barcode ?: ("899" + (100000000..999999999).random())) }
        var category by remember { mutableStateOf(editing?.category ?: "Beverages") }
        var unit by remember { mutableStateOf(editing?.unit ?: "Pcs") }
        var imageUri by remember { mutableStateOf(editing?.imageUri ?: "") }
        var buyPriceInput by remember { mutableStateOf(if (editing != null) editing.buyPrice.toInt().toString() else "10000") }
        var sellPriceInput by remember { mutableStateOf(if (editing != null) editing.sellPrice.toInt().toString() else "20000") }
        var stockInput by remember { mutableStateOf(if (editing != null) editing.stock.toString() else "25") }
        var minStockInput by remember { mutableStateOf(if (editing != null) editing.minStockAlert.toString() else "5") }
        var hasWholesale by remember { mutableStateOf(editing?.hasWholesale ?: false) }
        var wholesaleQtyInput by remember { mutableStateOf(editing?.wholesaleMinQty?.toString() ?: "5") }
        var wholesalePriceInput by remember { mutableStateOf(editing?.wholesalePrice?.toInt()?.toString() ?: "18000") }

        val cameraLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? ->
            if (bitmap != null) {
                val path = MediaHelper.saveBitmapToInternalStorage(context, bitmap, "prod")
                if (path != null) imageUri = path
            }
        }

        val photoPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            if (uri != null) {
                val path = MediaHelper.copyUriToInternalStorage(context, uri, "prod")
                if (path != null) imageUri = path
            }
        }

        val buyP = buyPriceInput.toDoubleOrNull() ?: 0.0
        val sellP = sellPriceInput.toDoubleOrNull() ?: 0.0
        val calculatedMargin = if (buyP > 0) ((sellP - buyP) / buyP) * 100.0 else 0.0

        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                productToEdit = null
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newProduct = Product(
                            id = editing?.id ?: 0L,
                            name = name.trim(),
                            sku = sku.trim(),
                            barcode = barcode.trim(),
                            category = category,
                            unit = unit,
                            imageUri = imageUri,
                            buyPrice = buyP,
                            sellPrice = sellP,
                            stock = stockInput.toIntOrNull() ?: 0,
                            minStockAlert = minStockInput.toIntOrNull() ?: 5,
                            hasWholesale = hasWholesale,
                            wholesaleMinQty = wholesaleQtyInput.toIntOrNull() ?: 0,
                            wholesalePrice = wholesalePriceInput.toDoubleOrNull() ?: 0.0
                        )
                        viewModel.saveProduct(newProduct)
                        showAddDialog = false
                        productToEdit = null
                    },
                    enabled = name.isNotBlank() && sellP > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue)
                ) {
                    Text(if (editing == null) "Tambah Produk" else "Simpan Perubahan")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showAddDialog = false
                    productToEdit = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            },
            title = { Text(if (editing == null) "Tambah Produk Baru" else "Edit Produk", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Real Photo Attachment Area
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Foto Fisik Produk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DarkSlate)
                            Spacer(modifier = Modifier.height(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (imageUri.isNotBlank()) {
                                    AsyncImage(
                                        model = File(imageUri),
                                        contentDescription = "Foto Produk",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(32.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { cameraLauncher.launch(null) },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = DeepRoyalBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Kamera", fontSize = 11.sp, color = DeepRoyalBlue)
                                }

                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp), tint = VibrantBlue)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Galeri", fontSize = 11.sp, color = VibrantBlue)
                                }

                                if (imageUri.isNotBlank()) {
                                    IconButton(
                                        onClick = { imageUri = "" },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Produk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = sku,
                            onValueChange = { sku = it },
                            label = { Text("SKU") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Kategori") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    // Unit Selection Chips
                    Text("Satuan / Unit Type:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkSlate)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Pcs", "Kg", "Box", "Cup", "Pack", "Botol").forEach { u ->
                            val isSelected = unit.equals(u, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DeepRoyalBlue else Color(0xFFE2E8F0))
                                    .clickable { unit = u }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = u,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CrispWhite else DarkSlate
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = barcode,
                        onValueChange = { barcode = it },
                        label = { Text("Barcode / EAN-13") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = buyPriceInput,
                            onValueChange = { buyPriceInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("HPP Modal (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = sellPriceInput,
                            onValueChange = { sellPriceInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Harga Jual (Rp)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Margin indicator
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Auto Margin Profit: +${calculatedMargin.toInt()}%", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(
                            value = stockInput,
                            onValueChange = { stockInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Stok Fisik") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minStockInput,
                            onValueChange = { minStockInput = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Batas Peringatan") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Aktifkan Harga Grosir", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Switch(
                            checked = hasWholesale,
                            onCheckedChange = { hasWholesale = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = DeepRoyalBlue)
                        )
                    }

                    if (hasWholesale) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = wholesaleQtyInput,
                                onValueChange = { wholesaleQtyInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Min Qty Grosir") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = wholesalePriceInput,
                                onValueChange = { wholesalePriceInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Harga Grosir (Rp)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        )
    }
}

package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.util.CsvReportGenerator
import com.example.util.CurrencyFormatter
import com.example.util.ReportPeriod
import com.example.util.ReportType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportSalesReportDialog(
    orders: List<OrderEntity>,
    products: List<Product>,
    storeProfile: StoreProfile,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }
    var selectedReportType by remember { mutableStateOf(ReportType.COMPREHENSIVE) }
    var customDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }
    var showPreviewText by remember { mutableStateOf(false) }

    val indonesianLocale = remember { Locale("id", "ID") }
    val displayDateFormat = remember { SimpleDateFormat("dd MMMM yyyy", indonesianLocale) }

    // Filter orders according to selected period
    val filteredOrders = remember(orders, selectedPeriod, customDateMillis) {
        CsvReportGenerator.filterOrders(orders, selectedPeriod, customDateMillis)
    }

    val periodLabel = remember(selectedPeriod, customDateMillis) {
        if (selectedPeriod == ReportPeriod.CUSTOM_DATE) {
            "Tanggal: ${displayDateFormat.format(Date(customDateMillis))}"
        } else {
            selectedPeriod.label
        }
    }

    // Compute live analytical summary
    val reportSummary = remember(filteredOrders, products, periodLabel) {
        CsvReportGenerator.computeSummary(filteredOrders, products, periodLabel)
    }

    // Generate CSV Content
    val generatedCsvContent by remember(filteredOrders, products, storeProfile, selectedReportType, periodLabel) {
        derivedStateOf {
            when (selectedReportType) {
                ReportType.COMPREHENSIVE -> CsvReportGenerator.generateComprehensiveCsv(
                    storeProfile = storeProfile,
                    orders = filteredOrders,
                    products = products,
                    periodLabel = periodLabel
                )
                ReportType.TRANSACTIONS_ONLY -> CsvReportGenerator.generateTransactionsOnlyCsv(
                    orders = filteredOrders,
                    products = products
                )
                ReportType.PRODUCTS_PERFORMANCE -> CsvReportGenerator.generateProductPerformanceCsv(
                    orders = filteredOrders,
                    products = products,
                    periodLabel = periodLabel
                )
            }
        }
    }

    // Default file name for download/sharing
    val defaultFileName = remember(selectedReportType, selectedPeriod, customDateMillis) {
        val dateSlug = SimpleDateFormat("yyyyMMdd", Locale.US).format(
            if (selectedPeriod == ReportPeriod.CUSTOM_DATE) Date(customDateMillis) else Date()
        )
        val typeSlug = when (selectedReportType) {
            ReportType.COMPREHENSIVE -> "Laporan_Harian_Lengkap"
            ReportType.TRANSACTIONS_ONLY -> "Transaksi_Penjualan"
            ReportType.PRODUCTS_PERFORMANCE -> "Performa_Produk"
        }
        "${typeSlug}_${dateSlug}.csv"
    }

    // Activity launcher for Android Storage Access Framework (Save to Device Storage)
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(generatedCsvContent.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(
                    context,
                    "Berhasil menyimpan laporan CSV ke perangkat!",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    context,
                    "Gagal menyimpan file: ${e.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // DatePickerDialog handler
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = customDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            customDateMillis = millis
                            selectedPeriod = ReportPeriod.CUSTOM_DATE
                        }
                        showDatePickerDialog = false
                    }
                ) {
                    Text("Pilih Tanggal", fontWeight = FontWeight.Bold, color = DeepRoyalBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerDialog = false }) {
                    Text("Batal", color = Color(0xFF64748B))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .testTag("export_sales_report_dialog"),
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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
                                .size(40.dp)
                                .background(Color(0xFFEFF6FF), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = DeepRoyalBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Ekspor Laporan Penjualan (CSV)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = DarkSlate
                            )
                            Text(
                                text = "Analitik bisnis & pembukuan akuntansi",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Period Selection
                Text(
                    text = "1. Pilih Periode Laporan:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkSlate
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReportPeriod.values().forEach { period ->
                        val isSelected = selectedPeriod == period
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (period == ReportPeriod.CUSTOM_DATE) {
                                    showDatePickerDialog = true
                                } else {
                                    selectedPeriod = period
                                }
                            },
                            label = {
                                if (period == ReportPeriod.CUSTOM_DATE && selectedPeriod == ReportPeriod.CUSTOM_DATE) {
                                    Text(
                                        displayDateFormat.format(Date(customDateMillis)),
                                        fontSize = 11.sp
                                    )
                                } else {
                                    Text(period.label, fontSize = 11.sp)
                                }
                            },
                            leadingIcon = if (period == ReportPeriod.CUSTOM_DATE) {
                                {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else if (isSelected) {
                                {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = DeepRoyalBlue
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFDBEAFE),
                                selectedLabelColor = DeepRoyalBlue
                            ),
                            modifier = Modifier.testTag("period_chip_${period.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Format / Report Type
                Text(
                    text = "2. Pilih Format Laporan CSV:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DarkSlate
                )
                Spacer(modifier = Modifier.height(6.dp))

                ReportType.values().forEach { type ->
                    val isSelected = selectedReportType == type
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) DeepRoyalBlue else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedReportType = type },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFF0F7FF) else CrispWhite
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedReportType = type },
                                colors = RadioButtonDefaults.colors(selectedColor = DeepRoyalBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = type.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) DeepRoyalBlue else DarkSlate
                                )
                                Text(
                                    text = type.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Live Analytical Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = SoftGrayBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ringkasan Laporan ($periodLabel)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate
                            )
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFDBEAFE), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${reportSummary.totalOrders} Transaksi",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepRoyalBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = Color(0xFFE2E8F0))
                        Spacer(modifier = Modifier.height(8.dp))

                        // 2x2 stats grid
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Penjualan Bersih (Net)", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(
                                    CurrencyFormatter.formatRupiah(reportSummary.netSales),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = DeepRoyalBlue
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Estimasi Laba Kotor", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(
                                    CurrencyFormatter.formatRupiah(reportSummary.grossProfit),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Margin Profit (%)", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(
                                    String.format(Locale.US, "%.1f%%", reportSummary.profitMarginPercent),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkSlate
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rata-rata Belanja (AOV)", fontSize = 10.sp, color = Color(0xFF64748B))
                                Text(
                                    CurrencyFormatter.formatRupiah(reportSummary.averageOrderValue),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkSlate
                                )
                            }
                        }

                        if (reportSummary.totalOrders == 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Tidak ada transaksi tercatat pada periode ini.",
                                fontSize = 11.sp,
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Toggle Preview Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPreviewText = !showPreviewText }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (showPreviewText) "Sembunyikan Cuplikan CSV" else "Lihat Cuplikan Data CSV",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DeepRoyalBlue
                        )
                    }
                    Icon(
                        imageVector = if (showPreviewText) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DeepRoyalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showPreviewText) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(Color(0xFF1E293B), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = generatedCsvContent.take(2000) + if (generatedCsvContent.length > 2000) "\n... [dan data lainnya]" else "",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 1. Share via System Sheet (WhatsApp, Email, etc.)
                    Button(
                        onClick = {
                            val cachedFile = CsvReportGenerator.saveCsvToCache(
                                context = context,
                                filename = defaultFileName,
                                csvContent = generatedCsvContent
                            )
                            CsvReportGenerator.shareCsvFile(
                                context = context,
                                file = cachedFile,
                                title = "Laporan Penjualan $periodLabel"
                            )
                        },
                        enabled = filteredOrders.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("export_share_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepRoyalBlue),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Kirim / Bagikan File CSV", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // 2. Save to Device Storage (Downloads / Documents via SAF)
                    OutlinedButton(
                        onClick = {
                            createDocumentLauncher.launch(defaultFileName)
                        },
                        enabled = filteredOrders.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("export_save_file_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp), tint = DeepRoyalBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan ke Memori Perangkat", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DeepRoyalBlue)
                    }

                    // 3. Copy to Clipboard
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Laporan Penjualan CSV", generatedCsvContent)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Data CSV disalin ke papan klip!", Toast.LENGTH_SHORT).show()
                        },
                        enabled = filteredOrders.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF64748B))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Salin Seluruh Teks CSV", fontSize = 12.sp, color = Color(0xFF64748B))
                    }
                }
            }
        },
        confirmButton = {}
    )
}

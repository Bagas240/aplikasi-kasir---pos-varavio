package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.PaymentMethod
import com.example.data.model.StoreProfile
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import java.io.File
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DangerLight
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningLight
import com.example.util.BarcodeGenerator
import com.example.util.CurrencyFormatter
import kotlin.math.ceil

/**
 * Digital Payment Item Descriptor for the selection list
 */
data class DigitalPaymentOption(
    val method: PaymentMethod,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
    val iconBgColor: Color = VibrantBlue
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PaymentCheckoutBottomSheet(
    grandTotal: Double,
    itemsCount: Int = 1,
    selectedCustomer: Customer? = null,
    storeProfile: StoreProfile = StoreProfile(),
    onProcessPayment: (method: PaymentMethod, cashPaid: Double, splitMethod2: String, splitAmt1: Double, splitAmt2: Double) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current

    // Payment Category Tab: 0 = Cash / Tunai, 1 = QRIS Toko, 2 = Kartu / Lainnya
    var selectedCategoryTab by remember { mutableIntStateOf(0) }

    // Cash Payment State
    var cashPaidInput by remember { mutableStateOf(grandTotal.toInt().toString()) }
    val cashPaid = cashPaidInput.toDoubleOrNull() ?: 0.0
    val changeAmount = (cashPaid - grandTotal).coerceAtLeast(0.0)
    val isCashSufficient = cashPaid >= grandTotal

    // Digital Payment State
    var selectedDigitalMethod by remember { mutableStateOf(PaymentMethod.QRIS) }

    // Sub-states for specific digital flows
    var selectedBank by remember { mutableStateOf("BCA") }
    var cardApprovalCode by remember { mutableStateOf("") }
    var selectedEWallet by remember { mutableStateOf("GoPay") }
    var eWalletPhone by remember { mutableStateOf(selectedCustomer?.phone ?: "") }
    var splitCashAmtInput by remember { mutableStateOf((grandTotal / 2).toInt().toString()) }
    var splitDigitalMethod by remember { mutableStateOf(PaymentMethod.QRIS.name) }
    var debtDueDateDays by remember { mutableIntStateOf(7) }

    // QRIS simulation state
    var qrisSimulatedSuccess by remember { mutableStateOf(false) }

    // Auto-calculated Cash Denominations and Change Suggestions
    val smartDenominations = remember(grandTotal) {
        generateSmartCashPresets(grandTotal)
    }

    // Change Banknote Breakdown (Pecahan Kembalian)
    val changeBreakdown = remember(changeAmount) {
        calculateChangeBreakdown(changeAmount)
    }

    // Digital Payment Options List
    val digitalPaymentOptions = listOf(
        DigitalPaymentOption(
            method = PaymentMethod.QRIS,
            title = "QRIS Dinamis",
            subtitle = "GoPay, OVO, DANA, ShopeePay, BCA, Livin",
            icon = Icons.Default.QrCodeScanner,
            badge = "Instan",
            iconBgColor = DeepRoyalBlue
        ),
        DigitalPaymentOption(
            method = PaymentMethod.DEBIT_CARD,
            title = "Kartu Debit (EDC)",
            subtitle = "BCA, Mandiri, BRI, BNI, CIMB",
            icon = Icons.Default.CreditCard,
            iconBgColor = VibrantBlue
        ),
        DigitalPaymentOption(
            method = PaymentMethod.CREDIT_CARD,
            title = "Kartu Kredit",
            subtitle = "Visa, Mastercard, JCB, Amex",
            icon = Icons.Default.CreditCard,
            iconBgColor = Color(0xFF7C3AED)
        ),
        DigitalPaymentOption(
            method = PaymentMethod.E_WALLET,
            title = "E-Wallet / Push Notif",
            subtitle = "GoPay, DANA, OVO, ShopeePay direct",
            icon = Icons.Default.AccountBalanceWallet,
            iconBgColor = Color(0xFF0284C7)
        ),
        DigitalPaymentOption(
            method = PaymentMethod.BANK_TRANSFER,
            title = "Transfer Bank / VA",
            subtitle = "Virtual Account BCA, Mandiri, BRI",
            icon = Icons.Default.AccountBalance,
            iconBgColor = Color(0xFF0D9488)
        ),
        DigitalPaymentOption(
            method = PaymentMethod.SPLIT,
            title = "Split Payment",
            subtitle = "Kombinasi Tunai + Digital/Kartu",
            icon = Icons.Default.CallSplit,
            badge = "Fleksibel",
            iconBgColor = Color(0xFFEA580C)
        ),
        DigitalPaymentOption(
            method = PaymentMethod.DEBT,
            title = "Kas Bon / Piutang",
            subtitle = if (selectedCustomer != null) "Catat ke ${selectedCustomer.name}" else "Khusus Member Terdaftar",
            icon = Icons.Default.Assignment,
            badge = if (selectedCustomer != null) "Member" else "Perlu Member",
            iconBgColor = if (selectedCustomer != null) Color(0xFFB45309) else SlateLight
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CrispWhite,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.testTag("payment_checkout_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = DeepRoyalBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Checkout & Pembayaran",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        )
                        Text(
                            text = if (selectedCustomer != null) "Pelanggan: ${selectedCustomer.name} (${selectedCustomer.points} pts)" else "Pelanggan Walk-In",
                            fontSize = 12.sp,
                            color = SlateMuted
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup",
                        tint = SlateMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Total Tagihan Hero Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DeepRoyalBlue),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DeepRoyalBlue)
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL TAGIHAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrispWhite.copy(alpha = 0.8f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = CurrencyFormatter.formatRupiah(grandTotal),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CrispWhite
                            )
                        }

                        // Right badge: items summary
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = CrispWhite.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$itemsCount Item",
                                    color = CrispWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Payment Mode Segmented Tab (Cash vs QRIS vs Kartu/Lainnya)
            TabRow(
                selectedTabIndex = selectedCategoryTab,
                containerColor = SoftGrayBg,
                contentColor = DeepRoyalBlue,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategoryTab]),
                        height = 3.dp,
                        color = DeepRoyalBlue
                    )
                },
                divider = {}
            ) {
                Tab(
                    selected = selectedCategoryTab == 0,
                    onClick = { selectedCategoryTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Payments,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "💵 Tunai",
                                fontWeight = if (selectedCategoryTab == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    },
                    selectedContentColor = DeepRoyalBlue,
                    unselectedContentColor = SlateMuted
                )

                Tab(
                    selected = selectedCategoryTab == 1,
                    onClick = {
                        selectedCategoryTab = 1
                        selectedDigitalMethod = PaymentMethod.QRIS
                    },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "📱 QRIS",
                                fontWeight = if (selectedCategoryTab == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    },
                    selectedContentColor = DeepRoyalBlue,
                    unselectedContentColor = SlateMuted
                )

                Tab(
                    selected = selectedCategoryTab == 2,
                    onClick = { selectedCategoryTab = 2 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CreditCard,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "💳 Lainnya",
                                fontWeight = if (selectedCategoryTab == 2) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    },
                    selectedContentColor = DeepRoyalBlue,
                    unselectedContentColor = SlateMuted
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Content Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                if (selectedCategoryTab == 0) {
                    // ==========================================
                    // CASH PAYMENT SECTION
                    // ==========================================
                    Text(
                        text = "Nominal Uang Tunai Diterima:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlate
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Large Cash Amount Input Field
                    OutlinedTextField(
                        value = cashPaidInput,
                        onValueChange = { input ->
                            cashPaidInput = input.filter { it.isDigit() }
                        },
                        leadingIcon = {
                            Text(
                                text = "Rp",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DeepRoyalBlue,
                                modifier = Modifier.padding(start = 14.dp, end = 4.dp)
                            )
                        },
                        trailingIcon = {
                            if (cashPaidInput.isNotEmpty()) {
                                IconButton(onClick = { cashPaidInput = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Hapus",
                                        tint = SlateMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarkSlate
                        ),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = VibrantBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = CrispWhite,
                            unfocusedContainerColor = CrispWhite
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cash_amount_input_field")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Auto-Calculated Change & Exact Preset Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pecahan Cepat & Uang Pas:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateMuted
                        )
                        Text(
                            text = "Ketuk untuk isi otomatis",
                            fontSize = 11.sp,
                            color = SlateLight
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Presets Grid / FlowRow
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. "UANG PAS" Button
                        val isPasActive = cashPaid == grandTotal
                        Button(
                            onClick = {
                                cashPaidInput = grandTotal.toInt().toString()
                                focusManager.clearFocus()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPasActive) EmeraldGreen else Color(0xFFEFF6FF)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("cash_preset_pas_button")
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isPasActive) CrispWhite else DeepRoyalBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Uang Pas (${CurrencyFormatter.formatRupiah(grandTotal).replace("Rp ", "")})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPasActive) CrispWhite else DeepRoyalBlue
                            )
                        }

                        // 2. Next Smart Denominations (Rounded up)
                        smartDenominations.forEach { presetAmount ->
                            val isSelected = cashPaid.toInt() == presetAmount
                            OutlinedButton(
                                onClick = {
                                    cashPaidInput = presetAmount.toString()
                                    focusManager.clearFocus()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSelected) DeepRoyalBlue else CrispWhite
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) DeepRoyalBlue else CardBorder
                                ),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Text(
                                    text = CurrencyFormatter.formatRupiah(presetAmount.toDouble()),
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) CrispWhite else DarkSlate
                                )
                            }
                        }

                        // 3. Delta Additive Buttons (+10k, +20k, +50k, +100k)
                        listOf(10000, 20000, 50000, 100000).forEach { delta ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SoftGrayBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier
                                    .height(38.dp)
                                    .clickable {
                                        val current = cashPaidInput.toDoubleOrNull() ?: 0.0
                                        cashPaidInput = (current + delta).toInt().toString()
                                    }
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp)
                                ) {
                                    Text(
                                        text = "+${CurrencyFormatter.formatRupiah(delta.toDouble()).replace("Rp ", "")}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SlateMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Calculated Change or Underpaid Feedback Banner
                    if (cashPaidInput.isNotEmpty()) {
                        if (isCashSufficient) {
                            // EMERALD GREEN KEMBALIAN CARD
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = EmeraldLight),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .background(EmeraldGreen, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = CrispWhite,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (changeAmount == 0.0) "UANG PAS" else "KEMBALIAN KASIR",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = EmeraldDark,
                                                    letterSpacing = 0.5.sp
                                                )
                                                Text(
                                                    text = if (changeAmount == 0.0) "Tidak ada kembalian" else "Kembalikan ke pelanggan",
                                                    fontSize = 11.sp,
                                                    color = EmeraldDark.copy(alpha = 0.8f)
                                                )
                                            }
                                        }

                                        Text(
                                            text = CurrencyFormatter.formatRupiah(changeAmount),
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldDark,
                                            modifier = Modifier.testTag("calculated_change_amount_text")
                                        )
                                    }

                                    // Breakdown of change banknotes (e.g. 1x 20rb, 1x 5rb)
                                    if (changeAmount > 0 && changeBreakdown.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Divider(color = EmeraldGreen.copy(alpha = 0.3f), thickness = 0.8.dp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Saran Uang Pecahan:",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldDark
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = changeBreakdown.joinToString(", ") { "${it.second}x ${CurrencyFormatter.formatRupiah(it.first.toDouble()).replace("Rp ", "")}" },
                                                fontSize = 11.sp,
                                                color = DarkSlate,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // DANGER/WARNING UANG KURANG CARD
                            val shortfall = grandTotal - cashPaid
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = DangerLight),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = null,
                                            tint = DangerRed,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "UANG BELUM CUKUP",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DangerRed
                                            )
                                            Text(
                                                text = "Nominal pembayaran kurang",
                                                fontSize = 11.sp,
                                                color = Color(0xFF991B1B)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "- ${CurrencyFormatter.formatRupiah(shortfall)}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = DangerRed
                                    )
                                }
                            }
                        }
                    }

                } else if (selectedCategoryTab == 1) {
                    // ==========================================
                    // DEDICATED QRIS TOKO PAYMENT VIEW
                    // ==========================================
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CrispWhite),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepRoyalBlue)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        tint = DeepRoyalBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "QRIS Resmi Toko",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DarkSlate
                                        )
                                        Text(
                                            text = storeProfile.storeName.ifBlank { "Toko Anda" },
                                            fontSize = 12.sp,
                                            color = DeepRoyalBlue,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = EmeraldLight
                                ) {
                                    Text(
                                        text = "QRIS Aktif",
                                        color = EmeraldDark,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            val hasCustomQris = !storeProfile.qrisImageUri.isNullOrBlank() && File(storeProfile.qrisImageUri!!).exists()

                            if (hasCustomQris) {
                                // Real uploaded QRIS image from store owner
                                Box(
                                    modifier = Modifier
                                        .size(240.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CrispWhite)
                                        .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .padding(6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AsyncImage(
                                        model = File(storeProfile.qrisImageUri!!),
                                        contentDescription = "Foto QRIS Toko",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(10.dp)),
                                        contentScale = ContentScale.Fit
                                    )
                                }
                            } else {
                                // Live generated QR canvas fallback
                                val qrPayload = remember(grandTotal) {
                                    "00020101021226600016ID.CO.QRIS.WWW01189360000000000000005303360540${grandTotal.toInt()}5802ID"
                                }
                                val qrMatrix = remember(qrPayload) {
                                    BarcodeGenerator.encodeQrMatrix(qrPayload)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CrispWhite)
                                        .border(2.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.size(184.dp)) {
                                        BarcodeGenerator.drawQrOnCanvas(this, qrMatrix, size.width, size.height)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Grand total highlight
                            Text(
                                text = CurrencyFormatter.formatRupiah(grandTotal),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DarkSlate
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Arahkan kamera smartphone pelanggan ke kode QRIS di atas untuk menyelesaikan pembayaran.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // One-tap customer payment complete & dismiss button
                            Button(
                                onClick = {
                                    onProcessPayment(PaymentMethod.QRIS, 0.0, "", 0.0, 0.0)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("confirm_qris_paid_button")
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pelanggan Sudah Bayar (Tutup QRIS)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CrispWhite
                                )
                            }
                        }
                    }
                } else {
                    // ==========================================
                    // DIGITAL PAYMENT SELECTION LIST & DETAILS
                    // ==========================================
                    Text(
                        text = "Pilih Saluran Pembayaran Lainnya:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkSlate
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Digital Payment Methods Selection List
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        digitalPaymentOptions.forEach { option ->
                            val isSelected = selectedDigitalMethod == option.method
                            val borderColor by animateColorAsState(
                                targetValue = if (isSelected) DeepRoyalBlue else CardBorder,
                                label = "border_color"
                            )
                            val containerColor by animateColorAsState(
                                targetValue = if (isSelected) Color(0xFFF0F7FF) else CrispWhite,
                                label = "container_color"
                            )

                            Card(
                                onClick = {
                                    selectedDigitalMethod = option.method
                                    qrisSimulatedSuccess = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = containerColor),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.8.dp else 1.dp,
                                    borderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("digital_method_card_${option.method.name}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .background(
                                                    if (isSelected) DeepRoyalBlue else option.iconBgColor.copy(alpha = 0.12f),
                                                    RoundedCornerShape(8.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = option.icon,
                                                contentDescription = null,
                                                tint = if (isSelected) CrispWhite else option.iconBgColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = option.title,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) DeepRoyalBlue else DarkSlate
                                                )
                                                if (option.badge != null) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = if (isSelected) DeepRoyalBlue else SoftGrayBg,
                                                        border = androidx.compose.foundation.BorderStroke(
                                                            0.5.dp,
                                                            if (isSelected) DeepRoyalBlue else CardBorder
                                                        )
                                                    ) {
                                                        Text(
                                                            text = option.badge,
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) CrispWhite else SlateMuted,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = option.subtitle,
                                                fontSize = 11.sp,
                                                color = SlateMuted,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    // Radio / Check indicator
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) DeepRoyalBlue else SlateLight,
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(DeepRoyalBlue, CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Selected Digital Method Drawer Sub-View
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SoftGrayBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            when (selectedDigitalMethod) {
                                PaymentMethod.QRIS -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "QRIS Dinamis - Standar Pembayaran Nasional",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = DeepRoyalBlue,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Vector QR Canvas generated live
                                        val qrPayload = remember(grandTotal) {
                                            "00020101021226600016ID.CO.QRIS.WWW01189360000000000000005303360540${grandTotal.toInt()}5802ID"
                                        }
                                        val qrMatrix = remember(qrPayload) {
                                            BarcodeGenerator.encodeQrMatrix(qrPayload)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(140.dp)
                                                .background(CrispWhite, RoundedCornerShape(8.dp))
                                                .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                                .padding(6.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Canvas(modifier = Modifier.size(126.dp)) {
                                                BarcodeGenerator.drawQrOnCanvas(this, qrMatrix, size.width, size.height)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (qrisSimulatedSuccess) EmeraldGreen else WarningAmber,
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (qrisSimulatedSuccess) "Pembayaran Dikonfirmasi!" else "Menunggu Scan Pembeli (Aktif 5:00)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (qrisSimulatedSuccess) EmeraldDark else DarkSlate
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Mendukung GoPay, OVO, DANA, ShopeePay, BCA Mobile, Livin Mandiri",
                                            fontSize = 10.sp,
                                            color = SlateMuted,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        OutlinedButton(
                                            onClick = {
                                                qrisSimulatedSuccess = true
                                                Toast.makeText(context, "QRIS Dikonfirmasi Sukses", Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp), tint = EmeraldDark)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Simulasi Scan Pelanggan Berhasil", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                PaymentMethod.DEBIT_CARD, PaymentMethod.CREDIT_CARD -> {
                                    val isDebit = selectedDigitalMethod == PaymentMethod.DEBIT_CARD
                                    Text(
                                        text = if (isDebit) "Pilih Bank Mesin EDC Debit:" else "Pilih Jaringan Kartu Kredit:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val banks = if (isDebit) listOf("BCA", "Mandiri", "BRI", "BNI", "CIMB") else listOf("Visa", "Mastercard", "JCB", "Amex")
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        banks.forEach { bank ->
                                            FilterChip(
                                                selected = selectedBank == bank,
                                                onClick = { selectedBank = bank },
                                                label = { Text(bank, fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = DeepRoyalBlue,
                                                    selectedLabelColor = CrispWhite
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = cardApprovalCode,
                                        onValueChange = { cardApprovalCode = it },
                                        label = { Text("No. Referensi / Kode Approval EDC (Opsional)", fontSize = 11.sp) },
                                        placeholder = { Text("Contoh: APP-983142", fontSize = 11.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CrispWhite,
                                            unfocusedContainerColor = CrispWhite
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("card_approval_code")
                                    )
                                }

                                PaymentMethod.E_WALLET -> {
                                    Text(
                                        text = "Pilih Penyedia E-Wallet:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.horizontalScroll(rememberScrollState())
                                    ) {
                                        listOf("GoPay", "DANA", "OVO", "ShopeePay", "LinkAja").forEach { wallet ->
                                            FilterChip(
                                                selected = selectedEWallet == wallet,
                                                onClick = { selectedEWallet = wallet },
                                                label = { Text(wallet, fontSize = 11.sp) },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = VibrantBlue,
                                                    selectedLabelColor = CrispWhite
                                                ),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = eWalletPhone,
                                        onValueChange = { eWalletPhone = it },
                                        label = { Text("Nomor HP Akun $selectedEWallet (08...)", fontSize = 11.sp) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CrispWhite,
                                            unfocusedContainerColor = CrispWhite
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                PaymentMethod.BANK_TRANSFER -> {
                                    Text(
                                        text = "Virtual Account (VA) Toko:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    val vaNumber = "8808 0812 3456 7890"
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CrispWhite,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("BCA Virtual Account", fontSize = 10.sp, color = SlateMuted)
                                                Text(
                                                    vaNumber,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = FontFamily.Monospace,
                                                    color = DeepRoyalBlue
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(vaNumber.replace(" ", "")))
                                                    Toast.makeText(context, "Nomor VA disalin", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Salin VA", tint = VibrantBlue)
                                            }
                                        }
                                    }
                                }

                                PaymentMethod.SPLIT -> {
                                    Text(
                                        text = "Pembagian Tagihan (Tunai + Non-Tunai):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DarkSlate
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = splitCashAmtInput,
                                        onValueChange = { splitCashAmtInput = it.filter { ch -> ch.isDigit() } },
                                        label = { Text("Bagian 1: Uang Tunai (Rp)") },
                                        prefix = { Text("Rp ") },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = CrispWhite,
                                            unfocusedContainerColor = CrispWhite
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    val part1 = splitCashAmtInput.toDoubleOrNull() ?: 0.0
                                    val part2 = (grandTotal - part1).coerceAtLeast(0.0)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Bagian 2: Sisa Digital (QRIS/EDC)", fontSize = 12.sp, color = SlateMuted)
                                        Text(
                                            CurrencyFormatter.formatRupiah(part2),
                                            fontWeight = FontWeight.Bold,
                                            color = DeepRoyalBlue,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                PaymentMethod.DEBT -> {
                                    if (selectedCustomer != null) {
                                        Column {
                                            Text(
                                                text = "Catat Kas Bon Pelanggan:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DarkSlate
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Nama: ${selectedCustomer.name} (${selectedCustomer.phone})",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = DeepRoyalBlue
                                            )
                                            Text(
                                                text = "Saldo Piutang Sebelumnya: ${CurrencyFormatter.formatRupiah(selectedCustomer.debtBalance)}",
                                                fontSize = 11.sp,
                                                color = SlateMuted
                                            )
                                            Text(
                                                text = "Total Piutang Baru: ${CurrencyFormatter.formatRupiah(selectedCustomer.debtBalance + grandTotal)}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = DangerRed
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("Jatuh Tempo:", fontSize = 11.sp, color = SlateMuted)
                                                listOf(7, 14, 30).forEach { days ->
                                                    FilterChip(
                                                        selected = debtDueDateDays == days,
                                                        onClick = { debtDueDateDays = days },
                                                        label = { Text("$days Hari", fontSize = 10.sp) },
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Perhatian: Harap pilih pelanggan terlebih dahulu di keranjang belanja kasir sebelum mencatat kas bon / piutang toko!",
                                                fontSize = 11.sp,
                                                color = DangerRed
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    Text(
                                        text = "Tekan konfirmasi setelah proses pembayaran di mesin EDC/terminal selesai.",
                                        fontSize = 11.sp,
                                        color = SlateMuted
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Divider(color = CardBorder, thickness = 1.dp)

            // Fixed Bottom Action Confirmation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(0.35f)
                ) {
                    Text(
                        text = "Batal",
                        color = SlateMuted,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                val isConfirmEnabled = when (selectedCategoryTab) {
                    0 -> isCashSufficient
                    1 -> true
                    else -> {
                        if (selectedDigitalMethod == PaymentMethod.DEBT) {
                            selectedCustomer != null
                        } else {
                            true
                        }
                    }
                }

                Button(
                    onClick = {
                        when (selectedCategoryTab) {
                            0 -> onProcessPayment(PaymentMethod.CASH, cashPaid, "", 0.0, 0.0)
                            1 -> onProcessPayment(PaymentMethod.QRIS, 0.0, "", 0.0, 0.0)
                            else -> {
                                when (selectedDigitalMethod) {
                                    PaymentMethod.SPLIT -> {
                                        val part1 = splitCashAmtInput.toDoubleOrNull() ?: 0.0
                                        val part2 = (grandTotal - part1).coerceAtLeast(0.0)
                                        onProcessPayment(PaymentMethod.SPLIT, part1, splitDigitalMethod, part1, part2)
                                    }
                                    else -> {
                                        onProcessPayment(selectedDigitalMethod, 0.0, "", 0.0, 0.0)
                                    }
                                }
                            }
                        }
                    },
                    enabled = isConfirmEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when (selectedCategoryTab) {
                            0, 1 -> EmeraldGreen
                            else -> DeepRoyalBlue
                        },
                        disabledContainerColor = SlateLight.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .weight(0.65f)
                        .testTag("confirm_payment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = CrispWhite,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (selectedCategoryTab) {
                            0 -> if (changeAmount > 0) "Bayar (Kembali ${CurrencyFormatter.formatRupiah(changeAmount)})" else "Selesaikan Bayar Pas"
                            1 -> "Konfirmasi QRIS Berhasil"
                            else -> "Konfirmasi ${selectedDigitalMethod.label}"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = CrispWhite,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Generate sensible Indonesian Rupiah banknote presets for quick cashier cash input
 */
private fun generateSmartCashPresets(grandTotal: Double): List<Int> {
    val totalInt = grandTotal.toInt()
    val presets = mutableSetOf<Int>()

    // Standard Indonesian Banknote Denominations
    val standardNotes = listOf(10000, 20000, 50000, 100000, 200000, 500000)

    // Next 10.000 round up
    val next10k = (ceil(totalInt / 10000.0) * 10000).toInt()
    if (next10k > totalInt) presets.add(next10k)

    // Next 50.000 round up
    val next50k = (ceil(totalInt / 50000.0) * 50000).toInt()
    if (next50k > totalInt) presets.add(next50k)

    // Add standard notes greater than total
    for (note in standardNotes) {
        if (note > totalInt) {
            presets.add(note)
            if (presets.size >= 4) break
        }
    }

    return presets.sorted().take(4)
}

/**
 * Calculates optimal banknote change breakdown (Pecahan Kembalian)
 */
private fun calculateChangeBreakdown(changeAmount: Double): List<Pair<Int, Int>> {
    var remaining = changeAmount.toInt()
    val denominations = listOf(100000, 50000, 20000, 10000, 5000, 2000, 1000)
    val result = mutableListOf<Pair<Int, Int>>()

    for (denom in denominations) {
        if (remaining >= denom) {
            val count = remaining / denom
            result.add(denom to count)
            remaining %= denom
        }
    }
    return result
}

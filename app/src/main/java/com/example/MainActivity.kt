package com.example

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AuthPreferences
import com.example.data.local.OnboardingPhase
import com.example.data.model.StaffUser
import com.example.data.model.UserRole
import com.example.ui.PosTab
import com.example.ui.PosViewModel
import com.example.ui.components.StaffPinDialog
import com.example.ui.components.ThermalReceiptDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.BarcodeCenterScreen
import com.example.ui.screens.CashierScreen
import com.example.ui.screens.CatalogScreen
import com.example.ui.screens.CrmScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.ShiftReportsScreen
import com.example.ui.screens.onboarding.AuthScreen
import com.example.ui.screens.onboarding.BrandSplashScreen
import com.example.ui.screens.onboarding.GuidedOnboardingScreen
import com.example.ui.screens.onboarding.StoreProfileSetupScreen
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DarkSlate
import com.example.ui.theme.DeepRoyalBlue
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftGrayBg
import com.example.ui.theme.VibrantBlue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PosMasterApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosMasterApp(viewModel: PosViewModel = viewModel()) {
    val context = LocalContext.current
    val authPreferences = remember { AuthPreferences(context) }
    var onboardingPhase by remember { mutableStateOf(authPreferences.onboardingPhase) }

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val staffUsers by viewModel.staffUsers.collectAsStateWithLifecycle()
    val shifts by viewModel.shifts.collectAsStateWithLifecycle()
    val storeProfile by viewModel.storeProfile.collectAsStateWithLifecycle()

    val showReceiptDialog by viewModel.showReceiptDialog.collectAsStateWithLifecycle()
    val lastCompletedOrder by viewModel.lastCompletedOrder.collectAsStateWithLifecycle()
    val lastCompletedItems by viewModel.lastCompletedItems.collectAsStateWithLifecycle()

    var showStaffDialog by remember { mutableStateOf(false) }

    val activeShift = remember(shifts) { shifts.find { it.status == "OPEN" } }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidthDp = configuration.screenWidthDp
    val isTabletOrLandscape = isLandscape || screenWidthDp >= 720

    // Synchronize stored preferences on launch
    LaunchedEffect(Unit) {
        if (authPreferences.storeName.isNotBlank()) {
            viewModel.updateStoreProfile(
                storeProfile.copy(
                    storeName = authPreferences.storeName,
                    address = authPreferences.storeAddress,
                    phone = authPreferences.storePhone,
                    logoUri = authPreferences.storeLogoUri,
                    qrisImageUri = authPreferences.storeQrisImageUri
                )
            )
        }
        if (authPreferences.fullName.isNotBlank()) {
            viewModel.switchUser(
                StaffUser(
                    id = "U-OWNER",
                    name = authPreferences.fullName,
                    role = authPreferences.role,
                    pin = authPreferences.pin
                )
            )
        }
    }

    when (onboardingPhase) {
        OnboardingPhase.SPLASH -> {
            BrandSplashScreen(
                onContinue = {
                    val next = if (authPreferences.username.isBlank() || !authPreferences.isLoggedIn) {
                        OnboardingPhase.AUTH
                    } else if (authPreferences.storeName.isBlank()) {
                        OnboardingPhase.STORE_SETUP
                    } else {
                        OnboardingPhase.TUTORIAL
                    }
                    authPreferences.onboardingPhase = next
                    onboardingPhase = next
                }
            )
        }
        OnboardingPhase.AUTH -> {
            AuthScreen(
                authPreferences = authPreferences,
                onAuthSuccess = { username, name, role ->
                    viewModel.switchUser(
                        StaffUser(
                            id = "U-OWNER",
                            name = name,
                            role = role,
                            pin = authPreferences.pin
                        )
                    )
                    val next = if (authPreferences.storeName.isBlank()) OnboardingPhase.STORE_SETUP else OnboardingPhase.TUTORIAL
                    authPreferences.onboardingPhase = next
                    onboardingPhase = next
                }
            )
        }
        OnboardingPhase.STORE_SETUP -> {
            StoreProfileSetupScreen(
                authPreferences = authPreferences,
                onProfileSaved = { profile ->
                    viewModel.updateStoreProfile(profile)
                    authPreferences.onboardingPhase = OnboardingPhase.TUTORIAL
                    onboardingPhase = OnboardingPhase.TUTORIAL
                }
            )
        }
        OnboardingPhase.TUTORIAL -> {
            GuidedOnboardingScreen(
                onCompleteTutorial = { targetTab ->
                    authPreferences.onboardingPhase = OnboardingPhase.COMPLETED
                    onboardingPhase = OnboardingPhase.COMPLETED
                    if (targetTab != null) {
                        viewModel.setTab(targetTab)
                    }
                },
                onSkip = {
                    authPreferences.onboardingPhase = OnboardingPhase.COMPLETED
                    onboardingPhase = OnboardingPhase.COMPLETED
                }
            )
        }
        OnboardingPhase.COMPLETED -> {
            val navItems = remember {
                listOf(
                    Triple(PosTab.CASHIER, "Kasir", Icons.Default.PointOfSale),
                    Triple(PosTab.BARCODE_ENGINE, "Barcode", Icons.Default.QrCode),
                    Triple(PosTab.CATALOG, "Produk", Icons.Default.ShoppingBag),
                    Triple(PosTab.INVENTORY, "Stok", Icons.Default.Inventory2),
                    Triple(PosTab.CRM, "Pelanggan", Icons.Default.People),
                    Triple(PosTab.SHIFT, "Shift", Icons.Default.AccountBalanceWallet),
                    Triple(PosTab.ANALYTICS, "Laporan", Icons.Default.Assessment)
                )
            }

            if (isTabletOrLandscape) {
                // Landscape & Tablet Enterprise Split/Rail Layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(CrispWhite)
                ) {
                    // Enterprise Navigation Rail (Far Left)
                    NavigationRail(
                        containerColor = CrispWhite,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(74.dp),
                        header = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(DeepRoyalBlue, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PointOfSale, contentDescription = null, tint = CrispWhite, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "POS",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = DeepRoyalBlue
                                )
                            }
                        }
                    ) {
                        navItems.forEach { (tab, label, icon) ->
                            val isSelected = currentTab == tab
                            NavigationRailItem(
                                selected = isSelected,
                                onClick = { viewModel.setTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = DeepRoyalBlue,
                                    selectedTextColor = DeepRoyalBlue,
                                    indicatorColor = Color(0xFFEFF6FF),
                                    unselectedIconColor = Color(0xFF64748B),
                                    unselectedTextColor = Color(0xFF64748B)
                                )
                            )
                        }
                    }

                    VerticalDivider(modifier = Modifier.fillMaxHeight().width(1.dp), color = CardBorder)

                    // Main Content Area
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        // Compact Enterprise Top Bar
                        Surface(
                            color = DeepRoyalBlue,
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = storeProfile.storeName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = CrispWhite
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(
                                                if (activeShift != null) EmeraldGreen else Color(0xFFF87171),
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (activeShift != null) "Shift Aktif" else "Shift Tutup",
                                        fontSize = 11.sp,
                                        color = CrispWhite.copy(alpha = 0.9f)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onboardingPhase = OnboardingPhase.TUTORIAL },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.HelpOutline, contentDescription = "Panduan", tint = CrispWhite, modifier = Modifier.size(18.dp))
                                    }

                                    IconButton(
                                        onClick = { onboardingPhase = OnboardingPhase.STORE_SETUP },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Store, contentDescription = "Profil Toko", tint = CrispWhite, modifier = Modifier.size(18.dp))
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    // Staff Role Badge (Clickable to switch staff / PIN)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(CrispWhite.copy(alpha = 0.2f))
                                            .clickable { showStaffDialog = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (currentUser.role == UserRole.OWNER) Icons.Default.AdminPanelSettings else Icons.Default.People,
                                                contentDescription = null,
                                                tint = CrispWhite,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = currentUser.name.take(14),
                                                color = CrispWhite,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            AnimatedContent(
                                targetState = currentTab,
                                transitionSpec = {
                                    (fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                                        scaleIn(initialScale = 0.98f, animationSpec = tween(220)))
                                        .togetherWith(fadeOut(animationSpec = tween(150)))
                                },
                                label = "landscape_tab_transition"
                            ) { targetTab ->
                                when (targetTab) {
                                    PosTab.CASHIER -> CashierScreen(viewModel = viewModel)
                                    PosTab.BARCODE_ENGINE -> BarcodeCenterScreen(viewModel = viewModel)
                                    PosTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                                    PosTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                                    PosTab.CRM -> CrmScreen(viewModel = viewModel)
                                    PosTab.SHIFT -> ShiftReportsScreen(viewModel = viewModel)
                                    PosTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait Mobile Layout with Scaffold & Bottom NavigationBar
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(end = 12.dp)
                                ) {
                                    Column {
                                        Text(
                                            text = storeProfile.storeName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = CrispWhite
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .background(
                                                        if (activeShift != null) EmeraldGreen else Color(0xFFF87171),
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (activeShift != null) "Shift Kasir: Aktif" else "Shift: Kasir Tutup",
                                                fontSize = 11.sp,
                                                color = CrispWhite.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { onboardingPhase = OnboardingPhase.TUTORIAL },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.HelpOutline, contentDescription = "Panduan", tint = CrispWhite, modifier = Modifier.size(20.dp))
                                        }

                                        IconButton(
                                            onClick = { onboardingPhase = OnboardingPhase.STORE_SETUP },
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.Store, contentDescription = "Profil Toko", tint = CrispWhite, modifier = Modifier.size(20.dp))
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Staff Role Badge (Clickable to switch staff / PIN)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(CrispWhite.copy(alpha = 0.2f))
                                                .clickable { showStaffDialog = true }
                                                .padding(horizontal = 10.dp, vertical = 5.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (currentUser.role == UserRole.OWNER) Icons.Default.AdminPanelSettings else Icons.Default.People,
                                                    contentDescription = null,
                                                    tint = CrispWhite,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(5.dp))
                                                Text(
                                                    text = currentUser.name.take(12),
                                                    color = CrispWhite,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepRoyalBlue)
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CrispWhite,
                            tonalElevation = 8.dp,
                            modifier = Modifier.height(64.dp)
                        ) {
                            navItems.forEach { (tab, label, icon) ->
                                val isSelected = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { viewModel.setTab(tab) },
                                    icon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = DeepRoyalBlue,
                                        selectedTextColor = DeepRoyalBlue,
                                        indicatorColor = Color(0xFFEFF6FF),
                                        unselectedIconColor = Color(0xFF64748B),
                                        unselectedTextColor = Color(0xFF64748B)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220, delayMillis = 40)) +
                                    scaleIn(initialScale = 0.98f, animationSpec = tween(220)))
                                    .togetherWith(fadeOut(animationSpec = tween(150)))
                            },
                            label = "portrait_tab_transition"
                        ) { targetTab ->
                            when (targetTab) {
                                PosTab.CASHIER -> CashierScreen(viewModel = viewModel)
                                PosTab.BARCODE_ENGINE -> BarcodeCenterScreen(viewModel = viewModel)
                                PosTab.CATALOG -> CatalogScreen(viewModel = viewModel)
                                PosTab.INVENTORY -> InventoryScreen(viewModel = viewModel)
                                PosTab.CRM -> CrmScreen(viewModel = viewModel)
                                PosTab.SHIFT -> ShiftReportsScreen(viewModel = viewModel)
                                PosTab.ANALYTICS -> AnalyticsScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }

            // Thermal Receipt Dialog after completed checkout
            if (showReceiptDialog && lastCompletedOrder != null) {
                ThermalReceiptDialog(
                    order = lastCompletedOrder!!,
                    items = lastCompletedItems,
                    store = storeProfile,
                    onDismiss = { viewModel.dismissReceiptDialog() },
                    onPrintSuccess = { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                    }
                )
            }

            // Staff / PIN Switcher Dialog
            if (showStaffDialog) {
                StaffPinDialog(
                    currentUser = currentUser,
                    staffUsers = staffUsers,
                    onUserSwitched = { newStaff ->
                        viewModel.switchUser(newStaff)
                        showStaffDialog = false
                        Toast.makeText(context, "Beralih akun ke: ${newStaff.name}", Toast.LENGTH_SHORT).show()
                    },
                    onLogout = {
                        authPreferences.isLoggedIn = false
                        authPreferences.onboardingPhase = OnboardingPhase.AUTH
                        onboardingPhase = OnboardingPhase.AUTH
                        showStaffDialog = false
                    },
                    onDismiss = { showStaffDialog = false }
                )
            }
        }
    }
}

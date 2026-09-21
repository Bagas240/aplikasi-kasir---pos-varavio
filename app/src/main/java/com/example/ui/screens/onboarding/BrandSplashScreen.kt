package com.example.ui.screens.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VoravioLogo
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.LightBlueAccent
import com.example.ui.theme.LightBluePastel
import com.example.ui.theme.LightBluePrimary

@Composable
fun BrandSplashScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowOffset"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBluePrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .graphicsLayer(alpha = alpha.value)
        ) {
            // Official Voravio Brand Logo Graphic
            Box(
                modifier = Modifier.scale(scale.value * pulseScale),
                contentAlignment = Alignment.Center
            ) {
                VoravioLogo(
                    size = 110.dp,
                    onDarkBackground = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subtitle Tag Badge
            Surface(
                color = CrispWhite.copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CrispWhite.copy(alpha = 0.35f))
            ) {
                Text(
                    text = "SMART POINT OF SALE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrispWhite,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // App Brand Name & Subtitle
            Text(
                text = "VORAVIO",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = CrispWhite,
                letterSpacing = 3.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sistem Kasir & Manajemen Toko Pintar\nPenjualan, Barcode Scanner, Stok & Laporan",
                fontSize = 14.sp,
                color = CrispWhite.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick Highlights Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SplashFeaturePill(label = "⚡ Kasir Cepat")
                SplashFeaturePill(label = "📦 Stok & Barcode")
                SplashFeaturePill(label = "📊 Laporan")
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Initialization Action CTA
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = CrispWhite),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(54.dp)
                    .scale(scale.value)
                    .testTag("splash_continue_button")
            ) {
                Text(
                    text = "Mulai Sekarang",
                    color = LightBluePrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = LightBluePrimary,
                    modifier = Modifier.size(18.dp).offset(x = arrowOffset.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Versi 2.0 • Voravio POS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = CrispWhite.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun SplashFeaturePill(label: String) {
    Surface(
        color = CrispWhite.copy(alpha = 0.14f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = CrispWhite,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

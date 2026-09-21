package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.LightBlueAccent
import com.example.ui.theme.LightBluePastel
import com.example.ui.theme.LightBluePrimary

/**
 * Official Voravio brand logo badge featuring the geometric "V" monogram
 * with smart point-of-sale checkout scanner accent.
 */
@Composable
fun VoravioLogo(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    onDarkBackground: Boolean = false
) {
    val containerBg = if (onDarkBackground) CrispWhite else LightBluePrimary
    val vStrokeColor = if (onDarkBackground) LightBluePrimary else CrispWhite
    val accentColor = if (onDarkBackground) LightBlueAccent else LightBluePastel

    val cornerRadius = size * 0.28f
    val borderWidth = (size.value * 0.035f).coerceAtLeast(1.5f).dp

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(cornerRadius), spotColor = Color(0x33000000))
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerBg)
            .border(
                width = borderWidth,
                color = if (onDarkBackground) CrispWhite.copy(alpha = 0.9f) else LightBlueAccent.copy(alpha = 0.6f),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.64f)) {
            val w = this.size.width
            val h = this.size.height
            val strokeW = w * 0.16f

            // Dynamic V Path for Voravio
            val vPath = Path().apply {
                moveTo(w * 0.16f, h * 0.22f)
                lineTo(w * 0.50f, h * 0.78f)
                lineTo(w * 0.84f, h * 0.22f)
            }

            drawPath(
                path = vPath,
                color = vStrokeColor,
                style = Stroke(
                    width = strokeW,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Horizontal scanner beam representing barcode/POS capability
            val beamPath = Path().apply {
                moveTo(w * 0.35f, h * 0.44f)
                lineTo(w * 0.65f, h * 0.44f)
            }
            drawPath(
                path = beamPath,
                color = accentColor,
                style = Stroke(
                    width = strokeW * 0.5f,
                    cap = StrokeCap.Round
                )
            )

            // Dynamic checkout indicator point
            drawCircle(
                color = accentColor,
                radius = w * 0.07f,
                center = Offset(w * 0.50f, h * 0.25f)
            )
        }
    }
}

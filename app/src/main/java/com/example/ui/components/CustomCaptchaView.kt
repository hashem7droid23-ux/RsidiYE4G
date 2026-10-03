package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Custom Captcha Canvas view rendering distorted anti-bot characters,
 * interference curves, and noise dots.
 */
@Composable
fun CustomCaptchaView(
    captchaCode: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val rotationAngle = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1E36), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Captcha Drawing Canvas
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF071224)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("captcha_canvas")
                ) {
                    val width = size.width
                    val height = size.height

                    // Deterministic seed based on captcha text so it stays steady on recompositions
                    val random = Random(captchaCode.hashCode())

                    // Draw noise grid lines
                    val lineColors = listOf(
                        Color(0xFF1E3A8A),
                        Color(0xFF0284C7),
                        Color(0xFF0D9488),
                        Color(0xFFB45309)
                    )
                    repeat(4) {
                        val start = Offset(random.nextFloat() * width, random.nextFloat() * height)
                        val end = Offset(random.nextFloat() * width, random.nextFloat() * height)
                        drawLine(
                            color = lineColors[it % lineColors.size].copy(alpha = 0.5f),
                            start = start,
                            end = end,
                            strokeWidth = 2.5f
                        )
                    }

                    // Draw noise dots
                    repeat(35) {
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = 0.4f),
                            radius = 1.5f + random.nextFloat() * 1.5f,
                            center = Offset(random.nextFloat() * width, random.nextFloat() * height)
                        )
                    }

                    // Render Stylized & Distorted Characters using Native Canvas
                    val textPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 62f
                        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                    }

                    val glyphColors = listOf(
                        android.graphics.Color.parseColor("#38BDF8"),
                        android.graphics.Color.parseColor("#FBBF24"),
                        android.graphics.Color.parseColor("#34D399"),
                        android.graphics.Color.parseColor("#F472B6"),
                        android.graphics.Color.parseColor("#60A5FA")
                    )

                    val charStep = (width - 24f) / (captchaCode.length.coerceAtLeast(1))

                    drawContext.canvas.nativeCanvas.apply {
                        captchaCode.forEachIndexed { index, char ->
                            val color = glyphColors[index % glyphColors.size]
                            textPaint.color = color

                            val x = 12f + (index * charStep) + (random.nextFloat() * 4f - 2f)
                            val y = height / 1.5f + (random.nextFloat() * 8f - 4f)
                            val angle = random.nextFloat() * 24f - 12f

                            save()
                            rotate(angle, x, y)
                            drawText(char.toString(), x, y, textPaint)
                            restore()
                        }
                    }
                }
            }
        }

        // Refresh Captcha Button
        IconButton(
            onClick = {
                coroutineScope.launch {
                    rotationAngle.animateTo(
                        targetValue = rotationAngle.value + 360f,
                        animationSpec = tween(durationMillis = 400)
                    )
                }
                onRefresh()
            },
            modifier = Modifier
                .size(40.dp)
                .testTag("captcha_refresh_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "تحديث رمز التحقق",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.rotate(rotationAngle.value)
            )
        }
    }
}

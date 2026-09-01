package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaCyanLight
import com.example.ui.theme.RimaFuchsia
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaViolet

@Composable
fun RimaLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isAnimated: Boolean = false,
    showBackground: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rima_logo_anim")
    val rotationAngle by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "gradient_rotation"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    val glowAlpha by if (isAnimated) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 0.85f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow_pulse"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0.6f) }
    }

    val cornerRadius = size * 0.28f

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBackground) {
                    Modifier
                        .shadow(
                            elevation = if (isAnimated) 12.dp else 4.dp,
                            shape = RoundedCornerShape(cornerRadius),
                            ambientColor = RimaIndigo.copy(alpha = glowAlpha),
                            spotColor = RimaCyan.copy(alpha = glowAlpha)
                        )
                        .clip(RoundedCornerShape(cornerRadius))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0F1221),
                                    Color(0xFF161A30),
                                    Color(0xFF0D0F1A)
                                )
                            )
                        )
                        .border(
                            width = (size * 0.035f).coerceAtLeast(1.dp),
                            brush = Brush.sweepGradient(
                                listOf(RimaIndigo, RimaViolet, RimaCyan, RimaFuchsia, RimaIndigo)
                            ),
                            shape = RoundedCornerShape(cornerRadius)
                        )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize(0.72f)) {
            val w = this.size.width
            val h = this.size.height

            val gradientBrush = Brush.linearGradient(
                colors = listOf(RimaCyanLight, RimaIndigo, RimaViolet, RimaFuchsia),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )

            val strokeWidth = w * 0.18f

            // Draw stylized futuristic "R"
            val stemPath = Path().apply {
                // Vertical stem with modern rounded cap
                moveTo(w * 0.25f, h * 0.15f)
                lineTo(w * 0.25f, h * 0.85f)
            }

            drawPath(
                path = stemPath,
                brush = gradientBrush,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Upper rounded loop of "R"
            val loopPath = Path().apply {
                moveTo(w * 0.25f, h * 0.18f)
                cubicTo(
                    w * 0.85f, h * 0.18f,
                    w * 0.85f, h * 0.52f,
                    w * 0.25f, h * 0.52f
                )
            }

            drawPath(
                path = loopPath,
                brush = gradientBrush,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Dynamic diagonal futuristic kick/leg of "R"
            val legPath = Path().apply {
                moveTo(w * 0.44f, h * 0.48f)
                cubicTo(
                    w * 0.52f, h * 0.60f,
                    w * 0.65f, h * 0.72f,
                    w * 0.80f, h * 0.85f
                )
            }

            drawPath(
                path = legPath,
                brush = gradientBrush,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Small glowing AI neural node accent dot
            drawCircle(
                color = RimaCyanLight,
                radius = strokeWidth * 0.42f,
                center = Offset(w * 0.80f, h * 0.85f)
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.RimaLogo
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaViolet
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600)
        )
        delay(1200)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF090B14),
                        Color(0xFF101426),
                        Color(0xFF07080E)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .padding(24.dp)
        ) {
            RimaLogo(
                size = 110.dp,
                isAnimated = true,
                showBackground = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Rima AI",
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ask Anything. Get Intelligent Answers.",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = RimaCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(listOf(RimaIndigo.copy(alpha = 0.2f), RimaViolet.copy(alpha = 0.2f))),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Bengali • Banglish • English",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

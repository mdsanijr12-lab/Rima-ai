package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.VoicePlaybackState
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RimaCyan
import com.example.ui.theme.RimaCyanLight
import com.example.ui.theme.RimaFuchsia
import com.example.ui.theme.RimaIndigo
import com.example.ui.theme.RimaViolet

@Composable
fun AudioWaveformVisualizer(
    waveLevels: List<Float>,
    modifier: Modifier = Modifier,
    barCount: Int = 8,
    activeColor: Color = RimaCyan,
    maxHeight: Float = 24f
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val levels = if (waveLevels.size >= barCount) waveLevels.take(barCount) else {
            waveLevels + List(barCount - waveLevels.size) { 0.2f }
        }

        levels.forEachIndexed { index, level ->
            val animatedHeight = animateDpAsState(
                targetValue = (level.coerceIn(0.1f, 1f) * maxHeight).dp,
                animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
                label = "wave_bar_$index"
            )

            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(animatedHeight.value)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(RimaCyanLight, activeColor, RimaViolet)
                        )
                    )
            )
        }
    }
}

@Composable
fun VoiceControlFloatingBar(
    playbackState: VoicePlaybackState,
    waveformLevels: List<Float>,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onReplay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVisible = playbackState is VoicePlaybackState.Playing || playbackState is VoicePlaybackState.Paused

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(16.dp, RoundedCornerShape(18.dp), ambientColor = RimaIndigo.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(RimaIndigo.copy(alpha = 0.6f), RimaCyan.copy(alpha = 0.6f))),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(RimaIndigo, RimaViolet))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Voice playing",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Rima AI Voice",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                when (playbackState) {
                                    is VoicePlaybackState.Playing -> {
                                        AudioWaveformVisualizer(
                                            waveLevels = waveformLevels,
                                            barCount = 5,
                                            maxHeight = 16f
                                        )
                                    }
                                    is VoicePlaybackState.Paused -> {
                                        Text(
                                            text = "(Paused)",
                                            fontSize = 11.sp,
                                            color = RimaCyan,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    else -> {}
                                }
                            }

                            val subText = when (playbackState) {
                                is VoicePlaybackState.Playing -> playbackState.currentSegment
                                is VoicePlaybackState.Paused -> "Playback paused. Tap resume to continue."
                                else -> ""
                            }

                            Text(
                                text = subText,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onReplay,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Replay,
                                contentDescription = "Replay",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        when (playbackState) {
                            is VoicePlaybackState.Playing -> {
                                IconButton(
                                    onClick = onPause,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RimaIndigo)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = "Pause",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            is VoicePlaybackState.Paused -> {
                                IconButton(
                                    onClick = onResume,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(RimaIndigo)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Resume",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            else -> {}
                        }

                        IconButton(
                            onClick = onStop,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                if (playbackState is VoicePlaybackState.Playing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { playbackState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = RimaCyan,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

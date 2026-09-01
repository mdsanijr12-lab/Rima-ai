package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary AI Brand Palette (Electric Indigo & Glowing Cyan)
val RimaIndigo = Color(0xFF6366F1)
val RimaIndigoDark = Color(0xFF4F46E5)
val RimaIndigoLight = Color(0xFF818CF8)

val RimaCyan = Color(0xFF06B6D4)
val RimaCyanLight = Color(0xFF22D3EE)

val RimaViolet = Color(0xFF8B5CF6)
val RimaFuchsia = Color(0xFFD946EF)

// Dark Theme Colors (Obsidian Midnight)
val DarkBackground = Color(0xFF0A0C14)
val DarkSurface = Color(0xFF121626)
val DarkSurfaceVariant = Color(0xFF1A1F36)
val DarkSurfaceCard = Color(0xFF161A2E)
val DarkBorder = Color(0xFF262D4A)

val DarkTextPrimary = Color(0xFFF8FAFC)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)

// Light Theme Colors (Clean Snow & Tech Slate)
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightSurfaceCard = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E8F0)

val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextTertiary = Color(0xFF94A3B8)

// Accent Colors
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val CodeBlockBackgroundDark = Color(0xFF0D1117)
val CodeBlockBackgroundLight = Color(0xFF1E293B)

// Gradients
val RimaGradient = Brush.horizontalGradient(
    colors = listOf(RimaIndigo, RimaViolet, RimaCyan)
)

val RimaCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF1E1B4B).copy(alpha = 0.6f), Color(0xFF0F172A).copy(alpha = 0.8f))
)

val RimaGlowGradient = Brush.radialGradient(
    colors = listOf(RimaIndigo.copy(alpha = 0.35f), Color.Transparent)
)

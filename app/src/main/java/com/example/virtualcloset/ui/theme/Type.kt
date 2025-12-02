package com.example.virtualcloset.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.virtualcloset.R

// --- IMPORTANT: ADD FONT FILES TO res/font/ ---
// Use fallback font family to avoid crashes if custom fonts are missing
val CluelessFont by lazy {
    try {
        FontFamily(Font(R.font.clueless_font))
    } catch (e: Exception) {
        FontFamily.SansSerif
    }
}

val ReadableFont by lazy {
    try {
        FontFamily(Font(R.font.readable_font))
    } catch (e: Exception) {
        FontFamily.SansSerif
    }
}

// Set of Material typography styles to start with
val AppTypography by lazy {
    Typography(
        // For big screen titles
        displayLarge = TextStyle(
            fontFamily = CluelessFont,
            fontWeight = FontWeight.Normal,
            fontSize = 48.sp,
            color = Color.White,
            shadow = Shadow(color = Color.Black, offset = androidx.compose.ui.geometry.Offset(4f, 4f), blurRadius = 3f)
        ),
        // For screen titles
        headlineLarge = TextStyle(
            fontFamily = CluelessFont,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            color = Color.White,
            shadow = Shadow(color = Color.Black, offset = androidx.compose.ui.geometry.Offset(2f, 2f), blurRadius = 2f)
        ),
        // For general text, buttons, and labels
        bodyLarge = TextStyle(
            fontFamily = ReadableFont,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        // For smaller text
        labelSmall = TextStyle(
            fontFamily = ReadableFont,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
        )
    )
}

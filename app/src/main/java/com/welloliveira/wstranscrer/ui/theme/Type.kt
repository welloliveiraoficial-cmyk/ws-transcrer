package com.welloliveira.wstranscrer.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val FontDisplay = FontFamily.Default
val FontBody = FontFamily.Default
val FontMono = FontFamily.Monospace

val WsTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontDisplay, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp),
    headlineMedium = TextStyle(fontFamily = FontDisplay, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = FontDisplay, fontWeight = FontWeight.Bold, fontSize = 17.sp),
    bodyLarge = TextStyle(fontFamily = FontBody, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = FontBody, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelSmall = TextStyle(fontFamily = FontMono, fontWeight = FontWeight.Normal, fontSize = 12.sp)
)

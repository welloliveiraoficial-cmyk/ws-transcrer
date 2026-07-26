package com.welloliveira.wstranscrer.ui.theme

import androidx.compose.ui.graphics.Color

// Fundo
val Bg = Color(0xFF0A0A12)
val BgAlt = Color(0xFF0E0E18)
val Panel = Color(0x0BFFFFFF) // ~4.5% branco, cartão "vidro"
val Panel2 = Color(0xFF191926)
val Line = Color(0x17FFFFFF)

// Azuis da marca
val Deep = Color(0xFF0E5A94)
val Mid = Color(0xFF2F8FE0)
val Sky = Color(0xFF55D6FF)
val SkySoft = Color(0xFF8FE7FF)

// Texto
val Ink = Color(0xFFF6F4FF)
val InkDim = Color(0xFFACA8CC)
val Muted = Color(0xFF66628A)

// Estados
val Danger = Color(0xFFFF7A72)
val Success = Color(0xFF43D9A3)

// Gradientes-chave (usar com Brush.linearGradient / radialGradient)
val GradienteTexto = listOf(Sky, Deep)
val GradienteBotao = listOf(Deep, Mid, Sky)
val GradienteBotaoGravar = listOf(Sky, Deep)

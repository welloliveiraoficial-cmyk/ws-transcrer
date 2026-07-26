package com.welloliveira.wstranscrer.ui.components

import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.welloliveira.wstranscrer.ui.theme.Bg
import com.welloliveira.wstranscrer.ui.theme.Deep
import kotlin.random.Random

@Composable
private fun lembrarTexturaDeGrao(tamanho: Int = 96): ImageBitmap {
    return remember {
        val bitmap = android.graphics.Bitmap.createBitmap(tamanho, tamanho, android.graphics.Bitmap.Config.ARGB_8888)
        val random = Random(42)
        for (y in 0 until tamanho) {
            for (x in 0 until tamanho) {
                val valor = random.nextInt(0, 255)
                val cor = (0xFF shl 24) or (valor shl 16) or (valor shl 8) or valor
                bitmap.setPixel(x, y, cor)
            }
        }
        bitmap.asImageBitmap()
    }
}

@Composable
private fun lembrarPincelDeGrao(textura: ImageBitmap): ShaderBrush {
    return remember(textura) {
        val shader = android.graphics.BitmapShader(
            textura.asAndroidBitmap(),
            Shader.TileMode.REPEAT,
            Shader.TileMode.REPEAT
        )
        ShaderBrush(shader)
    }
}

@Composable
fun Modifier.fundoComIdentidadeVisual(): Modifier {
    val textura = lembrarTexturaDeGrao()
    val pincelGrao = lembrarPincelDeGrao(textura)

    return this
        .background(Bg)
        .drawWithCache {
            val glow = Brush.radialGradient(
                colors = listOf(Deep.copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.5f, 0f),
                radius = size.width * 0.9f
            )
            onDrawBehind {
                drawRect(brush = glow)
            }
        }
        .background(brush = pincelGrao, alpha = 0.045f)
}

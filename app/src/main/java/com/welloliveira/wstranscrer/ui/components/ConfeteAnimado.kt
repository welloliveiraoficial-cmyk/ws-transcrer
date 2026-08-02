package com.welloliveira.wstranscrer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class Confete(
    val corIndice: Int,
    val xInicial: Float,
    val velocidadeX: Float,
    val velocidadeY: Float,
    val tamanho: Float,
    val rotacaoInicial: Float,
    val velocidadeRotacao: Float,
    val atraso: Float
)

private val CoresConfete = listOf(
    Color(0xFF55D6FF),
    Color(0xFF43D9A3),
    Color(0xFF2F8FE0),
    Color(0xFFF6F4FF),
    Color(0xFFFFD166)
)

/**
 * Explosão única de confete, disparada quando [ativo] vira true.
 * Chama [aoFinalizar] sozinho quando a animação termina (~1,6s).
 */
@Composable
fun ConfeteAnimado(ativo: Boolean, aoFinalizar: () -> Unit = {}) {
    if (!ativo) return

    val particulas = remember(ativo) {
        List(60) {
            Confete(
                corIndice = Random.nextInt(CoresConfete.size),
                xInicial = Random.nextFloat(),
                velocidadeX = (Random.nextFloat() - 0.5f) * 0.6f,
                velocidadeY = 0.5f + Random.nextFloat() * 0.5f,
                tamanho = 6f + Random.nextFloat() * 6f,
                rotacaoInicial = Random.nextFloat() * 360f,
                velocidadeRotacao = (Random.nextFloat() - 0.5f) * 900f,
                atraso = Random.nextFloat() * 0.15f
            )
        }
    }

    val progresso = remember(ativo) { Animatable(0f) }

    LaunchedEffect(ativo) {
        progresso.snapTo(0f)
        progresso.animateTo(1f, tween(1600, easing = LinearEasing))
        aoFinalizar()
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val t = progresso.value
        particulas.forEach { p ->
            val tLocal = ((t - p.atraso) / (1f - p.atraso)).coerceIn(0f, 1f)
            if (tLocal <= 0f) return@forEach
            val x = (p.xInicial * size.width) + (p.velocidadeX * size.width * tLocal)
            val queda = 0.5f * 2200f * tLocal * tLocal
            val y = -40f + (p.velocidadeY * size.height * 0.6f * tLocal) + queda
            if (y > size.height + 40f) return@forEach
            val alfa = (1f - tLocal).coerceIn(0f, 1f)
            rotate(p.rotacaoInicial + p.velocidadeRotacao * tLocal, pivot = Offset(x, y)) {
                drawRect(
                    color = CoresConfete[p.corIndice].copy(alpha = alfa),
                    topLeft = Offset(x - p.tamanho / 2, y - p.tamanho / 2),
                    size = Size(p.tamanho, p.tamanho * 0.5f)
                )
            }
        }
    }
}

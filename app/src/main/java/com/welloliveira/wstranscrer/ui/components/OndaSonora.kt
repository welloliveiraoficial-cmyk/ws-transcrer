package com.welloliveira.wstranscrer.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.welloliveira.wstranscrer.ui.theme.Deep
import com.welloliveira.wstranscrer.ui.theme.Sky

@Composable
fun OndaSonora(
    modifier: Modifier = Modifier,
    numeroDeBarras: Int = 5,
    alturaMaxima: androidx.compose.ui.unit.Dp = 40.dp,
    alturaMinima: androidx.compose.ui.unit.Dp = 8.dp,
    larguraBarra: androidx.compose.ui.unit.Dp = 6.dp,
    ativa: Boolean = true
) {
    Row(
        modifier = modifier.height(alturaMaxima),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(numeroDeBarras) { indice ->
            BarraOnda(
                indice = indice,
                total = numeroDeBarras,
                alturaMaxima = alturaMaxima,
                alturaMinima = alturaMinima,
                largura = larguraBarra,
                ativa = ativa
            )
        }
    }
}

@Composable
private fun BarraOnda(
    indice: Int,
    total: Int,
    alturaMaxima: androidx.compose.ui.unit.Dp,
    alturaMinima: androidx.compose.ui.unit.Dp,
    largura: androidx.compose.ui.unit.Dp,
    ativa: Boolean
) {
    val transicao = rememberInfiniteTransition(label = "onda_$indice")

    val duracaoBase = 900
    val duracao = duracaoBase + (indice * 130)
    val atraso = indice * 90

    val progresso by transicao.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duracao, easing = LinearEasing, delayMillis = atraso),
            repeatMode = RepeatMode.Reverse
        ),
        label = "altura_$indice"
    )

    val fatorAtivo = if (ativa) progresso else 0.35f
    val altura = alturaMinima + (alturaMaxima - alturaMinima) * fatorAtivo

    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .width(largura)
            .height(altura)
            .clip(RoundedCornerShape(50))
            .background(Brush.verticalGradient(listOf(Sky, Deep)))
    )
}

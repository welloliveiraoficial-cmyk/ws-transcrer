package com.welloliveira.wstranscrer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.welloliveira.wstranscrer.ui.theme.FontMono
import com.welloliveira.wstranscrer.ui.theme.Ink
import com.welloliveira.wstranscrer.ui.theme.Line
import com.welloliveira.wstranscrer.ui.theme.Muted
import com.welloliveira.wstranscrer.ui.theme.Sky

/**
 * Elementos visuais reutilizáveis com linguagem de "sala de edição de vídeo"
 * (régua de timeline, moldura de viewfinder, cabeçalho técnico).
 * Usa só as cores que já existem no tema (Sky, Muted, Line, Ink) — nenhuma cor nova.
 */

@Composable
fun ReguaTimeline(
    modifier: Modifier = Modifier,
    corPrincipal: Color = Sky,
    corSecundaria: Color = Line
) {
    Canvas(modifier = modifier.fillMaxWidth().height(10.dp)) {
        val passo = 12.dp.toPx()
        var x = 0f
        var i = 0
        while (x < size.width) {
            val marcacaoForte = i % 4 == 0
            val alturaLinha = if (marcacaoForte) size.height else size.height * 0.4f
            drawLine(
                color = if (marcacaoForte) corPrincipal.copy(alpha = 0.6f) else corSecundaria,
                start = Offset(x, size.height),
                end = Offset(x, size.height - alturaLinha),
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
            x += passo
            i++
        }
    }
}

@Composable
fun IconeComMoldura(
    icone: ImageVector,
    tint: Color = Sky,
    tamanho: Dp = 36.dp
) {
    Box(modifier = Modifier.size(tamanho), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(tamanho)) {
            val c = 7.dp.toPx()
            val espessura = 1.6.dp.toPx()
            val cor = tint.copy(alpha = 0.55f)

            drawLine(cor, Offset(0f, c), Offset(0f, 0f), espessura, cap = StrokeCap.Round)
            drawLine(cor, Offset(0f, 0f), Offset(c, 0f), espessura, cap = StrokeCap.Round)

            drawLine(cor, Offset(size.width - c, 0f), Offset(size.width, 0f), espessura, cap = StrokeCap.Round)
            drawLine(cor, Offset(size.width, 0f), Offset(size.width, c), espessura, cap = StrokeCap.Round)

            drawLine(cor, Offset(0f, size.height - c), Offset(0f, size.height), espessura, cap = StrokeCap.Round)
            drawLine(cor, Offset(0f, size.height), Offset(c, size.height), espessura, cap = StrokeCap.Round)

            drawLine(cor, Offset(size.width - c, size.height), Offset(size.width, size.height), espessura, cap = StrokeCap.Round)
            drawLine(cor, Offset(size.width, size.height - c), Offset(size.width, size.height), espessura, cap = StrokeCap.Round)
        }
        Icon(icone, contentDescription = null, tint = tint, modifier = Modifier.size(tamanho * 0.48f))
    }
}

@Composable
fun CabecalhoTecnico(
    titulo: String,
    status: String,
    corStatus: Color = Sky
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(corStatus, CircleShape)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            status.uppercase(),
            color = Muted,
            fontFamily = FontMono,
            fontSize = 11.sp,
            letterSpacing = 1.5.sp
        )
    }
    Spacer(Modifier.height(6.dp))
    Text(
        titulo,
        style = MaterialTheme.typography.headlineMedium,
        color = Ink
    )
    Spacer(Modifier.height(10.dp))
    ReguaTimeline()
}

/** Pequena marcação vertical usada antes de títulos de seção, tipo tick de timeline. */
@Composable
fun MarcadorSecao(texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(10.dp)
                .background(Sky.copy(alpha = 0.7f))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            texto.uppercase(),
            color = Muted,
            fontFamily = FontMono,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )
    }
}

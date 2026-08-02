package com.welloliveira.wstranscrer.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.welloliveira.wstranscrer.R
import com.welloliveira.wstranscrer.ui.theme.Bg
import com.welloliveira.wstranscrer.ui.theme.Deep
import com.welloliveira.wstranscrer.ui.theme.Ink
import com.welloliveira.wstranscrer.ui.theme.InkDim
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashAnimado(aoFinalizar: () -> Unit) {
    val escalaLogo = remember { Animatable(0.7f) }
    val alphaLogo = remember { Animatable(0f) }
    val alphaTexto = remember { Animatable(0f) }
    val alphaSaida = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        launch { escalaLogo.animateTo(1f, tween(650, easing = FastOutSlowInEasing)) }
        launch { alphaLogo.animateTo(1f, tween(650)) }
        delay(350)
        alphaTexto.animateTo(1f, tween(500))
        delay(850)
        alphaSaida.animateTo(0f, tween(400))
        aoFinalizar()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = alphaSaida.value }
            .background(Brush.verticalGradient(listOf(Deep, Bg)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(150.dp)
                    .graphicsLayer {
                        scaleX = escalaLogo.value
                        scaleY = escalaLogo.value
                        alpha = alphaLogo.value
                    }
            )

            Spacer(Modifier.height(18.dp))

            OndaSonora(numeroDeBarras = 5, alturaMaxima = 28.dp, alturaMinima = 6.dp)

            Spacer(Modifier.height(22.dp))

            Text(
                text = "Ws Transcrer",
                color = Ink,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = alphaTexto.value }
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "áudio e vídeo em texto",
                color = InkDim,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = alphaTexto.value }
            )
        }
    }
}

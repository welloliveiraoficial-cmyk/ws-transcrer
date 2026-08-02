package com.welloliveira.wstranscrer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.welloliveira.wstranscrer.BuildConfig
import com.welloliveira.wstranscrer.ui.components.DialogAtualizacaoSeDisponivel
import com.welloliveira.wstranscrer.ui.components.fundoComIdentidadeVisual
import com.welloliveira.wstranscrer.ui.screens.ConfiguracoesScreen
import com.welloliveira.wstranscrer.ui.screens.EnviarScreen
import com.welloliveira.wstranscrer.ui.screens.TranscricoesScreen
import com.welloliveira.wstranscrer.ui.theme.Bg
import com.welloliveira.wstranscrer.update.AtualizacaoState
import com.welloliveira.wstranscrer.update.UpdateChecker
import kotlinx.coroutines.launch

@Composable
fun WsAppRoot(idTranscricaoParaAbrir: Long?) {
    var abaAtual by remember {
        mutableStateOf(if (idTranscricaoParaAbrir != null) AbaPrincipal.TRANSCRICOES else AbaPrincipal.ENVIAR)
    }

    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        escopo.launch {
            val checker = UpdateChecker(BuildConfig.VERSION_CHECK_BASE_URL)
            val remota = checker.verificar(BuildConfig.VERSION_CODE)
            if (remota != null) {
                AtualizacaoState.versaoDisponivel.value = remota
            }
        }
    }

    Scaffold(containerColor = Bg) { paddingInterno ->
        Box(modifier = Modifier.fillMaxSize().fundoComIdentidadeVisual()) {
            Box(modifier = Modifier.padding(paddingInterno).fillMaxSize()) {
                when (abaAtual) {
                    AbaPrincipal.ENVIAR -> EnviarScreen()
                    AbaPrincipal.TRANSCRICOES -> TranscricoesScreen(idAbrirInicialmente = idTranscricaoParaAbrir)
                    AbaPrincipal.CONFIGURACOES -> ConfiguracoesScreen()
                }
            }

            BottomDock(
                abaAtual = abaAtual,
                aoSelecionar = { abaAtual = it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 18.dp)
            )

            DialogAtualizacaoSeDisponivel()
        }
    }
}

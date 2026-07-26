package com.welloliveira.wstranscrer.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.welloliveira.wstranscrer.ui.theme.InkDim
import com.welloliveira.wstranscrer.ui.theme.Muted
import com.welloliveira.wstranscrer.update.ApkDownloader
import com.welloliveira.wstranscrer.update.AtualizacaoState
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun DialogAtualizacaoSeDisponivel() {
    val versao = AtualizacaoState.versaoDisponivel.value ?: return
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val baixando = AtualizacaoState.baixando.value
    val progresso = AtualizacaoState.progresso.value
    val erro = AtualizacaoState.erro.value

    AlertDialog(
        onDismissRequest = { if (!baixando) AtualizacaoState.versaoDisponivel.value = null },
        title = { Text("Nova versão disponível 🔄") },
        text = {
            Column {
                Text("A versão ${versao.versionName} já está disponível.")
                if (versao.changelog.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    versao.changelog.forEach { item ->
                        Text("• $item", color = InkDim)
                    }
                }
                if (baixando) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(progress = progresso, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(4.dp))
                    Text("Baixando… ${(progresso * 100).toInt()}%", color = Muted)
                }
                erro?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = com.welloliveira.wstranscrer.ui.theme.Danger)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !baixando,
                onClick = {
                    escopo.launch {
                        try {
                            AtualizacaoState.erro.value = null
                            AtualizacaoState.baixando.value = true
                            val downloader = ApkDownloader(context)
                            val arquivo = downloader.baixar(versao.apkUrl) { p ->
                                AtualizacaoState.progresso.value = p
                            }
                            AtualizacaoState.baixando.value = false
                            AtualizacaoState.versaoDisponivel.value = null
                            context.startActivity(downloader.criarIntentInstalacao(arquivo))
                        } catch (e: Exception) {
                            AtualizacaoState.baixando.value = false
                            AtualizacaoState.erro.value = e.message ?: "Não foi possível baixar a atualização."
                        }
                    }
                }
            ) { Text(if (baixando) "Baixando…" else "Atualizar Agora") }
        },
        dismissButton = {
            TextButton(enabled = !baixando, onClick = { AtualizacaoState.versaoDisponivel.value = null }) {
                Text("Mais Tarde")
            }
        }
    )
}

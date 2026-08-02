package com.welloliveira.wstranscrer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.welloliveira.wstranscrer.BuildConfig
import com.welloliveira.wstranscrer.R
import com.welloliveira.wstranscrer.data.AppDatabase
import com.welloliveira.wstranscrer.ui.theme.*
import com.welloliveira.wstranscrer.update.UpdateChecker
import kotlinx.coroutines.launch

private enum class TelaConfig { PRINCIPAL, SOBRE, PRIVACIDADE }

@Composable
fun ConfiguracoesScreen() {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var tela by remember { mutableStateOf(TelaConfig.PRINCIPAL) }
    var mostrarConfirmarLimpeza by remember { mutableStateOf(false) }
    var totalSalvas by remember { mutableStateOf(0) }
    var mensagemUpdate by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        totalSalvas = AppDatabase.get(context).transcricaoDao().contar()
    }

    when (tela) {
        TelaConfig.SOBRE -> TelaTexto(
            titulo = stringResource(R.string.config_sobre_app),
            texto = stringResource(R.string.sobre_texto, BuildConfig.VERSION_NAME),
            aoVoltar = { tela = TelaConfig.PRINCIPAL }
        )
        TelaConfig.PRIVACIDADE -> TelaTexto(
            titulo = stringResource(R.string.config_privacidade),
            texto = stringResource(R.string.privacidade_texto),
            aoVoltar = { tela = TelaConfig.PRINCIPAL }
        )
        TelaConfig.PRINCIPAL -> {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 110.dp)
            ) {
                Text(stringResource(R.string.tab_configuracoes), style = MaterialTheme.typography.headlineMedium, color = Ink)
                Spacer(Modifier.height(20.dp))

                SecaoTitulo(stringResource(R.string.config_secao_preferencias))
                LinhaConfig(icone = Icons.Filled.DarkMode, titulo = stringResource(R.string.config_tema), subtitulo = stringResource(R.string.config_tema_valor))
                LinhaConfig(icone = Icons.Filled.Language, titulo = stringResource(R.string.config_idioma), subtitulo = stringResource(R.string.config_idioma_valor))
                LinhaConfig(icone = Icons.Filled.Notifications, titulo = stringResource(R.string.config_notificacoes), subtitulo = "Ativadas")

                Spacer(Modifier.height(20.dp))
                SecaoTitulo(stringResource(R.string.config_secao_historico))
                LinhaConfig(
                    icone = Icons.Filled.Delete,
                    titulo = stringResource(R.string.config_limpar_historico),
                    subtitulo = "$totalSalvas transcrições salvas",
                    clicavel = true,
                    aoClicar = { mostrarConfirmarLimpeza = true }
                )

                Spacer(Modifier.height(20.dp))
                SecaoTitulo(stringResource(R.string.config_secao_atualizacoes))
                LinhaConfig(
                    icone = Icons.Filled.Refresh,
                    titulo = stringResource(R.string.config_verificar_atualizacoes),
                    subtitulo = mensagemUpdate ?: "Versão instalada: ${BuildConfig.VERSION_NAME}",
                    clicavel = true,
                    aoClicar = {
                        escopo.launch {
                            val checker = UpdateChecker(BuildConfig.VERSION_CHECK_BASE_URL)
                            val remota = checker.verificar(BuildConfig.VERSION_CODE)
                            if (remota != null) {
                                mensagemUpdate = "Nova versão disponível: ${remota.versionName}"
                                com.welloliveira.wstranscrer.update.AtualizacaoState.versaoDisponivel.value = remota
                            } else {
                                mensagemUpdate = "Você já está na versão mais recente."
                            }
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))
                SecaoTitulo(stringResource(R.string.config_secao_sobre))
                LinhaConfig(
                    icone = Icons.Filled.Info,
                    titulo = stringResource(R.string.config_sobre_app),
                    subtitulo = null,
                    clicavel = true,
                    seta = true,
                    aoClicar = { tela = TelaConfig.SOBRE }
                )
                LinhaConfig(
                    icone = Icons.Filled.Shield,
                    titulo = stringResource(R.string.config_privacidade),
                    subtitulo = null,
                    clicavel = true,
                    seta = true,
                    aoClicar = { tela = TelaConfig.PRIVACIDADE }
                )
            }

            if (mostrarConfirmarLimpeza) {
                AlertDialog(
                    onDismissRequest = { mostrarConfirmarLimpeza = false },
                    title = { Text("Limpar histórico?") },
                    text = { Text("Isso vai apagar todas as $totalSalvas transcrições salvas no aparelho. Essa ação não pode ser desfeita.") },
                    confirmButton = {
                        TextButton(onClick = {
                            escopo.launch {
                                AppDatabase.get(context).transcricaoDao().limparTudo()
                                totalSalvas = 0
                            }
                            mostrarConfirmarLimpeza = false
                        }) { Text("Apagar tudo", color = Danger) }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarConfirmarLimpeza = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}

@Composable
private fun SecaoTitulo(texto: String) {
    Text(
        texto.uppercase(),
        color = Muted,
        fontFamily = FontMono,
        fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
        modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)
    )
}

@Composable
private fun LinhaConfig(
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String?,
    clicavel: Boolean = false,
    seta: Boolean = false,
    aoClicar: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel2)
            .then(if (clicavel) Modifier.clickable { aoClicar() } else Modifier)
            .padding(14.dp)
            .padding(bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Panel),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, contentDescription = null, tint = Sky, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(titulo, color = Ink, fontWeight = FontWeight.Bold)
            subtitulo?.let { Text(it, color = Muted, fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)) }
        }
        if (seta) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Muted)
        }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun TelaTexto(titulo: String, texto: String, aoVoltar: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 110.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = aoVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Ink)
            }
            Text(titulo, color = Ink, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(16.dp))
        Text(texto, color = InkDim, style = MaterialTheme.typography.bodyMedium)
    }
}

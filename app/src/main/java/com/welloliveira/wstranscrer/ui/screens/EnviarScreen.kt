package com.welloliveira.wstranscrer.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.welloliveira.wstranscrer.BuildConfig
import com.welloliveira.wstranscrer.R
import com.welloliveira.wstranscrer.data.AppDatabase
import com.welloliveira.wstranscrer.data.Transcricao
import com.welloliveira.wstranscrer.network.BackendApi
import com.welloliveira.wstranscrer.notifications.NotificationHelper
import com.welloliveira.wstranscrer.repository.StatusTranscricao
import com.welloliveira.wstranscrer.repository.TranscricaoRepository
import com.welloliveira.wstranscrer.ui.components.ConfeteAnimado
import com.welloliveira.wstranscrer.ui.components.OndaSonora
import com.welloliveira.wstranscrer.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File

private data class ArquivoSelecionado(val uri: Uri, val nome: String, val tamanho: Long, val mimeType: String)

@Composable
fun EnviarScreen() {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    var arquivo by remember { mutableStateOf<ArquivoSelecionado?>(null) }
    var status by remember { mutableStateOf<StatusTranscricao?>(null) }
    var processando by remember { mutableStateOf(false) }
    var textoResultado by remember { mutableStateOf<String?>(null) }
    var mostrarConfete by remember { mutableStateOf(false) }
    var passosOuvindo by remember { mutableStateOf(0) }

    val progressoAlvo = calcularProgresso(status, passosOuvindo)
    val progressoAnimado by animateFloatAsState(
        targetValue = progressoAlvo,
        animationSpec = tween(durationMillis = 500),
        label = "progresso_transcricao"
    )

    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { arquivo = lerMetadados(context, it) }
    }

    val fonteInteracaoBotao = remember { MutableInteractionSource() }
    val botaoPressionado by fonteInteracaoBotao.collectIsPressedAsState()
    val escalaBotao by animateFloatAsState(
        targetValue = if (botaoPressionado) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escala_botao_upload"
    )

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp, bottom = 110.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Panel2)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Success)
                )
                Text(
                    stringResource(R.string.hero_status),
                    color = InkDim,
                    fontFamily = FontMono,
                    fontSize = 12.sp()
                )
            }

            Spacer(Modifier.height(20.dp))

            OndaSonora(ativa = processando)

            Spacer(Modifier.height(20.dp))

            Text(
                buildWordmark(),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Text(
                stringResource(R.string.hero_tagline),
                color = InkDim,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(82.dp)
                    .scale(escalaBotao)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradienteBotaoGravar))
                    .clickable(
                        interactionSource = fonteInteracaoBotao,
                        indication = null,
                        enabled = !processando
                    ) {
                        seletor.launch(arrayOf("audio/*", "video/*"))
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.FileUpload, contentDescription = "Selecionar arquivo", tint = Ink)
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipDestaque(stringResource(R.string.chip_global))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipDestaque(stringResource(R.string.chip_rapido))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ChipDestaque(stringResource(R.string.chip_seguro))
            }

            Spacer(Modifier.height(24.dp))

            AnimatedVisibility(
                visible = arquivo != null,
                enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
                exit = fadeOut(tween(150))
            ) {
                arquivo?.let { arq ->
                    Column(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Panel2)
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(arq.nome, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text(formatarTamanho(arq.tamanho), color = Muted, fontFamily = FontMono, fontSize = 12.sp())
                            }
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Remover",
                                tint = Muted,
                                modifier = Modifier.clickable(enabled = !processando) {
                                    arquivo = null; textoResultado = null; status = null
                                }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                processando = true
                                textoResultado = null
                                passosOuvindo = 0
                                escopo.launch {
                                    val arquivoLocal = copiarParaCache(context, arq.uri, arq.nome)
                                    val api = BackendApi.criar(BuildConfig.BACKEND_BASE_URL)
                                    val repo = TranscricaoRepository(api)
                                    val resultado = repo.transcrever(arquivoLocal, arq.mimeType) { novoStatus ->
                                        status = novoStatus
                                        if (novoStatus is StatusTranscricao.Ouvindo) {
                                            passosOuvindo += 1
                                        }
                                    }
                                    processando = false
                                    status = resultado
                                    when (resultado) {
                                        is StatusTranscricao.Sucesso -> {
                                            textoResultado = resultado.texto
                                            val dao = AppDatabase.get(context).transcricaoDao()
                                            val id = dao.inserir(
                                                Transcricao(nomeArquivo = arq.nome, tamanhoBytes = arq.tamanho, texto = resultado.texto)
                                            )
                                            dao.aplicarLimite()
                                            NotificationHelper.notificarSucesso(context, arq.nome, id)
                                            mostrarConfete = true
                                        }
                                        is StatusTranscricao.Falha -> {
                                            NotificationHelper.notificarFalha(context, resultado.motivo)
                                        }
                                        else -> {}
                                    }
                                }
                            },
                            enabled = !processando,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.linearGradient(GradienteBotao), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(stringResource(R.string.btn_transcrever), color = Ink, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = processando,
                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
            ) {
                Column(Modifier.fillMaxWidth().padding(top = 20.dp)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Panel2)
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AnimatedContent(
                                targetState = status?.let { textoStatus(it) } ?: "",
                                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(150)) },
                                label = "texto_status"
                            ) { texto ->
                                Text(texto, color = InkDim, fontFamily = FontMono, fontSize = 13.sp())
                            }
                            Text(
                                "${(progressoAnimado * 100).toInt()}%",
                                color = Sky,
                                fontFamily = FontMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp()
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progressoAnimado },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(50)),
                            color = Sky,
                            trackColor = Panel
                        )

                        Spacer(Modifier.height(14.dp))

                        OndaSonora(numeroDeBarras = 4, alturaMaxima = 22.dp, alturaMinima = 6.dp, larguraBarra = 4.dp, ativa = true)
                    }
                }
            }

            AnimatedVisibility(
                visible = status is StatusTranscricao.Falha,
                enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                val falha = status as? StatusTranscricao.Falha
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Panel2)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, tint = Danger)
                    Spacer(Modifier.width(10.dp))
                    Text(falha?.motivo ?: "Falha ao transcrever.", color = Danger, fontSize = 13.sp())
                }
            }

            AnimatedVisibility(
                visible = textoResultado != null,
                enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 5 },
                exit = fadeOut(tween(150))
            ) {
                textoResultado?.let { texto ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Panel2)
                            .padding(16.dp)
                    ) {
                        Text(texto, color = Ink, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Transcrição", texto))
                            }) { Text(stringResource(R.string.btn_copiar)) }

                            OutlinedButton(onClick = {
                                compartilharTexto(context, texto)
                            }) { Text(stringResource(R.string.btn_compartilhar)) }
                        }
                    }
                }
            }
        }

        ConfeteAnimado(ativo = mostrarConfete) { mostrarConfete = false }
    }
}

@Composable
private fun ChipDestaque(texto: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Panel2)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(texto, color = InkDim, fontSize = 12.sp())
    }
}

@Composable
private fun buildWordmark(): androidx.compose.ui.text.AnnotatedString =
    androidx.compose.ui.text.buildAnnotatedString {
        withStyle(androidx.compose.ui.text.SpanStyle(color = Ink)) { append("Ws ") }
        withStyle(androidx.compose.ui.text.SpanStyle(color = Sky)) { append("Transcrer") }
    }

private fun textoStatus(status: StatusTranscricao): String = when (status) {
    StatusTranscricao.Enviando -> "enviando o arquivo…"
    is StatusTranscricao.EnviandoProgresso -> "enviando o arquivo… ${status.percentual}%"
    StatusTranscricao.Processando -> "processando no servidor…"
    StatusTranscricao.Ouvindo -> "ouvindo o áudio…"
    StatusTranscricao.IdentificandoIdioma -> "identificando o idioma…"
    StatusTranscricao.Escrevendo -> "escrevendo o texto…"
    is StatusTranscricao.Ocupado -> "servidor ocupado, tentando de novo (${status.tentativa}/${status.maximo})…"
    is StatusTranscricao.Sucesso -> "transcrição concluída ✅"
    is StatusTranscricao.Falha -> "falha: ${status.motivo}"
}

/**
 * Calcula uma porcentagem estimada e contínua para o processo inteiro de
 * transcrição (não só o upload). As etapas de processamento no servidor não
 * têm progresso real reportado pela API, então avançamos aos poucos a cada
 * novo "ouvindo" recebido, sem nunca dar a impressão de travar.
 */
private fun calcularProgresso(status: StatusTranscricao?, passosOuvindo: Int): Float = when (status) {
    null -> 0f
    StatusTranscricao.Enviando -> 0.04f
    is StatusTranscricao.EnviandoProgresso -> 0.05f + (status.percentual.coerceIn(0, 100) / 100f) * 0.25f
    StatusTranscricao.Processando -> 0.32f
    StatusTranscricao.Ouvindo -> (0.34f + (passosOuvindo.coerceAtMost(30) * 0.011f)).coerceAtMost(0.66f)
    StatusTranscricao.IdentificandoIdioma -> 0.74f
    StatusTranscricao.Escrevendo -> 0.86f
    is StatusTranscricao.Ocupado -> 0.92f
    is StatusTranscricao.Sucesso -> 1f
    is StatusTranscricao.Falha -> 0f
}

private fun formatarTamanho(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}

private fun lerMetadados(context: Context, uri: Uri): ArquivoSelecionado? {
    val resolver = context.contentResolver
    var nome = "arquivo"
    var tamanho = 0L
    resolver.query(uri, null, null, null, null)?.use { cursor ->
        val idxNome = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        val idxTamanho = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (idxNome >= 0) nome = cursor.getString(idxNome) ?: nome
            if (idxTamanho >= 0) tamanho = cursor.getLong(idxTamanho)
        }
    }
    val mime = resolver.getType(uri) ?: "application/octet-stream"
    return ArquivoSelecionado(uri, nome, tamanho, mime)
}

private fun copiarParaCache(context: Context, uri: Uri, nomeArquivo: String): File {
    val destino = File(context.cacheDir, nomeArquivo)
    context.contentResolver.openInputStream(uri)?.use { entrada ->
        destino.outputStream().use { saida -> entrada.copyTo(saida) }
    }
    return destino
}

private fun compartilharTexto(context: Context, texto: String) {
    val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(android.content.Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(android.content.Intent.createChooser(intent, "Compartilhar transcrição"))
}

private fun Int.sp() = androidx.compose.ui.unit.TextUnit(this.toFloat(), androidx.compose.ui.unit.TextUnitType.Sp)

package com.welloliveira.wstranscrer.ui.screens

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.welloliveira.wstranscrer.R
import com.welloliveira.wstranscrer.data.AppDatabase
import com.welloliveira.wstranscrer.data.Transcricao
import com.welloliveira.wstranscrer.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun TranscricoesScreen(idAbrirInicialmente: Long? = null) {
    val context = LocalContext.current
    val dao = remember { AppDatabase.get(context).transcricaoDao() }
    val escopo = rememberCoroutineScope()

    var busca by remember { mutableStateOf("") }
    val itens by (if (busca.isBlank()) dao.observarTodas() else dao.buscar(busca))
        .collectAsState(initial = emptyList())

    var selecionada by remember { mutableStateOf<Transcricao?>(null) }

    LaunchedEffect(idAbrirInicialmente, itens) {
        if (idAbrirInicialmente != null && selecionada == null) {
            selecionada = itens.find { it.id == idAbrirInicialmente }
        }
    }

    // mantém a transcrição aberta em dia após alternar favorito / recarregar lista
    LaunchedEffect(itens) {
        selecionada?.let { atual ->
            selecionada = itens.find { it.id == atual.id } ?: selecionada
        }
    }

    if (selecionada != null) {
        DetalheTranscricao(
            transcricao = selecionada!!,
            aoVoltar = { selecionada = null },
            aoExcluir = {
                escopo.launch { dao.excluir(selecionada!!) }
                selecionada = null
            },
            aoAlternarFavorito = { fav ->
                val atual = selecionada!!
                escopo.launch { dao.definirFavorito(atual.id, fav) }
                selecionada = atual.copy(favorito = fav)
            }
        )
        return
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 110.dp)) {
        Text(stringResource(R.string.tab_transcricoes), style = MaterialTheme.typography.headlineMedium, color = Ink)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = busca,
            onValueChange = { busca = it },
            placeholder = { Text(stringResource(R.string.busca_placeholder), color = Muted) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(16.dp))

        if (itens.isEmpty()) {
            Column(
                Modifier.fillMaxWidth().padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Description, contentDescription = null, tint = Muted, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    stringResource(R.string.historico_vazio),
                    color = Muted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(itens, key = { it.id }) { item ->
                    ItemTranscricao(
                        item = item,
                        aoClicar = { selecionada = item },
                        aoAlternarFavorito = { fav ->
                            escopo.launch { dao.definirFavorito(item.id, fav) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemTranscricao(
    item: Transcricao,
    aoClicar: () -> Unit,
    aoAlternarFavorito: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Panel2)
            .clickable { aoClicar() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.nomeArquivo, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Text(
                formatarData(item.criadoEm),
                color = Muted,
                fontFamily = FontMono,
                fontSize = 12.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = if (item.favorito) Icons.Filled.Star else Icons.Filled.StarBorder,
            contentDescription = "Favoritar",
            tint = if (item.favorito) Sky else Muted,
            modifier = Modifier
                .size(24.dp)
                .clickable { aoAlternarFavorito(!item.favorito) }
        )
    }
}

@Composable
private fun DetalheTranscricao(
    transcricao: Transcricao,
    aoVoltar: () -> Unit,
    aoExcluir: () -> Unit,
    aoAlternarFavorito: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var favorito by remember(transcricao.id) { mutableStateOf(transcricao.favorito) }
    var visivel by remember { mutableStateOf(false) }

    LaunchedEffect(transcricao.id) {
        visivel = false
        visivel = true
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 110.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Ink,
                modifier = Modifier.clickable { aoVoltar() }
            )
            Spacer(Modifier.width(12.dp))
            Text(
                transcricao.nomeArquivo,
                style = MaterialTheme.typography.titleMedium,
                color = Ink,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (favorito) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Favoritar",
                tint = if (favorito) Sky else Muted,
                modifier = Modifier
                    .size(26.dp)
                    .clickable {
                        favorito = !favorito
                        aoAlternarFavorito(favorito)
                    }
            )
        }

        Spacer(Modifier.height(4.dp))
        Text(formatarData(transcricao.criadoEm), color = Muted, fontFamily = FontMono, fontSize = 12.sp)

        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visivel,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { it / 6 })
        ) {
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Panel2)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(transcricao.texto, color = InkDim, style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotaoAcao(
                texto = stringResource(R.string.btn_compartilhar),
                icone = Icons.Filled.Share,
                modifier = Modifier.weight(1f)
            ) { compartilharTexto(context, transcricao.texto) }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            BotaoAcao(
                texto = stringResource(R.string.btn_baixar),
                icone = Icons.Filled.Description,
                modifier = Modifier.weight(1f)
            ) { baixarComoTxt(context, transcricao) }

            BotaoAcao(
                texto = stringResource(R.string.btn_exportar_pdf),
                icone = Icons.Filled.PictureAsPdf,
                modifier = Modifier.weight(1f)
            ) { exportarComoPdf(context, transcricao) }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = aoExcluir) {
            Text("Excluir transcrição", color = Danger)
        }
    }
}

@Composable
private fun BotaoAcao(
    texto: String,
    icone: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    aoClicar: () -> Unit
) {
    OutlinedButton(
        onClick = aoClicar,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(icone, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(texto, color = Ink, fontSize = 13.sp)
    }
}

private fun formatarData(millis: Long): String {
    val formato = SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    return formato.format(java.util.Date(millis))
}

private fun compartilharTexto(context: android.content.Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar transcrição"))
}

private fun baixarComoTxt(context: android.content.Context, transcricao: Transcricao) {
    val pasta = File(context.cacheDir, "transcricoes").apply { mkdirs() }
    val arquivo = File(pasta, "${nomeSeguro(transcricao.nomeArquivo)}.txt")
    arquivo.writeText(transcricao.texto)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Salvar ou compartilhar .txt"))
}

private fun exportarComoPdf(context: android.content.Context, transcricao: Transcricao) {
    val pasta = File(context.cacheDir, "transcricoes").apply { mkdirs() }
    val arquivo = File(pasta, "${nomeSeguro(transcricao.nomeArquivo)}.pdf")

    val documento = PdfDocument()
    val paint = Paint().apply { textSize = 12f }
    val margem = 40
    val larguraPagina = 595
    val alturaPagina = 842
    val larguraTexto = larguraPagina - margem * 2

    var paginaAtual = documento.startPage(
        PdfDocument.PageInfo.Builder(larguraPagina, alturaPagina, 1).create()
    )
    var canvas = paginaAtual.canvas
    var y = margem + 20

    val paragrafos = transcricao.texto.split("\n")
    for (paragrafo in paragrafos) {
        var restante = paragrafo
        if (restante.isEmpty()) {
            y += 18
        }
        while (restante.isNotEmpty()) {
            val quantidade = paint.breakText(restante, true, larguraTexto.toFloat(), null)
            val linha = restante.substring(0, quantidade)
            restante = restante.substring(quantidade)

            if (y > alturaPagina - margem) {
                documento.finishPage(paginaAtual)
                paginaAtual = documento.startPage(
                    PdfDocument.PageInfo.Builder(larguraPagina, alturaPagina, documento.pages.size + 1).create()
                )
                canvas = paginaAtual.canvas
                y = margem + 20
            }

            canvas.drawText(linha, margem.toFloat(), y.toFloat(), paint)
            y += 18
        }
    }
    documento.finishPage(paginaAtual)

    FileOutputStream(arquivo).use { saida -> documento.writeTo(saida) }
    documento.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Salvar ou compartilhar PDF"))
}

private fun nomeSeguro(nome: String): String {
    val semExtensao = nome.substringBeforeLast(".")
    return semExtensao.ifBlank { "transcricao" }.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
}

package com.welloliveira.wstranscrer.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
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

    if (selecionada != null) {
        DetalheTranscricao(
            transcricao = selecionada!!,
            aoVoltar = { selecionada = null },
            aoExcluir = {
                escopo.launch { dao.excluir(selecionada!!) }
                selecionada = null
            },
            aoAlternarFavorito = { fav ->
                escopo.launch { dao.definirFavorito(selecionada!!.id, fav) }
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
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(itens, key = { it.id }) { item ->
                    ItemTranscricao(
                        item = item,
                        aoClicar = { selecionada = item },
                        aoExcluir = { escopo.launch { dao.excluir(item) } },
                        aoAlternarFavorito = { escopo.launch { dao.definirFavorito(item.id, !item.favorito) } }
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
    aoExcluir: () -> Unit,
    aoAlternarFavorito: () -> Unit
) {
    val formato = remember { SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR")) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Panel2)
            .clickable { aoClicar() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Description, contentDescription = null, tint = Sky)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.nomeArquivo, color = Ink, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                "${formato.format(item.criadoEm)} · ${formatarTamanhoLocal(item.tamanhoBytes)}",
                color = Muted,
                fontFamily = FontMono,
                fontSize = androidx.compose.ui.unit.TextUnit(11f, androidx.compose.ui.unit.TextUnitType.Sp)
            )
        }
        Icon(
            if (item.favorito) Icons.Filled.Star else Icons.Filled.StarBorder,
            contentDescription = "Favoritar",
            tint = if (item.favorito) Sky else Muted,
            modifier = Modifier.clickable { aoAlternarFavorito() }
        )
        Spacer(Modifier.width(12.dp))
        Icon(
            Icons.Filled.Close,
            contentDescription = "Excluir",
            tint = Muted,
            modifier = Modifier.clickable { aoExcluir() }
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
    var visivel by remember { mutableStateOf(false) }
    var favoritoLocal by remember(transcricao.id) { mutableStateOf(transcricao.favorito) }
    LaunchedEffect(transcricao.id) { visivel = true }

    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 110.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = aoVoltar) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Ink)
            }
            Text(
                transcricao.nomeArquivo,
                color = Ink,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            Icon(
                if (favoritoLocal) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Favoritar",
                tint = if (favoritoLocal) Sky else Muted,
                modifier = Modifier.clickable {
                    favoritoLocal = !favoritoLocal
                    aoAlternarFavorito(favoritoLocal)
                }
            )
        }
        Spacer(Modifier.height(16.dp))

        AnimatedVisibility(
            visible = visivel,
            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 6 }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Panel2)
                    .padding(16.dp)
            ) {
                Text(transcricao.texto, color = Ink, style = MaterialTheme.typography.bodyMedium)
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Transcrição", transcricao.texto))
            }) { Text(stringResource(R.string.btn_copiar)) }

            OutlinedButton(onClick = {
                compartilharTexto(context, transcricao.texto)
            }) { Text(stringResource(R.string.btn_compartilhar)) }
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = {
                exportarTxt(context, transcricao.nomeArquivo, transcricao.texto)
            }) { Text(stringResource(R.string.btn_baixar)) }

            OutlinedButton(onClick = {
                exportarPdf(context, transcricao.nomeArquivo, transcricao.texto)
            }) { Text(stringResource(R.string.btn_exportar_pdf)) }
        }

        Spacer(Modifier.height(10.dp))

        OutlinedButton(onClick = aoExcluir, modifier = Modifier.fillMaxWidth()) {
            Text("Excluir", color = Danger)
        }
    }
}

private fun compartilharTexto(context: android.content.Context, texto: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, texto)
    }
    context.startActivity(Intent.createChooser(intent, "Compartilhar transcrição"))
}

private fun exportarTxt(context: android.content.Context, nomeArquivo: String, texto: String) {
    val pasta = File(context.cacheDir, "transcricoes").apply { mkdirs() }
    val nomeBase = nomeArquivo.substringBeforeLast('.', nomeArquivo)
    val arquivo = File(pasta, "$nomeBase.txt")
    arquivo.writeText(texto)

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar como TXT"))
}

private fun exportarPdf(context: android.content.Context, nomeArquivo: String, texto: String) {
    val documento = PdfDocument()
    val larguraPagina = 595
    val alturaPagina = 842
    val margem = 40f
    val paint = android.graphics.Paint().apply {
        textSize = 12f
        color = android.graphics.Color.BLACK
    }
    val larguraTexto = larguraPagina - margem * 2

    val linhas = mutableListOf<String>()
    texto.split("\n").forEach { paragrafo ->
        if (paragrafo.isBlank()) {
            linhas.add("")
        } else {
            var restante = paragrafo
            while (restante.isNotEmpty()) {
                var corte = paint.breakText(restante, true, larguraTexto, null)
                if (corte <= 0) corte = restante.length
                linhas.add(restante.substring(0, corte))
                restante = restante.substring(corte)
            }
        }
    }

    val linhasPorPagina = ((alturaPagina - margem * 2) / 16f).toInt().coerceAtLeast(1)
    var indice = 0
    var numeroPagina = 1
    while (indice < linhas.size || numeroPagina == 1) {
        val pageInfo = PdfDocument.PageInfo.Builder(larguraPagina, alturaPagina, numeroPagina).create()
        val pagina = documento.startPage(pageInfo)
        var y = margem + 12f
        var contador = 0
        while (indice < linhas.size && contador < linhasPorPagina) {
            pagina.canvas.drawText(linhas[indice], margem, y, paint)
            y += 16f
            indice++
            contador++
        }
        documento.finishPage(pagina)
        numeroPagina++
    }

    val pasta = File(context.cacheDir, "transcricoes").apply { mkdirs() }
    val nomeBase = nomeArquivo.substringBeforeLast('.', nomeArquivo)
    val arquivo = File(pasta, "$nomeBase.pdf")
    FileOutputStream(arquivo).use { documento.writeTo(it) }
    documento.close()

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exportar como PDF"))
}

private fun formatarTamanhoLocal(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}

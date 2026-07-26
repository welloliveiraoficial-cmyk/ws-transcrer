package com.welloliveira.wstranscrer.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.welloliveira.wstranscrer.R
import com.welloliveira.wstranscrer.data.AppDatabase
import com.welloliveira.wstranscrer.data.Transcricao
import com.welloliveira.wstranscrer.ui.theme.*
import kotlinx.coroutines.launch
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
                        aoExcluir = { escopo.launch { dao.excluir(item) } }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemTranscricao(item: Transcricao, aoClicar: () -> Unit, aoExcluir: () -> Unit) {
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
            Icons.Filled.Close,
            contentDescription = "Excluir",
            tint = Muted,
            modifier = Modifier.clickable { aoExcluir() }
        )
    }
}

@Composable
private fun DetalheTranscricao(transcricao: Transcricao, aoVoltar: () -> Unit, aoExcluir: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).padding(top = 24.dp, bottom = 110.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = aoVoltar) {
                Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Ink)
            }
            Text(transcricao.nomeArquivo, color = Ink, style = MaterialTheme.typography.titleMedium, maxLines = 1)
        }
        Spacer(Modifier.height(16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Panel2)
                .padding(16.dp)
        ) {
            Text(transcricao.texto, color = Ink, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Transcrição", transcricao.texto))
            }) { Text(stringResource(R.string.btn_copiar)) }

            OutlinedButton(onClick = aoExcluir) {
                Text("Excluir", color = Danger)
            }
        }
    }
}

private fun formatarTamanhoLocal(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.1f MB".format(mb) else "%.0f KB".format(kb)
}

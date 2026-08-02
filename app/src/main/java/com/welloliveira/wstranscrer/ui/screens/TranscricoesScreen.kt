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

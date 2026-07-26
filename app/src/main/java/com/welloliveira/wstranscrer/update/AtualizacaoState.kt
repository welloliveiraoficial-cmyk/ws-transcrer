package com.welloliveira.wstranscrer.update

import androidx.compose.runtime.mutableStateOf

object AtualizacaoState {
    val versaoDisponivel = mutableStateOf<VersaoRemota?>(null)
    val baixando = mutableStateOf(false)
    val progresso = mutableStateOf(0f)
    val erro = mutableStateOf<String?>(null)
}

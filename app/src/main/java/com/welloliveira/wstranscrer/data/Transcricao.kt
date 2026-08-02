package com.welloliveira.wstranscrer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcricoes")
data class Transcricao(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nomeArquivo: String,
    val tamanhoBytes: Long,
    val texto: String,
    val criadoEm: Long = System.currentTimeMillis(),
    val favorito: Boolean = false
)

package com.welloliveira.wstranscrer.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranscricaoDao {

    @Query("SELECT * FROM transcricoes ORDER BY favorito DESC, criadoEm DESC")
    fun observarTodas(): Flow<List<Transcricao>>

    @Query("""
        SELECT * FROM transcricoes
        WHERE nomeArquivo LIKE '%' || :busca || '%' OR texto LIKE '%' || :busca || '%'
        ORDER BY favorito DESC, criadoEm DESC
    """)
    fun buscar(busca: String): Flow<List<Transcricao>>

    @Insert
    suspend fun inserir(transcricao: Transcricao): Long

    @Delete
    suspend fun excluir(transcricao: Transcricao)

    @Query("UPDATE transcricoes SET favorito = :favorito WHERE id = :id")
    suspend fun definirFavorito(id: Long, favorito: Boolean)

    @Query("DELETE FROM transcricoes")
    suspend fun limparTudo()

    @Query("SELECT COUNT(*) FROM transcricoes")
    suspend fun contar(): Int

    @Query("""
        DELETE FROM transcricoes WHERE id NOT IN (
            SELECT id FROM transcricoes ORDER BY criadoEm DESC LIMIT 50
        )
    """)
    suspend fun aplicarLimite()
}

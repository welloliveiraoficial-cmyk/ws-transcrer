package com.welloliveira.wstranscrer.repository

import com.welloliveira.wstranscrer.network.BackendApi
import com.welloliveira.wstranscrer.network.CheckFileRequest
import com.welloliveira.wstranscrer.network.GeminiUploadClient
import com.welloliveira.wstranscrer.network.StartUploadRequest
import com.welloliveira.wstranscrer.network.TranscribeRequest
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

sealed class StatusTranscricao {
    object Enviando : StatusTranscricao()
    data class EnviandoProgresso(val percentual: Int) : StatusTranscricao()
    object Processando : StatusTranscricao()
    object Ouvindo : StatusTranscricao()
    object IdentificandoIdioma : StatusTranscricao()
    object Escrevendo : StatusTranscricao()
    data class Ocupado(val tentativa: Int, val maximo: Int) : StatusTranscricao()
    data class Sucesso(val texto: String) : StatusTranscricao()
    data class Falha(val motivo: String) : StatusTranscricao()
}

class TranscricaoRepository(
    private val api: BackendApi,
    private val uploadClient: GeminiUploadClient = GeminiUploadClient()
) {
    suspend fun transcrever(
        arquivo: File,
        mimeType: String,
        aoAtualizarStatus: suspend (StatusTranscricao) -> Unit
    ): StatusTranscricao {
        return try {
            aoAtualizarStatus(StatusTranscricao.Enviando)
            val sessao = api.startUpload(
                StartUploadRequest(arquivo.name, mimeType, arquivo.length())
            )

            // Envia o arquivo reportando o percentual em tempo real, sem travar
            // as outras atualizações de status (roda numa coroutine filha que é
            // cancelada assim que o upload termina).
            val progressoAtual = AtomicInteger(0)
            val resultadoUpload = coroutineScope {
                val jobProgresso = launch {
                    while (isActive) {
                        aoAtualizarStatus(StatusTranscricao.EnviandoProgresso(progressoAtual.get()))
                        delay(150)
                    }
                }
                val resultado = uploadClient.enviarArquivo(sessao.uploadUrl, arquivo, mimeType) { percentual ->
                    progressoAtual.set(percentual)
                }
                jobProgresso.cancel()
                resultado
            }

            if (!resultadoUpload.sucesso) {
                return StatusTranscricao.Falha("Não foi possível enviar o arquivo para o Google.")
            }
            val nomeGoogle = resultadoUpload.nomeGoogle
                ?: return StatusTranscricao.Falha("O Google não retornou um identificador de arquivo.")

            aoAtualizarStatus(StatusTranscricao.Processando)

            var tentativasProcessamento = 0
            var estado = resultadoUpload.state ?: "PROCESSING"
            var fileUri: String? = resultadoUpload.uri
            var fileMime: String? = mimeType

            while (estado == "PROCESSING" && tentativasProcessamento < 200) {
                delay(3000)
                aoAtualizarStatus(StatusTranscricao.Ouvindo)
                val arquivoGoogle = api.checkFile(CheckFileRequest(name = nomeGoogle))
                estado = arquivoGoogle.state ?: "PROCESSING"
                fileUri = arquivoGoogle.uri
                fileMime = arquivoGoogle.mimeType ?: mimeType
                tentativasProcessamento++
            }

            if (estado != "ACTIVE" || fileUri == null) {
                return StatusTranscricao.Falha("O Google não conseguiu processar o arquivo a tempo.")
            }

            aoAtualizarStatus(StatusTranscricao.IdentificandoIdioma)
            aoAtualizarStatus(StatusTranscricao.Escrevendo)

            val maxTentativas = 4
            var ultimaFalha = "Erro desconhecido."
            for (tentativa in 1..maxTentativas) {
                try {
                    val resultado = api.transcribe(TranscribeRequest(fileUri, fileMime ?: mimeType))
                    if (!resultado.text.isNullOrBlank()) {
                        return StatusTranscricao.Sucesso(resultado.text)
                    }
                    ultimaFalha = resultado.error ?: "Falha ao transcrever."
                } catch (e: Exception) {
                    ultimaFalha = e.message ?: "Erro de rede."
                }
                if (tentativa < maxTentativas) {
                    aoAtualizarStatus(StatusTranscricao.Ocupado(tentativa, maxTentativas))
                    delay(2000L * tentativa)
                }
            }
            StatusTranscricao.Falha(ultimaFalha)
        } catch (e: Exception) {
            StatusTranscricao.Falha(e.message ?: "Erro inesperado.")
        }
    }
}

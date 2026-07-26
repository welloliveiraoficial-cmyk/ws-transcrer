package com.welloliveira.wstranscrer.network

import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.util.concurrent.TimeUnit

/** Resultado do upload direto: nome do arquivo no Google (ex: "files/abc123") + uri + estado. */
data class ResultadoUpload(
    val sucesso: Boolean,
    val nomeGoogle: String? = null,
    val uri: String? = null,
    val state: String? = null
)

/**
 * Envia os bytes do arquivo direto pra URL de upload retornada pelo /api/start-upload.
 * Isso só é possível em app nativo: CORS bloqueava esse envio direto no navegador (PWA),
 * mas não existe essa restrição em requisições feitas pelo OkHttp no Android.
 */
class GeminiUploadClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.MINUTES) // arquivos podem ser grandes (até 2GB)
        .readTimeout(2, TimeUnit.MINUTES)
        .build()

    suspend fun enviarArquivo(uploadUrl: String, arquivo: File, mimeType: String): ResultadoUpload =
        withContext(Dispatchers.IO) {
            val corpo = arquivo.asRequestBody(mimeType.toMediaTypeOrNull())
            val request = Request.Builder()
                .url(uploadUrl)
                .addHeader("X-Goog-Upload-Offset", "0")
                .addHeader("X-Goog-Upload-Command", "upload, finalize")
                .post(corpo)
                .build()

            client.newCall(request).execute().use { resposta ->
                if (!resposta.isSuccessful) return@withContext ResultadoUpload(sucesso = false)
                val corpoResposta = resposta.body?.string().orEmpty()
                try {
                    // Resposta esperada do Google: { "file": { "name", "uri", "mimeType", "state" } }
                    val json = JsonParser.parseString(corpoResposta).asJsonObject
                    val fileObj = json.getAsJsonObject("file")
                    ResultadoUpload(
                        sucesso = true,
                        nomeGoogle = fileObj?.get("name")?.asString,
                        uri = fileObj?.get("uri")?.asString,
                        state = fileObj?.get("state")?.asString
                    )
                } catch (e: Exception) {
                    ResultadoUpload(sucesso = true) // upload ok, mas não deu pra ler metadados
                }
            }
        }
}

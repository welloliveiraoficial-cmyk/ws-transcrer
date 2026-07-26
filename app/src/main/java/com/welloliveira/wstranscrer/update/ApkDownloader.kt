package com.welloliveira.wstranscrer.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.util.concurrent.TimeUnit

class ApkDownloader(private val context: Context) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.MINUTES)
        .build()

    suspend fun baixar(apkUrl: String, aoAtualizarProgresso: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        val pastaApk = File(context.cacheDir, "apk").apply { mkdirs() }
        val destino = File(pastaApk, "atualizacao.apk")
        if (destino.exists()) destino.delete()

        val request = Request.Builder().url(apkUrl).build()
        client.newCall(request).execute().use { resposta ->
            if (!resposta.isSuccessful) throw Exception("Falha ao baixar atualização (código ${resposta.code}).")
            val corpo = resposta.body ?: throw Exception("Resposta vazia do servidor.")
            val total = corpo.contentLength()
            var lido = 0L

            corpo.byteStream().use { entrada ->
                destino.outputStream().use { saida ->
                    val buffer = ByteArray(8 * 1024)
                    var bytes = entrada.read(buffer)
                    while (bytes >= 0) {
                        saida.write(buffer, 0, bytes)
                        lido += bytes
                        if (total > 0) {
                            aoAtualizarProgresso(lido.toFloat() / total.toFloat())
                        }
                        bytes = entrada.read(buffer)
                    }
                }
            }
        }
        destino
    }

    fun criarIntentInstalacao(arquivoApk: File): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", arquivoApk)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
    }
}

package com.welloliveira.wstranscrer.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.welloliveira.wstranscrer.MainActivity
import com.welloliveira.wstranscrer.R

object NotificationHelper {
    private const val CANAL_TRANSCRICOES = "canal_transcricoes"
    private const val CANAL_ATUALIZACOES = "canal_atualizacoes"

    fun criarCanais(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CANAL_TRANSCRICOES, "Transcrições", NotificationManager.IMPORTANCE_DEFAULT)
        )
        manager.createNotificationChannel(
            NotificationChannel(CANAL_ATUALIZACOES, "Atualizações", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    fun notificarSucesso(context: Context, nomeArquivo: String, idTranscricao: Long) {
        val intent = MainActivity.criarIntentAbrirTranscricao(context, idTranscricao)
        val pendingIntent = android.app.PendingIntent.getActivity(
            context, idTranscricao.toInt(), intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        val notificacao = NotificationCompat.Builder(context, CANAL_TRANSCRICOES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_sucesso_titulo))
            .setContentText(nomeArquivo)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(idTranscricao.toInt(), notificacao)
    }

    fun notificarFalha(context: Context, motivo: String) {
        val notificacao = NotificationCompat.Builder(context, CANAL_TRANSCRICOES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_falha_titulo))
            .setContentText(motivo)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacao)
    }

    fun notificarAtualizacao(context: Context, versaoNome: String) {
        val notificacao = NotificationCompat.Builder(context, CANAL_ATUALIZACOES)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.notif_update_titulo))
            .setContentText("Versão $versaoNome disponível")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(9999, notificacao)
    }
}

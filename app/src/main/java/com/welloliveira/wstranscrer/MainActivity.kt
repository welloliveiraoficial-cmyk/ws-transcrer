package com.welloliveira.wstranscrer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.welloliveira.wstranscrer.ui.navigation.WsAppRoot
import com.welloliveira.wstranscrer.ui.theme.WsTranscrerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val idTranscricaoAbrir = intent?.getLongExtra(EXTRA_TRANSCRICAO_ID, -1L)?.takeIf { it >= 0 }

        setContent {
            WsTranscrerTheme {
                WsAppRoot(idTranscricaoParaAbrir = idTranscricaoAbrir)
            }
        }
    }

    companion object {
        private const val EXTRA_TRANSCRICAO_ID = "extra_transcricao_id"

        fun criarIntentAbrirTranscricao(context: Context, idTranscricao: Long): Intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_TRANSCRICAO_ID, idTranscricao)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
    }
}

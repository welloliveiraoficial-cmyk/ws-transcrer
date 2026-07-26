package com.welloliveira.wstranscrer

import android.app.Application
import com.welloliveira.wstranscrer.notifications.NotificationHelper

class WsTranscrerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.criarCanais(this)
    }
}

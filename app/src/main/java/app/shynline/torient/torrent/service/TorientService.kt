package app.shynline.torient.torrent.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class TorientService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return TorientBinder(this)
    }

    fun foreground() {

    }

    fun background() {

    }

    class TorientBinder(val service: TorientService) : Binder()
}
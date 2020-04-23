package app.shynline.torient.torrent.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder

class TorientService : ObservableService<TorientService.Listener>() {

    interface Listener {

    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return TorientBinder(this)
    }


    class TorientBinder(val service: TorientService) : Binder()
}
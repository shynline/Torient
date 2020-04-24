package app.shynline.torient.torrent.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType

class TorientService : ObservableService<TorientService.Listener>(), ITorrentService,
    AlertListener {

    interface Listener {

    }

    private lateinit var session: SessionManager

    override fun alert(p0: Alert<*>?) {
        val type = p0?.type()
        when (type) {
            AlertType.ADD_TORRENT -> {
                (p0 as? AddTorrentAlert)?.handle()?.resume()
            }
            AlertType.BLOCK_FINISHED -> {

            }
            AlertType.TORRENT_FINISHED -> {

            }
        }
    }

    override fun types(): IntArray {
        return intArrayOf()
    }

    override fun onCreate() {
        super.onCreate()
        session = SessionManager()
        session.addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        session.removeListener(this)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return TorientBinder(this)
    }


    class TorientBinder(val service: TorientService) : Binder()

    override fun startSession() {
        session.start()
    }

    override fun stopSession() {
        session.stop()
    }
}
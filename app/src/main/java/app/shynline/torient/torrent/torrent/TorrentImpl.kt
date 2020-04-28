package app.shynline.torient.torrent.torrent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.model.TorrentStats
import app.shynline.torient.torrent.service.BaseObservable
import app.shynline.torient.torrent.service.Observable
import app.shynline.torient.torrent.service.TorientService

import com.frostwire.jlibtorrent.TorrentInfo
import com.frostwire.jlibtorrent.alerts.ListenSucceededAlert
import com.frostwire.jlibtorrent.alerts.StatsAlert


class TorrentImpl(private val context: Context) : BaseObservable<Torrent.Listener>(),
    ServiceConnection, Torrent,
    TorrentController, TorientService.Listener, Observable<Torrent.Listener> {
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)
    private val downloadRequestQueue: HashSet<String> = hashSetOf()

    private val torrentsInfo: MutableMap<String, TorrentInfo> = hashMapOf()

    override fun onActivityStart() {
        // hide service notification if exist
        // bind to service
        bindService()
        // app should decide to start service and downloading base on database info on torrents

        //temp
        context.startService(intent)

    }

    override fun onActivityStop() {
        // show service notification if it's downloading
        // unbind service
        unbindService()
        // stop service if not downloading

        //temp
        service?.stopSession()
        context.stopService(intent)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.service = (service as? TorientService.TorientBinder)?.service
        this.service?.registerListener(this)
        this.service?.startSession()
        downloadRequestQueue.forEach {
            downloadTorrent(it)
        }
    }

    private fun unbindService() {
        if (service != null) {
            service!!.unRegisterListener(this)
            context.unbindService(this)
            service = null
        }
    }

    private fun bindService() {
        if (service == null) {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    private fun makeServiceForeground() {

    }

    override fun downloadTorrent(magnet: String) {
        if (service != null) {
            service!!.downloadTorrent(magnet)
        } else {
            downloadRequestQueue.add(magnet)
        }
    }


    override fun getTorrentIdentifier(data: ByteArray): TorrentIdentifier {
        val torrentInfo = TorrentInfo(data)
        val identifier = TorrentIdentifier.from(torrentInfo)
        torrentsInfo[identifier.infoHash] = torrentInfo
        return identifier
    }

    override fun getTorrentDetail(infoHash: String): TorrentDetail? {
        torrentsInfo[infoHash]?.let {
            return TorrentDetail.from(it)
        }
        return null
    }

    override fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail? {
        return getTorrentDetail(identifier.infoHash)
    }


    override fun onAlertStats(alert: StatsAlert) {
        val stat = TorrentStats(
            alert.handle().infoHash().toHex()
        )
        getListeners().forEach {
            it.onStatReceived(stat)
        }
    }

    override fun onAlertListenSucceeded(alert: ListenSucceededAlert) {

    }
}
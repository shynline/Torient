package app.shynline.torient.torrent.torrent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.shynline.torient.common.downloadDir
import app.shynline.torient.common.observable.Observable
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentIdentifier
import app.shynline.torient.torrent.service.TorientService
import com.frostwire.jlibtorrent.SessionManager
import com.frostwire.jlibtorrent.TorrentInfo
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TorrentImpl(private val context: Context) : BaseTorrent(),
    ServiceConnection,
    TorrentController,
    Observable<Torrent.Listener> {
    private val session: SessionManager = SessionManager()
    private var isActivityRunning = false
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)

    private val torrentsInfo: MutableMap<String, TorrentDetail> = hashMapOf()

    init {
        session.start()
    }

    override fun onActivityStart() {
        isActivityRunning = true
        handleServiceState()
    }

    override fun onActivityStop() {
        isActivityRunning = false
        handleServiceState()
    }

    private fun handleServiceState() = GlobalScope.launch {
        if (isActivityRunning) {
            if (service != null) {
                service!!.background()
            } else {
                if (!session.isRunning) {
                    session.start()
                }
            }
        } else {
            if (torrentsHandles.isEmpty()) {
                session.stop() //blocking
                unbindService()
                context.stopService(intent)
            } else {
                if (service != null) {
                    service!!.foreground()
                } else {
                    bindService()
                    context.startService(intent)
                }
            }
        }
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.service = (service as? TorientService.TorientBinder)?.service
        handleServiceState()
    }

    private fun unbindService() {
        if (service != null) {

            context.unbindService(this)
            service = null
        }
    }

    private fun bindService() {
        if (service == null) {
            context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        }
    }

    ///////////////////////////////////////////////////////////////
    // Torrent interface impl

    override suspend fun addTorrent(identifier: TorrentIdentifier) {
        addTorrent(identifier.magnet)
    }

    override suspend fun addTorrent(magnet: String) {
        session.download(magnet, context.downloadDir)
    }


    override suspend fun getTorrentDetail(identifier: TorrentIdentifier): TorrentDetail? {
        if (torrentsInfo.containsKey(identifier.infoHash)) {
            return torrentsInfo[identifier.infoHash]
        }
        return getTorrentDetail(identifier.magnet)
    }

    override suspend fun getTorrentDetail(magnet: String): TorrentDetail? {
        // Waiting for at most 10 seconds to find at least 10 dht nodes if doesn't exist
        var times = 0
        while (dhtNodes < 10 && times < 10) {
            delay(1000)
            times += 1
        }
        val bytes: ByteArray? = session.fetchMagnet(magnet, 30)
        return getTorrentDetail(bytes)
    }

    override suspend fun getTorrentDetail(data: ByteArray?): TorrentDetail? {
        val torrentInfo = TorrentInfo(data)
        val torrentDetail = TorrentDetail.from(torrentInfo)
        torrentsInfo[torrentDetail.infoHash] = torrentDetail
        return torrentDetail
    }

    override suspend fun resumeTorrent(infoHash: String) {
        torrentsHandles[infoHash]?.resume()
    }

    override suspend fun pauseTorrent(infoHash: String) {
        torrentsHandles[infoHash]?.pause()
    }


    override suspend fun removeTorrent(infoHash: String): Boolean {
        if (torrentsHandles.containsKey(infoHash)) {
            session.remove(torrentsHandles[infoHash]!!)
            return true
        }
        return false
    }

}
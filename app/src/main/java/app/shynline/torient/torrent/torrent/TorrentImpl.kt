package app.shynline.torient.torrent.torrent

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.shynline.torient.TorrentDetail
import app.shynline.torient.TorrentIdentifier
import app.shynline.torient.torrent.service.TorientService

import com.frostwire.jlibtorrent.TorrentInfo


class TorrentImpl(private val context: Context) : ServiceConnection, Torrent,
    TorrentController, TorientService.Listener {
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)

    private val torrentsInfo: MutableMap<String, TorrentInfo> = hashMapOf()

    override fun onActivityStart() {
    }

    override fun onActivityStop() {
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.service = (service as? TorientService.TorientBinder)?.service
        this.service?.registerListener(this)
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
}
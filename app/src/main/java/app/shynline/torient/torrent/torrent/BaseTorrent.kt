package app.shynline.torient.torrent.torrent

import app.shynline.torient.common.observable.BaseObservable
import app.shynline.torient.database.datasource.torrent.InternalTorrentDataSource
import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import com.frostwire.jlibtorrent.AlertListener
import com.frostwire.jlibtorrent.Sha1Hash
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentStatus
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert
import kotlinx.coroutines.*


abstract class BaseTorrent(
    protected open val internalTorrentDataSource: InternalTorrentDataSource,
    protected open val torrentFilePriorityDataSource: TorrentFilePriorityDataSource
) : BaseObservable<Torrent.Listener>(), Torrent, AlertListener {
    protected val managedTorrents: MutableMap<String, TorrentStatus.State?> = hashMapOf()

    protected lateinit var torrentScope: CoroutineScope
    protected open fun start() {
        torrentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    protected open fun stop() {
        torrentScope.cancel()
    }

    protected abstract fun applyPreference(infoHash: String?)

    protected abstract fun findHandle(sha1: Sha1Hash): TorrentHandle?

    protected abstract fun saveTorrentFileToCache(infoHash: String, data: ByteArray)

    override suspend fun getAllManagedTorrents(): List<String> {
        return managedTorrents.keys.toList()
    }

    protected abstract fun onAlertAddTorrentAlert(addTorrentAlert: AddTorrentAlert)


    protected abstract fun onAlertMetaDataReceived(metadataReceivedAlert: MetadataReceivedAlert)

    /**
     * alert is sent by libTorrent when an event happens
     *
     * @param p0
     */
    override fun alert(p0: Alert<*>?) {
        // Bad practice right there
        GlobalScope.launch(Dispatchers.IO) {

            when (p0?.type()) {
                AlertType.ADD_TORRENT -> {
                    onAlertAddTorrentAlert(p0 as AddTorrentAlert)
                }
                AlertType.METADATA_RECEIVED -> {
                    onAlertMetaDataReceived(p0 as MetadataReceivedAlert)
                }
                else -> {
                }
            }
        }
    }

    /**
     * filter the events which a libTorrent have to send
     * return null to not filter anything
     *
     * @return
     */
    override fun types(): IntArray? {
        return intArrayOf(AlertType.ADD_TORRENT.swig(), AlertType.METADATA_RECEIVED.swig())
    }


}
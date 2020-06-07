package app.shynline.torient.domain.torrentmanager.service

import com.frostwire.jlibtorrent.SessionStats
import com.frostwire.jlibtorrent.TorrentHandle
import com.frostwire.jlibtorrent.TorrentInfo
import com.frostwire.jlibtorrent.TorrentStatus
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert

interface SessionController {
    interface SessionControllerInterface {
        fun periodic()
        fun onAlertAddTorrentAlert(addTorrentAlert: AddTorrentAlert)
        fun onAlertMetaDataReceived(metadataReceivedAlert: MetadataReceivedAlert)
        fun onStart()
        fun onStop()
    }

    data class PreferenceParams(val maxConnection: Int)

    fun setInterface(sessionControllerInterface: SessionControllerInterface)
    fun stats(): SessionStats
    fun fetchMagnet(magnet: String, timeout: Int): ByteArray?
    fun addTorrent(infoHash: String, magnet: String, torrentInfo: TorrentInfo? = null): Boolean
    fun removeTorrent(infoHash: String): Boolean
    fun applyPreference(preferenceParams: PreferenceParams)
    fun setManageTorrentState(infoHash: String, state: TorrentStatus.State?)
    fun getManageTorrentState(infoHash: String): TorrentStatus.State?
    fun getAllManagedTorrent(): List<String>
    fun findHandle(infoHash: String): TorrentHandle?
}
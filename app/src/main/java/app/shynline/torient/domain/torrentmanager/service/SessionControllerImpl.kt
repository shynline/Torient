package app.shynline.torient.domain.torrentmanager.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import app.shynline.torient.Config
import app.shynline.torient.common.logTorrent
import app.shynline.torient.utils.downloadDir
import com.frostwire.jlibtorrent.*
import com.frostwire.jlibtorrent.alerts.AddTorrentAlert
import com.frostwire.jlibtorrent.alerts.Alert
import com.frostwire.jlibtorrent.alerts.AlertType
import com.frostwire.jlibtorrent.alerts.MetadataReceivedAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer

data class TorrentActivityStats(var lastCheck: Date, var lastActivity: Date, var lastReset: Date) {
    companion object {
        fun default() = TorrentActivityStats(Date(), Date(), Date(0))
    }
}

class SessionControllerImpl(
    private val context: Context
) : ActivityCycle, ServiceConnection, AlertListener, SessionController {


    private var sessionControllerInterface: SessionController.SessionControllerInterface? = null
    private var periodicTimer: Timer? = null
    private var isActivityRunning = false
    private val managedTorrents: MutableMap<String, TorrentStatus.State?> = hashMapOf()
    private val magnets: MutableMap<String, String> = hashMapOf()
    private val torrentActivities: MutableMap<String, TorrentActivityStats> = hashMapOf()
    private val sessionParams: SessionParams = SessionParams(
        SettingsPack()
            .enableDht(true)
            .activeDownloads(5)
            .activeSeeds(5)
            .connectionsLimit(50)
    )
    private var service: TorientService? = null
    private val intent: Intent = Intent(context, TorientService::class.java)
    private val session: SessionManager = SessionManager(false)

    // This is just a helper var to create 15 seconds
    private var counter15 = 0

    override fun onActivityStart() {
        isActivityRunning = true
        handleServiceState()
    }

    override fun onActivityStop() {
        isActivityRunning = false
        handleServiceState()
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        this.service = (service as? TorientService.TorientBinder)?.service
        handleServiceState()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
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

    private fun handleServiceState() = GlobalScope.launch {
        if (isActivityRunning) {
            if (service != null) {
                service!!.background()
            } else {
                if (!session.isRunning) {
                    start()
                }
            }
        } else {
            if (managedTorrents.isEmpty()) {
                stop()
                unbindService()
                context.stopService(intent)
            } else {
                if (service != null) {
                    service!!.foreground(
                        managedTorrents.size,
                        session.downloadRate(),
                        session.uploadRate()
                    )
                } else {
                    bindService()
                    context.startService(intent)
                }
            }
        }
    }

    private fun start() {
        sessionControllerInterface?.onStart()
        session.addListener(this)
        session.start(sessionParams)
        periodicTimer = fixedRateTimer(
            name = "periodicTaskTorrentsList",
            initialDelay = 1000,
            period = 1000
        ) {
            sessionControllerInterface?.periodic()
            updateTorrentActivity()
            // Run every 15 seconds
            if (counter15 >= 15) {
                checkTorrentsActivityAndRestartIfNecessary()
            }
            counter15 += 1
            if (!isActivityRunning) {
                service?.updateNotification(
                    managedTorrents.size,
                    session.stats().downloadRate(),
                    session.stats().uploadRate()
                )
            }
        }
    }

    private fun updateTorrentActivity() {
        managedTorrents.keys.forEach { infoHash ->
            findHandle(infoHash)?.let { handle ->
                if (handle.isValid) {
                    val status = handle.status()
                    val rate = status.downloadRate() + status.uploadRate()
                    if (rate >= Config.TORRENT_ACTIVE_RATE_THRESHOLD) {
                        torrentActivities[infoHash]!!.lastActivity = Date()
                    }
                }
            }
        }
    }

    private fun checkTorrentsActivityAndRestartIfNecessary() {
        // There is an issue with Jlibtorrent in which torrents go to idle mode in some situations
        // This will restart them individually
        // Exclusively those are either downloading or downloading meta data
        managedTorrents.filter {
            it.value in listOf(
                TorrentStatus.State.DOWNLOADING,
                TorrentStatus.State.DOWNLOADING_METADATA
            )
        }.keys.forEach { infoHash ->
            findHandle(infoHash)?.let { handle ->
                if (handle.isValid) {
                    val status = handle.status()
                    val rate = status.downloadRate() + status.uploadRate()
                    val now = Date()
                    torrentActivities[infoHash]!!.lastCheck = now
                    // If the rate is below threshold it might need to restart
                    if (rate < Config.TORRENT_ACTIVE_RATE_THRESHOLD) {
                        // Check if inactivity time is more than a configured minimum
                        if (now.time - torrentActivities[infoHash]!!.lastActivity.time > Config.TORRENT_MIN_INACTIVITY) {
                            // Reset only if it haven't been restarted for more than a certain
                            // amount configured in Config file
                            if (now.time - torrentActivities[infoHash]!!.lastReset.time > Config.TORRENT_RESET_BACKOFF_PERIOD) {
                                // Saving magnet and torrent info if exists
                                // to be able to add the torrent again
                                val magnet = magnets[infoHash]!!
                                var torrentInfo: TorrentInfo? = null
                                handle.torrentFile()?.let {
                                    if (it.isValid) {
                                        torrentInfo = it
                                    }
                                }
                                // Save the last activity date
                                // Because it will be cleared in remove process
                                val lastActive = torrentActivities[infoHash]!!.lastActivity
                                removeTorrent(infoHash)
                                addTorrent(infoHash, magnet, torrentInfo)
                                // Update the torrent activity
                                torrentActivities[infoHash]?.apply {
                                    lastReset = now
                                    lastCheck = now
                                    lastActivity = lastActive
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun stop() {
        sessionControllerInterface?.onStop()
        periodicTimer?.cancel()
        session.stop() //blocking
        session.removeListener(this)
    }

    override fun setManageTorrentState(infoHash: String, state: TorrentStatus.State?) {
        managedTorrents[infoHash] = state
    }

    override fun getManageTorrentState(infoHash: String): TorrentStatus.State? {
        return managedTorrents[infoHash]
    }

    override fun getAllManagedTorrent() = managedTorrents.keys.toList()


    override fun findHandle(infoHash: String): TorrentHandle? {
        return session.find(Sha1Hash(infoHash))
    }

    override fun stats(): SessionStats = session.stats()

    override fun fetchMagnet(magnet: String, timeout: Int): ByteArray? =
        session.fetchMagnet(magnet, timeout)


    override fun addTorrent(infoHash: String, magnet: String, torrentInfo: TorrentInfo?): Boolean {
        // Return if the torrent is already being managed by session
        if (managedTorrents.containsKey(infoHash))
            return false
        managedTorrents[infoHash] = null
        magnets[infoHash] = magnet
        torrentActivities[infoHash] = TorrentActivityStats.default()
        if (torrentInfo != null) {
            logTorrent("add to session by torrent", infoHash)
            session.download(torrentInfo, context.downloadDir)
            return true
        }
        logTorrent("add to session by magnet!", infoHash)
        session.download(magnet, context.downloadDir)
        return true
    }

    override fun removeTorrent(infoHash: String): Boolean {
        managedTorrents.remove(infoHash)
        magnets.remove(infoHash)
        torrentActivities.remove(infoHash)
        findHandle(infoHash)?.let {
            session.remove(it)
            return true
        }
        return false
    }

    override fun setInterface(sessionControllerInterface: SessionController.SessionControllerInterface) {
        this.sessionControllerInterface = sessionControllerInterface
    }

    override fun applyPreference(preferenceParams: SessionController.PreferenceParams) {
        session.maxConnections(preferenceParams.maxConnection)
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
                    sessionControllerInterface?.onAlertAddTorrentAlert(p0 as AddTorrentAlert)
                }
                AlertType.METADATA_RECEIVED -> {
                    sessionControllerInterface?.onAlertMetaDataReceived(p0 as MetadataReceivedAlert)
                }
                else -> {
                }
            }
        }
    }
}
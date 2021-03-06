package app.shynline.torient.domain.torrentmanager.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import app.shynline.torient.R
import app.shynline.torient.utils.toStandardRate

class TorientService : Service() {
    class TorientBinder(val service: TorientService) : Binder()

    private var isForeground = false
    private val pendingIntent: PendingIntent by lazy {
        NavDeepLinkBuilder(this)
            .setGraph(R.navigation.torient)
            .setDestination(R.id.torrent_list_fragment)
            .createPendingIntent()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return TorientBinder(this)
    }

    fun foreground(active: Int, downloadRate: Long, uploadRate: Long) {
        if (isForeground)
            return
        val notification = showNotification(active, downloadRate, uploadRate, false)
        startForeground(ACTIVE_TORRENT_NOTIFICATION_ID, notification)
        isForeground = true
    }

    fun updateNotification(active: Int, downloadRate: Long, uploadRate: Long) {
        if (!isForeground)
            return
        showNotification(active, downloadRate, uploadRate, true)
    }

    private fun showNotification(
        active: Int,
        downloadRate: Long,
        uploadRate: Long,
        show: Boolean
    ): Notification {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("$active active torrent${if (active > 1) "s" else ""}")
            .setContentText(
                "⬇️: ${downloadRate.toStandardRate()}   " +
                        "\n⬆: ${uploadRate.toStandardRate()}"
            )
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        if (show) {
            with(NotificationManagerCompat.from(this)) {
                notify(ACTIVE_TORRENT_NOTIFICATION_ID, notification)
            }
        }
        return notification
    }


    fun background() {
        if (!isForeground)
            return
        isForeground = false
        stopForeground(true)
    }

    companion object {
        private const val CHANNEL_ID = "torient.torrent"
        private const val ACTIVE_TORRENT_NOTIFICATION_ID = 999
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.torrent_channel_name)
                val description = context.getString(R.string.torrent_channel_description)
                val importance = NotificationManager.IMPORTANCE_LOW
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    this.description = description
                }
                val notificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}
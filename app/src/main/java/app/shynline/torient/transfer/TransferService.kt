package app.shynline.torient.transfer

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import app.shynline.torient.R
import app.shynline.torient.common.downloadDir
import app.shynline.torient.utils.FileMimeDetector
import app.shynline.torient.utils.calculateTotalSize
import app.shynline.torient.utils.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.*


private const val EXTRA_SRC = "app.shynline.torient.transfer.extra.src"

class TransferService : Service() {


    private val pendingIntent: PendingIntent by lazy {
        NavDeepLinkBuilder(this)
            .setGraph(R.navigation.torient)
            .setDestination(R.id.torrent_list_fragment)
            .createPendingIntent()
    }

    private var name = ""
    private var size = 0L
    private var overallProgress = 0L
    private var isTransferring = false
    private val files: ArrayDeque<String> = ArrayDeque()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Checking if there is an intent with required extra string
        val src = intent?.getStringExtra(EXTRA_SRC)
        if (src == null) {
            // If this service is not transferring anything it should be stopped
            if (!isTransferring) {
                stopSelf()
            }
            return super.onStartCommand(intent, flags, startId)
        }
        // Push the extra string which probably is a file uri to files deque
        files.push(src)

        // If service is not actively transferring anything activate it
        if (!isTransferring) {
            copyNextOrStop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /***
     * This function get strings one by one from the deque and perform a copy action
     * If it find a null which represent an empty deque it stop transferring and
     * shut down the service
     */
    private fun copyNextOrStop() {
        // Make sure isTransferring flag is on
        isTransferring = true
        // Poll a string from deque
        val src = files.poll()
        if (src == null) {
            // Empty deque / clear the flag and stop the service
            isTransferring = false
            stopSelf()
            return
        }
        GlobalScope.launch(Dispatchers.IO) {
            name = src
            val srcFile = File(downloadDir, src)
            // Trying to open the file and see if it exists or not
            if (!srcFile.exists()) {
                // If the file doesn't exist clean up everything
                // and recall this function again to poll next
                cleanUp()
                copyNextOrStop()
                return@launch
            }
            // Calculate the total size for calculating the progress in notification
            size = srcFile.calculateTotalSize()
            overallProgress = 0L

            // bring the service in foreground with overallProgress = 0
            foreground()

            // Copy the file(s)
            cp(srcFile)

            showFileCopiedNotification()
            // cleaning up includes stopping foreground
            cleanUp()
            // Poll next
            copyNextOrStop()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun cp(file: File, path: String = "") {
        // If this file is a directory we recall this function recursively for each file inside
        if (file.isDirectory) {
            file.listFiles()?.forEach {
                cp(it, path + File.separator + file.name)
            }
            return
        }
        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.DownloadColumns.DISPLAY_NAME, file.name)
            put(MediaStore.DownloadColumns.IS_PENDING, 1)
            put(MediaStore.DownloadColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + path)
        }
        FileMimeDetector.getType(file.name)?.let {
            contentValues.put(MediaStore.DownloadColumns.MIME_TYPE, it)
        }

        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val item = resolver.insert(collection, contentValues)!!
        try {
            resolver.openOutputStream(item)!!.use { os ->
                file.inputStream().use { fis ->
                    fis.copyTo(os) { delta, progress ->
                        overallProgress += delta
                        showProgressNotification(true)
                    }
                }
            }
            contentValues.clear()
            contentValues.put(MediaStore.DownloadColumns.IS_PENDING, 0)
            resolver.update(item, contentValues, null, null)
        } catch (exception: IOException) {
            resolver.delete(item, null, null)
            showFileNotCopiedNotification()
        }
    }

    private fun cleanUp() {
        name = ""
        size = 0L
        overallProgress = 0L
        background()
    }

    private fun foreground() {
        val notification = showProgressNotification(false)
        startForeground(ACTIVE_TRANSFER_NOTIFICATION_ID, notification)
    }

    private fun showFileCopiedNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Save complete")
            .setContentIntent(pendingIntent)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText("$name has been saved successfully")
            )
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        with(NotificationManagerCompat.from(this)) {
            notify(NotificationID.getID(), notification)
        }

    }

    private fun showFileNotCopiedNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Save failed")
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$name couldn't be saved"))
            .setSmallIcon(R.drawable.ic_notification)
            .build()
        with(NotificationManagerCompat.from(this)) {
            notify(NotificationID.getID(), notification)
        }

    }

    private fun showProgressNotification(
        show: Boolean
    ): Notification {
        val progress = (overallProgress.toFloat() * 100 / size).toInt()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Saving $name")
            .setContentText(
                "progress: $progress%"
            )
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_notification)
            .setProgress(100, progress, false)
            .build()
        if (show) {
            with(NotificationManagerCompat.from(this)) {
                notify(ACTIVE_TRANSFER_NOTIFICATION_ID, notification)
            }
        }
        return notification
    }

    private fun background() {
        stopForeground(true)
    }


    companion object {

        @JvmStatic
        fun copyFile(context: Context, src: String) {
            val intent = Intent(context, TransferService::class.java).apply {
                putExtra(EXTRA_SRC, src)
            }
            context.startService(intent)
        }

        private const val CHANNEL_ID = "torient.transfer"
        private const val ACTIVE_TRANSFER_NOTIFICATION_ID = 998
        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.transfer_channel_name)
                val description = context.getString(R.string.transfer_channel_description)
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

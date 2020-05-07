package app.shynline.torient.transfer

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.FileUtils
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import app.shynline.torient.R
import app.shynline.torient.common.MainActivity
import app.shynline.torient.common.downloadDir
import app.shynline.torient.utils.FileMimeDetector
import java.io.File


private const val EXTRA_SRC = "app.shynline.torient.transfer.extra.src"

class TransferService : IntentService("TransferService"), FileUtils.ProgressListener {


    private val pendingIntent: PendingIntent by lazy {
        Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
    }

    override fun onProgress(progress: Long) {
        if (!isTransferring)
            return
        showProgressNotification(progress, true)
    }

    private var isTransferring = false
    private var name = ""
    private var size = 0L

    override fun onHandleIntent(intent: Intent?) {
        if (intent == null)
            return
        val src = intent.getStringExtra(EXTRA_SRC) ?: return
        isTransferring = true
        name = src
        val srcFile = File(downloadDir, src)
        if (!srcFile.exists()) {
            cleanUp()
            return
        }
        size = srcFile.length()
        foreground()

        cp(srcFile)
        cleanUp()
    }

    private fun cp(file: File, path: String = "") {
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

        val item = resolver.insert(collection, contentValues)
        resolver.openFileDescriptor(item!!, "rw")?.use {
            FileUtils.copy(file.inputStream().fd, it.fileDescriptor, null, null, this)
            showFileCopiedNotification()
        }
        contentValues.clear()
        contentValues.put(MediaStore.DownloadColumns.IS_PENDING, 0)
        resolver.update(item, contentValues, null, null)
    }

    private fun cleanUp() {
        name = ""
        size = 0L
        isTransferring = false
        background()
    }

    private fun foreground() {
        val notification = showProgressNotification(0, false)
        startForeground(ACTIVE_TRANSFER_NOTIFICATION_ID, notification)
    }

    private fun showFileCopiedNotification() {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Save complete")
            .setContentText("$name has been saved successfully")
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        with(NotificationManagerCompat.from(this)) {
            notify(NotificationID.getID(), notification)
        }

    }

    private fun showProgressNotification(
        progress: Long,
        show: Boolean
    ): Notification {
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Saving $name")
            .setContentText(
                "progress: ${(progress.toFloat() * 100 / size).toInt()}%"
            )
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.mipmap.ic_launcher_round)
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

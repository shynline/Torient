package app.shynline.torient.screens.torrentslist.items

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.screens.torrentslist.TorrentListViewMvcImpl
import app.shynline.torient.domain.torrentmanager.common.states.TorrentDownloadingState
import app.shynline.torient.utils.*
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

class TorrentItem(
    val torrentModel: TorrentModel,
    private val subscription: Subscription
) : AbstractItem<TorrentItem.ViewHolder>() {


    interface Subscription {
        fun subscribe(infoHash: String, listener: TorrentListViewMvcImpl.TorrentItemUpdateListener)
        fun unsubscribe(infoHash: String)
    }

    override val layoutRes: Int
        get() = R.layout.item_torrent

    override val type: Int
        get() = R.id.fastadapter_torrent_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v, subscription)
    }

    override var identifier: Long
        get() = torrentModel.hexHash
        set(@Suppress("UNUSED_PARAMETER") value) {
            throw RuntimeException("It's not modifiable.")
        }

    class ViewHolder(view: View, private val subscription: Subscription) :
        FastAdapter.ViewHolder<TorrentItem>(view),
        TorrentListViewMvcImpl.TorrentItemUpdateListener {
        private val nameTV = view.findViewById<TextView>(R.id.name)
        private val priorityBar = view.findViewById<View>(R.id.bar)
        private val iconIv = view.findViewById<ImageView>(R.id.icon)
        val option: ImageView = view.findViewById(R.id.option)
        private val progressView: MaterialProgressBar = view.findViewById(R.id.progressView)
        private val statusTv = view.findViewById<TextView>(R.id.status)
        private val statusProgressTv = view.findViewById<TextView>(R.id.statusProgress)

        private var latestItem: TorrentItem? = null

        override fun onUpdate() {
            if (latestItem == null) {
                return
            }
            bindView(latestItem!!, listOf())
        }

        override fun bindView(item: TorrentItem, payloads: List<Any>) {
            subscription.subscribe(item.torrentModel.infoHash, this)

            latestItem = item
            nameTV.text = item.torrentModel.name
            priorityBar.setBackgroundColor(Color.RED)
            iconIv.setImageResource(
                FileIcon.iconOf(
                    item.torrentModel.torrentFile?.fileType ?: FileType.UNKNOWN
                )
            )
            val bytesDone: Float
            val currentProgress = if (item.torrentModel.finished) {
                bytesDone = item.torrentModel.selectedFilesSize.toFloat()
                100
            } else {
                bytesDone = item.torrentModel.selectedFilesBytesDone
                (bytesDone * 100 / item.torrentModel.selectedFilesSize).toInt()
            }
            val progressText = "${bytesDone.toLong().toByteRepresentation()} of " +
                    "${item.torrentModel.selectedFilesSize.toByteRepresentation()} " +
                    "($currentProgress%)"
            when (item.torrentModel.userState) {
                TorrentUserState.PAUSED -> {
                    statusTv.text = "Paused"
                    progressView.isIndeterminate = false
                    progressView.progress = currentProgress
                    statusProgressTv.text = progressText
                }
                TorrentUserState.ACTIVE -> {
                    when (item.torrentModel.downloadingState) {
                        TorrentDownloadingState.UNKNOWN -> {
                            progressView.isIndeterminate = true
                            statusTv.text = "Finding peers..."
                            statusProgressTv.text = "Preparing"
                        }
                        TorrentDownloadingState.ALLOCATING -> {
                            progressView.isIndeterminate = true
                            statusTv.text = "Allocating files..."
                            statusProgressTv.text = "Preparing"
                        }
                        TorrentDownloadingState.CHECKING_FILES -> {
                            progressView.isIndeterminate = false
                            statusTv.text = "Checking files..."
                            statusProgressTv.text = "Preparing"
                            progressView.progress =
                                (item.torrentModel.progress * 100).toInt()
                        }
                        TorrentDownloadingState.CHECKING_RESUME_DATA -> {
                            progressView.isIndeterminate = true
                            statusTv.text = "Loading torrent state..."
                            statusProgressTv.text = "Preparing"
                        }
                        TorrentDownloadingState.DOWNLOADING -> {
                            progressView.isIndeterminate = false
                            progressView.progress = currentProgress
                            val downloadRate = item.torrentModel.downloadRate
                            val remainingBytes =
                                (item.torrentModel.selectedFilesSize * (1f - item.torrentModel.progress)).toLong()
                            val remainingTime = if (downloadRate == 0) {
                                null
                            } else {
                                remainingBytes / downloadRate
                            }
                            statusTv.text = "Downloading \uD83D\uDC64 " +
                                    "${item.torrentModel.connectedPeers}/" +
                                    "${item.torrentModel.maxPeers} - ⬇️ " +
                                    "${downloadRate.toStandardRate()} " +
                                    "⬆️ ${item.torrentModel.uploadRate.toStandardRate()}"
                            statusProgressTv.text =
                                "$progressText ${remainingTime?.toReadableTime() ?: ""}"
                        }
                        TorrentDownloadingState.DOWNLOADING_METADATA -> {
                            progressView.isIndeterminate = true
                            statusTv.text = "Downloading metadata..."
                            statusProgressTv.text = "Preparing"
                        }
                        TorrentDownloadingState.FINISHED -> {
                            progressView.isIndeterminate = false
                            progressView.progress = currentProgress
                            statusTv.text = "Download finished"
                            statusProgressTv.text = progressText
                        }
                        TorrentDownloadingState.SEEDING -> {
                            progressView.isIndeterminate = false
                            statusProgressTv.text = progressText
                            progressView.progress = currentProgress
                            statusTv.text =
                                "Seeding \uD83D\uDC64 ${item.torrentModel.connectedPeers}" +
                                        "/${item.torrentModel.maxPeers} - ⬇️ " +
                                        "${item.torrentModel.downloadRate.toStandardRate()} " +
                                        "⬆️ ${item.torrentModel.uploadRate.toStandardRate()}"
                        }
                    }
                }
            }
        }


        override fun unbindView(item: TorrentItem) {
            latestItem = null
            subscription.unsubscribe(item.torrentModel.infoHash)
            nameTV.text = ""
        }
    }
}
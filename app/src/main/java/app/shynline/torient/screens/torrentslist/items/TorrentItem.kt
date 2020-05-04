package app.shynline.torient.screens.torrentslist.items

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.torrentslist.TorrentListViewMvcImpl
import app.shynline.torient.torrent.states.ManageState
import app.shynline.torient.torrent.states.TorrentDownloadingState
import app.shynline.torient.utils.FileIcon
import app.shynline.torient.utils.toStandardRate
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import me.zhanghai.android.materialprogressbar.MaterialProgressBar

class TorrentItem(
    val torrentDetail: TorrentDetail,
    val subscription: Subscription
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
        get() = torrentDetail.hexHash
        set(value) {
            throw RuntimeException("It's not modifiable.")
        }

    class ViewHolder(view: View, private val subscription: Subscription) :
        FastAdapter.ViewHolder<TorrentItem>(view),
        TorrentListViewMvcImpl.TorrentItemUpdateListener {
        private val nameTV = view.findViewById<TextView>(R.id.name)
        private val priorityBar = view.findViewById<View>(R.id.bar)
        private val iconIv = view.findViewById<ImageView>(R.id.icon)
        val handle: ImageView = view.findViewById(R.id.handle)
        private val progressView: MaterialProgressBar = view.findViewById(R.id.progressView)
        private val statusTv = view.findViewById<TextView>(R.id.status)

        private var latestItem: TorrentItem? = null

        override fun onUpdate() {
            if (latestItem == null) {
                return
            }
            bindView(latestItem!!, listOf())
        }

        override fun bindView(item: TorrentItem, payloads: List<Any>) {
            subscription.subscribe(item.torrentDetail.infoHash, this)

            latestItem = item
            nameTV.text = item.torrentDetail.name
            priorityBar.setBackgroundColor(Color.RED)
            iconIv.setImageResource(FileIcon.iconOf(item.torrentDetail.torrentFile.fileType))
            when (item.torrentDetail.userState) {
                TorrentUserState.PAUSED -> {
                    handle.setImageResource(R.drawable.icon_play)
                    statusTv.text = "Paused"
                    progressView.isIndeterminate = false
                    progressView.progress = if (item.torrentDetail.finished) {
                        100
                    } else {
                        (item.torrentDetail.progress * 100).toInt()
                    }
                    // update ui from database data
                }
                TorrentUserState.ACTIVE -> {
                    handle.setImageResource(R.drawable.icon_pause)
                    when (item.torrentDetail.serviceState) {
                        ManageState.UNKNOWN, ManageState.ADDED -> {
                            // Requested to add to service
                            // Progressbar should be indeterminate
                            statusTv.text = "Finding peers..."
                            progressView.isIndeterminate = true
                        }
                        ManageState.FINISHED -> {
                            // It's finished download now it's a seeder
                            // handle depends on user's state
                            progressView.isIndeterminate = false
                            progressView.progress = 100
//                            progressView.labelText = "100%"
                            statusTv.text = "Download finished."
                        }
                        ManageState.RESUMED -> {
                            when (item.torrentDetail.downloadingState) {
                                TorrentDownloadingState.UNKNOWN -> {
                                }
                                TorrentDownloadingState.ALLOCATING -> {
                                    progressView.isIndeterminate = false
                                    progressView.progress =
                                        (item.torrentDetail.progress * 100).toInt()
                                    statusTv.text = "Allocating space..."
//                                    progressView.labelText =
//                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                }
                                TorrentDownloadingState.CHECKING_FILES,
                                TorrentDownloadingState.CHECKING_RESUME_DATA -> {
                                    progressView.isIndeterminate = false
                                    statusTv.text = "Checking files..."
                                    progressView.progress =
                                        (item.torrentDetail.progress * 100).toInt()
//                                    progressView.labelText =
//                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                }
                                TorrentDownloadingState.DOWNLOADING -> {
                                    progressView.isIndeterminate = false
                                    statusTv.text = "Checking files..."
                                    progressView.progress =
                                        (item.torrentDetail.progress * 100).toInt()
//                                    progressView.labelText =
//                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                    statusTv.text = "Downloading P 0/0 - D " +
                                            "${item.torrentDetail.downloadRate.toStandardRate()} " +
                                            "U ${item.torrentDetail.uploadRate.toStandardRate()}"
                                }
                                TorrentDownloadingState.DOWNLOADING_METADATA -> {
                                    progressView.isIndeterminate = false
                                    statusTv.text = "Downloading metadata..."
                                    progressView.progress =
                                        (item.torrentDetail.progress * 100).toInt()
//                                    progressView.labelText =
//                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                }
                                TorrentDownloadingState.FINISHED -> {
                                    progressView.isIndeterminate = false
                                    progressView.progress = 100
//                                    progressView.labelText = "100%"
                                    statusTv.text = "Download finished."
                                }
                                TorrentDownloadingState.SEEDING -> {
                                    progressView.isIndeterminate = false
                                    statusTv.text = "Seeding to 0 P ..."
                                    statusTv.text = "Seeding P 0/0 - D " +
                                            "${item.torrentDetail.downloadRate.toStandardRate()} " +
                                            "U ${item.torrentDetail.uploadRate.toStandardRate()}"
                                }
                            }
                        }
                    }
                }
            }
        }


        override fun unbindView(item: TorrentItem) {
            latestItem = null
            subscription.unsubscribe(item.torrentDetail.infoHash)
            nameTV.text = ""
        }
    }
}
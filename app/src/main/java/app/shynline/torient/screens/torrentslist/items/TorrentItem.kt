package app.shynline.torient.screens.torrentslist.items

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.database.TorrentUserState
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.torrent.torrent.ManageState
import app.shynline.torient.torrent.torrent.TorrentDownloadingState
import app.shynline.torient.utils.FileIcon
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem
import com.skydoves.progressview.ProgressView

class TorrentItem(val torrentDetail: TorrentDetail) :
    AbstractItem<TorrentItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.item_torrent

    override val type: Int
        get() = R.id.fastadapter_torrent_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override var identifier: Long
        get() = torrentDetail.hexHash
        set(value) {
            throw RuntimeException("It's not modifiable.")
        }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<TorrentItem>(view) {
        private val nameTV = view.findViewById<TextView>(R.id.name)
        private val priorityBar = view.findViewById<View>(R.id.bar)
        private val iconIv = view.findViewById<ImageView>(R.id.icon)
        val handle: ImageView = view.findViewById(R.id.handle)
        private val progressView: ProgressView = view.findViewById(R.id.progressView)
        private val statusTv = view.findViewById<TextView>(R.id.status)

        override fun bindView(item: TorrentItem, payloads: List<Any>) {
            nameTV.text = item.torrentDetail.name
            priorityBar.setBackgroundColor(Color.RED)
            iconIv.setImageResource(FileIcon.iconOf(item.torrentDetail.torrentFile.fileType))
            when (item.torrentDetail.userState) {
                TorrentUserState.PAUSED -> {
                    handle.setImageResource(R.drawable.icon_play)
                    statusTv.text = "Paused"
                    // update ui from database data
                }
                TorrentUserState.ACTIVE -> {
                    handle.setImageResource(R.drawable.icon_pause)
                    when (item.torrentDetail.serviceState) {
                        ManageState.UNKNOWN, ManageState.ADDED -> {
                            // Requested to add to service
                            // Progressbar should be indeterminate
                            statusTv.text = "Finding peers..."
                        }
                        ManageState.FINISHED -> {
                            // It's finished download now it's a seeder
                            // handle depends on user's state
                            progressView.progress = 100f
                            progressView.labelText = "100%"
                            statusTv.text = "Download finished."
                        }
                        ManageState.RESUMED -> {
                            when (item.torrentDetail.downloadingState) {
                                TorrentDownloadingState.UNKNOWN -> {
                                }
                                TorrentDownloadingState.ALLOCATING -> {
                                    progressView.progress = item.torrentDetail.progress * 100
                                    statusTv.text = "Allocating space..."
                                    progressView.labelText =
                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                }
                                TorrentDownloadingState.CHECKING_FILES,
                                TorrentDownloadingState.CHECKING_RESUME_DATA -> {
                                    statusTv.text = "Checking files..."
                                    progressView.progress = item.torrentDetail.progress * 100
                                    progressView.labelText =
                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                }
                                TorrentDownloadingState.DOWNLOADING -> {
                                    statusTv.text = "Checking files..."
                                    progressView.progress = item.torrentDetail.progress * 100
                                    progressView.labelText =
                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                    val dr =
                                        String.format(
                                            "%.2f",
                                            item.torrentDetail.downloadRate / (1024f)
                                        )
                                    val ur =
                                        String.format(
                                            "%.2f",
                                            item.torrentDetail.uploadRate / (1024f)
                                        )
                                    statusTv.text = "Downloading P 0/0 - D $dr KB/s U $ur KB/s"
                                }
                                TorrentDownloadingState.DOWNLOADING_METADATA -> {
                                    statusTv.text = "Downloading metadata..."
                                    progressView.progress = item.torrentDetail.progress * 100
                                    progressView.labelText =
                                        (item.torrentDetail.progress * 100f).toString() + "%"
                                }
                                TorrentDownloadingState.FINISHED -> {
                                    progressView.progress = 100f
                                    progressView.labelText = "100%"
                                    statusTv.text = "Download finished."
                                }
                                TorrentDownloadingState.SEEDING -> {
                                    statusTv.text = "Seeding to 0 P ..."
                                    val dr =
                                        String.format(
                                            "%.2f",
                                            item.torrentDetail.downloadRate / (1024f)
                                        )
                                    val ur =
                                        String.format(
                                            "%.2f",
                                            item.torrentDetail.uploadRate / (1024f)
                                        )
                                    statusTv.text = "Seeding P 0/0 - D $dr KB/s U $ur KB/s"
                                }
                            }
                        }
                    }
                }
            }
        }


        override fun unbindView(item: TorrentItem) {
            nameTV.text = ""
        }
    }
}
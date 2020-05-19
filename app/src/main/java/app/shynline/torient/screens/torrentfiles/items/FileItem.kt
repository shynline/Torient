package app.shynline.torient.screens.torrentfiles.items

import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.model.FilePriority
import app.shynline.torient.model.TorrentFile
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.screens.torrentfiles.TorrentFilesViewMvcImpl
import app.shynline.torient.utils.FileIcon
import app.shynline.torient.utils.MetricsUtil
import app.shynline.torient.utils.toByteRepresentation
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IParentItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.items.AbstractItem

class FileItem(
    private val torrentFile: TorrentFile,
    private val subscription: Subscription
) : AbstractItem<FileItem.ViewHolder>(),
    ISubItem<FileItem.ViewHolder> {

    interface Subscription {
        fun subscribe(index: Int, listener: TorrentFilesViewMvcImpl.TorrentFileUpdateListener)
        fun unsubscribe(index: Int)
    }

    var level = -1

    override val layoutRes: Int
        get() = R.layout.torrent_files_item_file

    override val type: Int
        get() = R.id.fastadapter_file_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(
            v, subscription
        )
    }

    override var parent: IParentItem<*>? = null

    class ViewHolder(view: View, private val subscription: Subscription) :
        FastAdapter.ViewHolder<FileItem>(view),
        TorrentFilesViewMvcImpl.TorrentFileUpdateListener {
        private val nameTV: TextView = view.findViewById(R.id.name)
        private val paddingView: View = view.findViewById(R.id.padding)
        private val iconIV: ImageView = view.findViewById(R.id.icon)
        private val layoutParams: ViewGroup.LayoutParams
        private val haveTv: TextView = view.findViewById(R.id.have)
        private val downloadCb: CheckBox = view.findViewById(R.id.download)
        private val priorityTv: TextView = view.findViewById(R.id.priority)
        private val sizeTv: TextView = view.findViewById(R.id.size)
        private var item: FileItem? = null

        init {
            layoutParams = paddingView.layoutParams
        }

        override fun onUpdatePriority(torrentFilePriority: TorrentFilePriority) {
            downloadCb.isChecked = torrentFilePriority.active
            priorityTv.text = when (torrentFilePriority.priority) {
                FilePriority.NORMAL -> "Normal"
                FilePriority.HIGH -> "High"
                FilePriority.LOW -> "Low"
                FilePriority.MIXED -> {
                    throw IllegalStateException("Files can not have mixed priority.")
                }
            }
        }

        override fun onUpdateProgress(fileProgress: Long) {
            // Item is not null here
            haveTv.text = "${(fileProgress * 100f / item!!.torrentFile.size).toInt()}%"
        }

        override fun bindView(item: FileItem, payloads: List<Any>) {
            // Make sure to assign item before subscribe
            this.item = item
            subscription.subscribe(item.torrentFile.index, this)
            sizeTv.text = item.torrentFile.size.toByteRepresentation()
            nameTV.text = item.torrentFile.name
            iconIV.setImageResource(FileIcon.iconOf(item.torrentFile.fileType))
            layoutParams.width =
                MetricsUtil.convertDpToPixel(8f * item.level, paddingView.context).toInt() + 1
        }

        override fun unbindView(item: FileItem) {
            subscription.unsubscribe(item.torrentFile.index)
            this.item = null
            nameTV.text = ""
        }

    }
}
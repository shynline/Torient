package app.shynline.torient.screens.newtorrent.items

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.domain.models.TorrentFile
import app.shynline.torient.utils.FileIcon
import app.shynline.torient.utils.MetricsUtil
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IParentItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.items.AbstractItem

class FileItem(private val torrentFile: TorrentFile) : AbstractItem<FileItem.ViewHolder>(),
    ISubItem<FileItem.ViewHolder> {

    var level = -1

    override val layoutRes: Int
        get() = R.layout.new_torrent_item_file

    override val type: Int
        get() = R.id.fastadapter_file_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(
            v
        )
    }

    override var parent: IParentItem<*>? = null

    class ViewHolder(view: View) :
        FastAdapter.ViewHolder<FileItem>(view) {
        private val nameTV: TextView = view.findViewById(R.id.name)
        private val paddingView: View = view.findViewById(R.id.padding)
        private val iconIV: ImageView = view.findViewById(R.id.icon)
        private val layoutParams: ViewGroup.LayoutParams

        init {
            layoutParams = paddingView.layoutParams
        }

        override fun bindView(item: FileItem, payloads: List<Any>) {
            nameTV.text = item.torrentFile.name
            iconIV.setImageResource(FileIcon.iconOf(item.torrentFile.fileType))
            layoutParams.width =
                MetricsUtil.convertDpToPixel(8f * item.level, paddingView.context).toInt() + 1
        }

        override fun unbindView(item: FileItem) {
            nameTV.text = ""
        }

    }
}
package app.shynline.torient.screens.torrentfiles.items

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.domain.models.TorrentFile
import app.shynline.torient.utils.FileIcon
import app.shynline.torient.utils.FileType
import app.shynline.torient.utils.MetricsUtil
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem

class FolderItem(private val torrentFile: TorrentFile) :
    AbstractExpandableItem<FolderItem.ViewHolder>() {
    override val layoutRes: Int
        get() = R.layout.torrent_files_item_folder
    override val type: Int
        get() = R.id.fastadapter_folder_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    var level = -1

    class ViewHolder(view: View) :
        FastAdapter.ViewHolder<FolderItem>(view) {
        private val nameTV: TextView = view.findViewById(R.id.name)
        private val paddingView: View = view.findViewById(R.id.padding)
        private val handleIV: ImageView = view.findViewById(R.id.handle)
        private val iconIV: ImageView = view.findViewById(R.id.icon)
        private val layoutParams: ViewGroup.LayoutParams

        init {
            handleIV.setImageResource(R.drawable.icon_triangle)
            iconIV.setImageResource(FileIcon.iconOf(FileType.DIR))
            layoutParams = paddingView.layoutParams
        }

        override fun bindView(item: FolderItem, payloads: List<Any>) {
            nameTV.text = item.torrentFile.name
            if (item.isExpanded) {
                handleIV.rotation = 0f
            } else {
                handleIV.rotation = -90f
            }
            layoutParams.width =
                MetricsUtil.convertDpToPixel(8f * item.level, paddingView.context).toInt() + 1
        }

        override fun unbindView(item: FolderItem) {
            nameTV.text = ""
        }
    }
}
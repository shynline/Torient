package app.shynline.torient.screens.newtorrent.items

import android.view.View
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.model.TorrentFile
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem

class FolderItem(private val torrentFile: TorrentFile) :
    AbstractExpandableItem<FolderItem.ViewHolder>() {
    override val layoutRes: Int
        get() = R.layout.item_folder
    override val type: Int
        get() = R.id.fastadapter_folder_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(
            v
        )
    }


    class ViewHolder(view: View) :
        FastAdapter.ViewHolder<FolderItem>(view) {
        private val nameTV: TextView = view.findViewById(R.id.name)

        override fun bindView(item: FolderItem, payloads: List<Any>) {
            nameTV.text = item.torrentFile.size.toString() + " \\/"
        }

        override fun unbindView(item: FolderItem) {
            nameTV.text = ""
        }
    }
}
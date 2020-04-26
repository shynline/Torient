package app.shynline.torient.screens.newtorrent.items

import android.view.View
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.model.TorrentFile
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IParentItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.items.AbstractItem

class FileItem(private val torrentFile: TorrentFile) : AbstractItem<FileItem.ViewHolder>(),
    ISubItem<FileItem.ViewHolder> {

    override val layoutRes: Int
        get() = R.layout.item_file

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

        override fun bindView(item: FileItem, payloads: List<Any>) {
            nameTV.text = item.torrentFile.size.toString()
        }

        override fun unbindView(item: FileItem) {
            nameTV.text = ""
        }

    }
}
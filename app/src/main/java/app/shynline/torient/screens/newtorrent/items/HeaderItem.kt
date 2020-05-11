package app.shynline.torient.screens.newtorrent.items

import android.view.View
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.model.TorrentModel
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class HeaderItem(
    private val torrentModel: TorrentModel
) : AbstractItem<HeaderItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.item_header
    override val type: Int
        get() = R.id.fastadapter_header_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(
            v,
            torrentModel
        )
    }

    class ViewHolder(view: View, private val torrentModel: TorrentModel) :
        FastAdapter.ViewHolder<HeaderItem>(view) {
        private val nameTV = view.findViewById<TextView>(R.id.name)
        private val creatorTV = view.findViewById<TextView>(R.id.creator)
        private val commentTV = view.findViewById<TextView>(R.id.comment)
        private val infoHashTV = view.findViewById<TextView>(R.id.infoHash)
        private val sizeTV = view.findViewById<TextView>(R.id.size)

        override fun bindView(item: HeaderItem, payloads: List<Any>) {
            nameTV.text = torrentModel.name
            creatorTV.text = torrentModel.author
            commentTV.text = torrentModel.comment
            infoHashTV.text = torrentModel.infoHash
            sizeTV.text = torrentModel.totalSize.toString()
        }

        override fun unbindView(item: HeaderItem) {
            nameTV.text = ""
            creatorTV.text = ""
            commentTV.text = ""
            infoHashTV.text = ""
            sizeTV.text = ""
        }
    }
}
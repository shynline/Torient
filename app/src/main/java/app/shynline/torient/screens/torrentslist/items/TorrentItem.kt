package app.shynline.torient.screens.torrentslist.items

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.database.TorrentState
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.utils.FileIcon
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class TorrentItem(val torrentDetail: TorrentDetail) :
    AbstractItem<TorrentItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.item_torrent

    override val type: Int
        get() = R.id.fastadapter_torrent_item_id

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<TorrentItem>(view) {
        private val nameTV = view.findViewById<TextView>(R.id.name)
        private val priorityBar = view.findViewById<View>(R.id.bar)
        private val iconIv = view.findViewById<ImageView>(R.id.icon)
        val handle = view.findViewById<ImageView>(R.id.handle)

        override fun bindView(item: TorrentItem, payloads: List<Any>) {
            nameTV.text = item.torrentDetail.name
            priorityBar.setBackgroundColor(Color.RED)
            iconIv.setImageResource(FileIcon.iconOf(item.torrentDetail.torrentFile.fileType))
            when (item.torrentDetail.state) {
                TorrentState.PAUSED -> handle.setImageResource(R.drawable.icon_play)
                TorrentState.FINISHED -> handle.setImageResource(R.drawable.icon_reset)
                TorrentState.ACTIVE -> handle.setImageResource(R.drawable.icon_pause)
            }
        }

        override fun unbindView(item: TorrentItem) {
            nameTV.text = ""
        }
    }
}
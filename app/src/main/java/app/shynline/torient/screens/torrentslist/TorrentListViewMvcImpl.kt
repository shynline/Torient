package app.shynline.torient.screens.torrentslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.shynline.torient.R
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.screens.torrentslist.items.TorrentItem
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.listeners.LongClickEventHook

class TorrentListViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentListViewMvc.Listener>(), TorrentListViewMvc,
    TorrentItem.Subscription {

    private val addTorrentFileBtn: FloatingActionButton
    private val torrentListRv: RecyclerView
    private val torrentAdapter: ItemAdapter<TorrentItem>
    private val fastAdapter: FastAdapter<TorrentItem>
    private val torrentItemUpdateListeners: MutableMap<String, TorrentItemUpdateListener> =
        mutableMapOf()

    override fun subscribe(infoHash: String, listener: TorrentItemUpdateListener) {
        torrentItemUpdateListeners[infoHash] = listener
    }

    override fun unsubscribe(infoHash: String) {
        torrentItemUpdateListeners.remove(infoHash)
    }

    interface TorrentItemUpdateListener {
        fun onUpdate()
    }

    init {
        setRootView(
            inflater.inflate(R.layout.fragment_torrent_list_view, parent, false)
        )
        addTorrentFileBtn = findViewById(R.id.addTorrentFile)
        torrentListRv = findViewById(R.id.torrentsList)

        torrentAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(torrentAdapter)
        torrentListRv.adapter = fastAdapter
        torrentListRv.layoutManager = LinearLayoutManager(getContext())

        addTorrentFileBtn.setOnClickListener {
            getListeners().forEach {
                it.addTorrentFile()
            }
        }

        fastAdapter.addEventHook(object : ClickEventHook<TorrentItem>() {
            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<TorrentItem>,
                item: TorrentItem
            ) {
                getListeners().forEach {
                    it.handleClicked(position, item.torrentDetail)
                }
            }

            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                if (viewHolder is TorrentItem.ViewHolder)
                    return viewHolder.handle
                return null
            }
        })

        fastAdapter.addEventHook(object : LongClickEventHook<TorrentItem>() {
            override fun onLongClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<TorrentItem>,
                item: TorrentItem
            ): Boolean {
                val menu = PopupMenu(getContext(), v)
                menu.inflate(R.menu.torrent_item_menu)
                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.torrent_copy -> {
                            getListeners().forEach { listener ->
                                listener.onCopyToDownloadRequested(item.torrentDetail)
                            }
                            return@setOnMenuItemClickListener true
                        }
                    }
                    false
                }
                menu.show()
                return true
            }

            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                if (viewHolder is TorrentItem.ViewHolder)
                    return viewHolder.itemView
                return null
            }
        })

    }

    override fun notifyItemUpdate(infoHash: String) {
        torrentItemUpdateListeners[infoHash]?.onUpdate()
    }

    override fun showTorrents(torrentDetails: List<TorrentDetail>) {
        // use diff util here
        torrentAdapter.clear()
        torrentAdapter.add(torrentDetails.map { torrentDetail ->
            TorrentItem(torrentDetail, this)
        })
    }

    override fun removeTorrent(identifier: Long) {
        fastAdapter.notifyAdapterItemRemoved(fastAdapter.getPosition(identifier))
    }

}
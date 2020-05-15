package app.shynline.torient.screens.torrentslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.shynline.torient.R
import app.shynline.torient.database.states.TorrentUserState
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.screens.torrentslist.items.TorrentItem
import com.cheekiat.fabmenu.FabMenu
import com.cheekiat.fabmenu.listener.OnItemClickListener
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import com.mikepenz.fastadapter.listeners.ClickEventHook

class TorrentListViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentListViewMvc.Listener>(), TorrentListViewMvc,
    TorrentItem.Subscription {

    private val fabMenuBtn: FabMenu
    private val torrentListRv: RecyclerView
    private val torrentAdapter: ItemAdapter<TorrentItem>
    private val fastAdapter: FastAdapter<TorrentItem>
    private val emptyPlaceholder: View
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
        fabMenuBtn = findViewById(R.id.fabMenu)
        torrentListRv = findViewById(R.id.torrentsList)
        emptyPlaceholder = findViewById(R.id.emptyPlaceHolder)
        emptyPlaceholder.visibility = View.GONE
        torrentAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(torrentAdapter)
        torrentListRv.adapter = fastAdapter
        torrentListRv.layoutManager = LinearLayoutManager(getContext())

        fabMenuBtn.addItem(R.drawable.icon_magnet, R.color.colorSecondary)
        fabMenuBtn.addItem(R.drawable.icon_add_file, R.color.colorSecondary)
        fabMenuBtn.setOnItemClickListener(object : OnItemClickListener {
            override fun onItemClick(position: Int) {
                if (position == 0) { // torrent file
                    getListeners().forEach {
                        it.addTorrentFile()
                    }
                } else { // magnet
                    getListeners().forEach {
                        it.addTorrentMagnet()
                    }
                }
            }
        })

        fastAdapter.addEventHook(object : ClickEventHook<TorrentItem>() {
            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<TorrentItem>,
                item: TorrentItem
            ) {
                val menu = PopupMenu(getContext(), v)
                menu.inflate(R.menu.torrent_item_menu)
                val cItem = menu.menu.findItem(R.id.torrent_control)
                cItem.title = if (item.torrentModel.userState == TorrentUserState.ACTIVE) {
                    "Pause"
                } else {
                    "Start"
                }
                menu.menu.findItem(R.id.torrent_save).isVisible = item.torrentModel.finished

                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.torrent_save -> {
                            getListeners().forEach { listener ->
                                listener.onSaveToDownloadRequested(item.torrentModel)
                            }
                            return@setOnMenuItemClickListener true
                        }
                        R.id.torrent_control -> {
                            getListeners().forEach { listener ->
                                listener.handleClicked(position, item.torrentModel)
                            }
                        }
                        R.id.torrent_remove -> {
                            getListeners().forEach { listener ->
                                listener.onRemoveTorrent(item.torrentModel)
                            }
                        }
                        R.id.torrent_copy_magnet -> {
                            getListeners().forEach { listener ->
                                listener.onCopyMagnetRequested(item.torrentModel)
                            }
                        }
                    }
                    false
                }
                menu.show()
            }

            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                if (viewHolder is TorrentItem.ViewHolder)
                    return viewHolder.option
                return null
            }
        })

        fastAdapter.addEventHook(object : ClickEventHook<TorrentItem>() {
            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<TorrentItem>,
                item: TorrentItem
            ) {
                getListeners().forEach { listener ->
                    listener.onTorrentClicked(item.torrentModel)
                }
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

    override fun showTorrents(torrentModels: List<TorrentModel>) {
        // use diff util here
        emptyPlaceholder.visibility = if (torrentModels.isEmpty()) {
            View.VISIBLE
        } else {
            View.GONE
        }
        FastAdapterDiffUtil[torrentAdapter] = torrentModels.map { torrentDetail ->
            TorrentItem(torrentDetail, this)
        }
    }


    override fun removeTorrent(identifier: Long) {
        torrentAdapter.removeByIdentifier(identifier)
        emptyPlaceholder.visibility = if (fastAdapter.itemCount == 0) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

}
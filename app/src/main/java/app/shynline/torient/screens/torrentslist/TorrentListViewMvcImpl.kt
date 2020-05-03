package app.shynline.torient.screens.torrentslist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

class TorrentListViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentListViewMvc.Listener>(), TorrentListViewMvc {

    private val addTorrentFileBtn: FloatingActionButton
    private val torrentListRv: RecyclerView
    private val torrentAdapter: ItemAdapter<TorrentItem>
    private val fastAdapter: FastAdapter<TorrentItem>

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
    }

    override fun notifyItemChange(position: Int) {
        fastAdapter.notifyAdapterItemChanged(position)
    }

    override fun showTorrents(torrentDetails: List<TorrentDetail>) {
        torrentAdapter.clear()
        torrentAdapter.add(torrentDetails.map { torrentDetail ->
            TorrentItem(torrentDetail)
        })
    }

    override fun notifyItemChangeIdentifier(identifier: Long) {
        fastAdapter.notifyAdapterItemChanged(fastAdapter.getPosition(identifier))
    }

    override fun removeTorrent(identifier: Long) {
        fastAdapter.notifyAdapterItemRemoved(fastAdapter.getPosition(identifier))
    }

}
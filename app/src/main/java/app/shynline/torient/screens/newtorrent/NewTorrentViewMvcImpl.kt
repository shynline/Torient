package app.shynline.torient.screens.newtorrent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.shynline.torient.R
import app.shynline.torient.model.TorrentDetail
import app.shynline.torient.model.TorrentFile
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.screens.newtorrent.items.FileItem
import app.shynline.torient.screens.newtorrent.items.FolderItem
import app.shynline.torient.screens.newtorrent.items.HeaderItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.adapters.GenericFastItemAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.expandable.getExpandableExtension

class NewTorrentViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<NewTorrentViewMvc.Listener>(),
    NewTorrentViewMvc {
    private val fileTreeRV: RecyclerView
    private val fastItemAdapter: GenericFastItemAdapter
    private val expandableExtension: ExpandableExtension<GenericItem>
    private val headerItemAdapter: ItemAdapter<HeaderItem>

    companion object {
        private var fileIdentifier = 0L
    }

    init {
        setRootView(
            inflater.inflate(R.layout.fragment_new_torrent, parent, false)
        )
        fileTreeRV = findViewById(R.id.recyclerView)
        fastItemAdapter = FastItemAdapter()
        headerItemAdapter = ItemAdapter()
        val adapter = FastAdapter.with(listOf(headerItemAdapter, fastItemAdapter))
        expandableExtension = adapter.getExpandableExtension()
        fileTreeRV.layoutManager = LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false)
        fileTreeRV.adapter = adapter
    }

    private fun parseTorrentFile(torrentFile: TorrentFile): ISubItem<*> {
        if (torrentFile.isFolder) {
            val folder = FolderItem(
                torrentFile
            ).apply { this.identifier = fileIdentifier++ }
            torrentFile.files!!.forEach {
                folder.subItems.add(parseTorrentFile(it))
            }
            return folder
        }
        return FileItem(torrentFile)
            .apply { this.identifier = fileIdentifier++ }

    }

    override fun showTorrent(torrentDetail: TorrentDetail) {
        headerItemAdapter.clear()
        headerItemAdapter.add(
            HeaderItem(
                torrentDetail
            )
        )
        fileIdentifier = 0L
        fastItemAdapter.add(parseTorrentFile(torrentDetail.torrentFile))
    }
}
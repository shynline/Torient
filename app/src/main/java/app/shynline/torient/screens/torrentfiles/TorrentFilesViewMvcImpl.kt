package app.shynline.torient.screens.torrentfiles

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.shynline.torient.R
import app.shynline.torient.model.TorrentFile
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.screens.torrentfiles.items.FileItem
import app.shynline.torient.screens.torrentfiles.items.FolderItem
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.adapters.GenericFastItemAdapter
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.expandable.getExpandableExtension

class TorrentFilesViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentFilesViewMvc.Listener>(), TorrentFilesViewMvc,
    FileItem.Subscription {

    private val recyclerView: RecyclerView
    private val fastItemAdapter: GenericFastItemAdapter
    private val expandableExtension: ExpandableExtension<GenericItem>
    private val torrentFileUpdateListeners: MutableMap<Int, TorrentFileUpdateListener> =
        mutableMapOf()
    private val queuedFileProgress: MutableMap<Int, Long> = mutableMapOf()
    private val queuedFilePriorities: MutableMap<Int, TorrentFilePriority> = mutableMapOf()

    interface TorrentFileUpdateListener {
        fun onUpdateProgress(fileProgress: Long)
        fun onUpdatePriority(torrentFilePriority: TorrentFilePriority)
    }


    companion object {
        private var fileIdentifier = 0L
    }

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_files, parent, false))
        recyclerView = findViewById(R.id.recyclerView)
        fastItemAdapter = FastItemAdapter()
        val adapter = FastAdapter.with(listOf(fastItemAdapter))
        expandableExtension = adapter.getExpandableExtension()
        recyclerView.layoutManager = LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

    }

    override fun subscribe(index: Int, listener: TorrentFileUpdateListener) {
        torrentFileUpdateListeners[index] = listener
        queuedFilePriorities[index]?.let {
            listener.onUpdatePriority(it)
        }
        queuedFileProgress[index]?.let {
            listener.onUpdateProgress(it)
        }
    }

    override fun unsubscribe(index: Int) {
        torrentFileUpdateListeners.remove(index)
    }

    private fun parseTorrentFile(
        torrentFile: TorrentFile, level: Int = 0
    ): ISubItem<*> {
        if (torrentFile.isFolder) {
            val folder = FolderItem(torrentFile).apply {
                this.identifier = fileIdentifier++
                this.level = level
            }
            torrentFile.files!!.forEach {
                folder.subItems.add(parseTorrentFile(it, level + 1))
            }
            return folder
        }
        return FileItem(torrentFile, this)
            .apply {
                this.identifier = fileIdentifier++
                this.level = level
            }

    }

    override fun updateFileProgress(fileProgress: List<Long>) {
        queuedFileProgress.clear()
        fileProgress.forEachIndexed { index, l ->
            val callback = torrentFileUpdateListeners[index]
            if (callback == null) {
                queuedFileProgress[index] = l
            } else {
                callback.onUpdateProgress(fileProgress[index])
            }
        }
    }

    override fun showTorrent(torrentModel: TorrentModel) {
        fileIdentifier = 0L
        val files = parseTorrentFile(torrentModel.torrentFile!!)
        fastItemAdapter.add(files)
    }

    override fun updateFilePriority(torrentFilePriorities: List<TorrentFilePriority>) {
        queuedFilePriorities.clear()
        torrentFilePriorities.forEachIndexed { index, torrentFilePriority ->
            val callback = torrentFileUpdateListeners[index]
            if (callback == null) {
                queuedFilePriorities[index] = torrentFilePriority
            } else {
                callback.onUpdatePriority(torrentFilePriority)
            }
        }
    }

}
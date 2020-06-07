package app.shynline.torient.screens.torrentfiles

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.shynline.torient.R
import app.shynline.torient.domain.models.TorrentFile
import app.shynline.torient.domain.models.TorrentFilePriority
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.screens.torrentfiles.items.FileItem
import app.shynline.torient.screens.torrentfiles.items.FolderItem
import com.google.android.material.checkbox.MaterialCheckBox
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.GenericItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.adapters.GenericFastItemAdapter
import com.mikepenz.fastadapter.expandable.ExpandableExtension
import com.mikepenz.fastadapter.expandable.getExpandableExtension
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.fastadapter.listeners.LongClickEventHook

class TorrentFilesViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentFilesViewMvc.Listener>(), TorrentFilesViewMvc,
    FileItem.Subscription {

    private val recyclerView: RecyclerView
    private val emptyPlaceHolder: ViewGroup
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
        emptyPlaceHolder = findViewById(R.id.emptyPlaceHolder)
        fastItemAdapter = FastItemAdapter()
        val adapter = FastAdapter.with(listOf(fastItemAdapter))
        expandableExtension = adapter.getExpandableExtension()
        recyclerView.layoutManager = LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter

        adapter.addEventHook(object : LongClickEventHook<FileItem>() {
            override fun onLongClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<FileItem>,
                item: FileItem
            ): Boolean {
                val menu = PopupMenu(getContext(), v)
                menu.inflate(R.menu.torrent_files_file_menu)
                var fileDownloaded = false
                getListeners().forEach { listener ->
                    fileDownloaded =
                        fileDownloaded or listener.isFileCompleted(item.torrentFile.index)
                }
                // Only show the menu if file is downloaded
                // Because it's the only option available in menu for now
                if (!fileDownloaded) {
                    return false
                }

                menu.menu.findItem(R.id.save_file_to_download).apply {
                    isVisible = fileDownloaded
                }

                menu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.save_file_to_download -> {
                            getListeners().forEach { listener ->
                                listener.saveFile(item.torrentFile.index)
                            }
                            true
                        }
                        else -> false
                    }
                }
                menu.show()
                return true
            }

            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                if (viewHolder is FileItem.ViewHolder) {
                    return viewHolder.itemView
                }
                return null
            }
        })

        adapter.addEventHook(object : ClickEventHook<FileItem>() {
            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<FileItem>,
                item: FileItem
            ) {
                getListeners().forEach { listener ->
                    listener.onPriorityClicked(item.torrentFile.index)
                }
            }

            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                if (viewHolder is FileItem.ViewHolder) {
                    return viewHolder.priorityTv
                }
                return null
            }
        })

        adapter.addEventHook(object : ClickEventHook<FileItem>() {
            override fun onClick(
                v: View,
                position: Int,
                fastAdapter: FastAdapter<FileItem>,
                item: FileItem
            ) {
                getListeners().forEach { listener ->
                    listener.onDownloadCheckBoxClicked(
                        item.torrentFile.index,
                        (v as MaterialCheckBox).isChecked
                    )
                }
            }

            override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
                if (viewHolder is FileItem.ViewHolder) {
                    return viewHolder.downloadCb
                }
                return null
            }
        })

    }

    override fun saveState(): HashMap<String, Any> {
        val state = HashMap<String, Any>()
        val foldersState = HashMap<Long, Boolean>()
        fastItemAdapter.adapterItems.forEach {
            if (it is FolderItem) {
                foldersState[it.identifier] = it.isExpanded
                saveFolderState(it.subItems, foldersState)
            }
        }
        state["folders_state"] = foldersState
        return state
    }

    private fun saveFolderState(items: List<ISubItem<*>>, foldersState: HashMap<Long, Boolean>) {
        items.forEach {
            if (it is FolderItem) {
                foldersState[it.identifier] = it.isExpanded
                saveFolderState(it.subItems, foldersState)
            }
        }
    }

    private fun loadFolderState(items: List<ISubItem<*>>, foldersState: HashMap<Long, Boolean>?) {
        items.forEach { item ->
            if (item is FolderItem) {
                val position = fastItemAdapter.getPosition(item.identifier)
                if (position != -1) {
                    val expand = foldersState?.get(item.identifier) ?: true
                    if (expand) {
                        expandableExtension.expand(position)
                    } else {
                        expandableExtension.collapse(position)
                    }
                }
                loadFolderState(item.subItems, foldersState)
            }
        }
    }

    override fun loadState(state: HashMap<String, Any>?) {
        @Suppress("UNCHECKED_CAST")
        val foldersState = state?.get("folders_state") as? HashMap<Long, Boolean>
        fastItemAdapter.adapterItems.forEach { item ->
            if (item is FolderItem) {
                val position = fastItemAdapter.getPosition(item.identifier)
                if (position != -1) {
                    val expand = foldersState?.get(item.identifier) ?: true
                    if (expand) {
                        expandableExtension.expand(position)
                    } else {
                        expandableExtension.collapse(position)
                    }
                }
                loadFolderState(item.subItems, foldersState)
            }
        }

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
        emptyPlaceHolder.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
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
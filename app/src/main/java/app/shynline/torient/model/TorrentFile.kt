package app.shynline.torient.model

import app.shynline.torient.utils.FileType
import app.shynline.torient.utils.FileTypeDetector
import com.frostwire.jlibtorrent.TorrentInfo

data class TorrentFile(
    val name: String,
    var size: Long,
    val isFolder: Boolean,
    var index: Int = -1,
    var files: MutableList<TorrentFile>? = null
) {
    val fileType by lazy {
        if (isFolder)
            return@lazy FileType.DIR
        FileTypeDetector.getType(name)
    }
    companion object {
        fun from(torrentInfo: TorrentInfo): TorrentFile {
            var fileTree: TorrentFile? = null
            for (index in 0 until torrentInfo.files().numFiles()) {
                val path = torrentInfo.files().filePath(index).split("/")
                var current = fileTree
                if (current != null) {
                    current.size += torrentInfo.files().fileSize(index)
                }
                for (i in path.indices) {
                    if (i < path.size - 1) { // folder
                        if (fileTree == null) {
                            // It's the root folder
                            fileTree = TorrentFile(
                                path[i],
                                torrentInfo.files().fileSize(index),
                                true,
                                files = mutableListOf()
                            )
                            // And make the current directory the root
                            current = fileTree
                        } else {
                            // It's a sub folder
                            if (i == 0) {
                                // index 0 refer to file tree
                                // and file tree already exists
                                continue
                            }
                            val ind = current!!.files!!.indexOfFirst { it.name == path[i] }
                            if (ind == -1) { // New folder
                                // Creating a new folder
                                // As our current directory
                                val newFolder = TorrentFile(
                                    path[i],
                                    torrentInfo.files().fileSize(index),
                                    true,
                                    files = mutableListOf()
                                )
                                // Add it to file tree
                                current.files!!.add(newFolder)
                                current = newFolder
                            } else { // ind is the index of the folder
                                // Make the folder our current directory
                                current = current.files!![ind]
                                current.size += torrentInfo.files().fileSize(index)
                            }
                        }
                    } else { //file
                        if (fileTree == null) {
                            // If file tree is null and first item is a file
                            // certainly it's a single file torrent
                            return TorrentFile(
                                path[0],
                                torrentInfo.files().fileSize(index),
                                false,
                                index = 0
                            )
                        } else {
                            // The last part of a branch is the file
                            current!!.files!!.add(
                                TorrentFile(
                                    path[i],
                                    torrentInfo.files().fileSize(index),
                                    false,
                                    index = index
                                )
                            )
                        }
                    }
                }
            }

            return fileTree!!
        }

    }
}
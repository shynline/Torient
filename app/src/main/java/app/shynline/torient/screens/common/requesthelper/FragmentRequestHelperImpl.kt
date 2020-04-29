package app.shynline.torient.screens.common.requesthelper

import android.content.Intent
import android.content.Intent.ACTION_OPEN_DOCUMENT_TREE
import androidx.fragment.app.Fragment

const val REQUEST_ID_OPEN_TORRENT_FILE = 100
const val REQUEST_ID_OPEN_DIRECTORY = 101

class FragmentRequestHelperImpl(private val fragment: Fragment) :
    FragmentRequestHelper {

    private val EXTRA_SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED"

    override fun openTorrentFile(requestId: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        fragment.startActivityForResult(intent, requestId)
    }


    override fun openDirectory(requestId: Int) {
        val intent = Intent(ACTION_OPEN_DOCUMENT_TREE).apply {
            putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            putExtra(EXTRA_SHOW_ADVANCED, true)
        }
        fragment.startActivityForResult(intent, requestId)
    }

}
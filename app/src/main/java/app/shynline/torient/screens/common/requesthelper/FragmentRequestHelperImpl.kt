package app.shynline.torient.screens.common.requesthelper

import android.content.Intent
import androidx.fragment.app.Fragment
import app.shynline.torient.transfer.TransferService

const val REQUEST_ID_OPEN_TORRENT_FILE = 100

class FragmentRequestHelperImpl(private val fragment: Fragment) :
    FragmentRequestHelper {

    override fun openTorrentFile(requestId: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/x-bittorrent"
        }
        fragment.startActivityForResult(intent, requestId)
    }

    override fun saveToDownload(name: String) {
        TransferService.copyFile(fragment.requireContext(), name)
    }
}
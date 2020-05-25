package app.shynline.torient.screens.common.requesthelper

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
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

    override fun saveToDownload(name: String, infoHash: String) {
        TransferService.copyFile(fragment.requireContext(), name, infoHash)
    }

    override fun copyMagnetToClipBoard(name: String, magnet: String) {
        val clipBoardManager = fragment.requireContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("magnet: $name", magnet)
        clipBoardManager.setPrimaryClip(clip)
        Toast.makeText(fragment.requireContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }
}
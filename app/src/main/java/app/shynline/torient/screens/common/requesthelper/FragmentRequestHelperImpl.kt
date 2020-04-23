package app.shynline.torient.screens.common.requesthelper

import android.content.Intent
import androidx.fragment.app.Fragment

class FragmentRequestHelperImpl(private val fragment: Fragment) :
    FragmentRequestHelper {

    override fun openTorrentFile(requestId: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        fragment.startActivityForResult(intent, requestId)
    }

}
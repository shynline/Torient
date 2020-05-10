package app.shynline.torient.screens.addmagnet

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.utils.Magnet
import org.koin.android.ext.android.inject

class AddMagnetFragment : DialogFragment(), AddMagnetViewMvc.Listener {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val torrentMediator by inject<TorrentMediator>()
    private var width = 0
    private var height = 0
    private lateinit var pageNavigationHelper: PageNavigationHelper
    private lateinit var viewMvc: AddMagnetViewMvc

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().resources.displayMetrics.let {
            if (it.widthPixels > it.heightPixels) {
                width = (it.heightPixels * 0.8f).toInt()
                height = (it.heightPixels * 0.4f).toInt()
            } else {
                width = (it.widthPixels * 0.8f).toInt()
                height = (it.widthPixels * 0.4f).toInt()
            }
        }
        pageNavigationHelper = PageNavigationHelper(findNavController())
        viewMvc = viewMvcFactory.getAddMagnetViewMvc(inflater, container)

        return viewMvc.getRootView()
    }

    override fun onResume() {
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        super.onResume()
    }

    override fun addMagnet(magnetStr: String) {
        if (magnetStr.startsWith("magnet")) {
            val magnet = Magnet.parse(magnetStr)
            if (magnet != null) {
                pageNavigationHelper.back()
                if (torrentMediator.isTorrentFileCached(magnet.infoHash!!)) {
                    pageNavigationHelper.showNewTorrentDialog(magnet.infoHash!!)
                } else {
                    pageNavigationHelper.showNewTorrentDialogByMagnet(magnetStr)
                }
            } else {
                showInvalidMagnetError()
            }
        } else {
            showInvalidMagnetError()
        }
    }

    private fun showInvalidMagnetError() {
        viewMvc.showError("Invalid magnet link")
    }

    override fun onStart() {
        super.onStart()
        viewMvc.registerListener(this)
    }

    override fun onStop() {
        super.onStop()
        viewMvc.unRegisterListener(this)
    }

    override fun pasteClipBoard() {
        val clipBoardManager = requireContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (clipBoardManager.primaryClip?.itemCount ?: 0 > 0) {
            val clipText = clipBoardManager.primaryClip!!.getItemAt(0).text.toString()
            viewMvc.showClipBoard(clipText)
        }
    }
}
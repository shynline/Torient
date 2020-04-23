package app.shynline.torient.screens.torrentslist

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelperImpl
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope
import java.io.BufferedInputStream

const val REQUEST_ID_OPEN_TORRENT_FILE = 100

class TorrentsListFragment : BaseFragment() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val controller by lifecycleScope.inject<TorrentsListController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewMvc = viewMvcFactory.getTorrentListViewMvc(inflater, container)
        controller.bind(
            viewMvc,
            FragmentRequestHelperImpl(
                this
            )
        )
        return viewMvc.getRootView()
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onStop() {
        super.onStop()
        controller.onStop()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_ID_OPEN_TORRENT_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            data?.data?.also { uri ->
                requireContext().contentResolver.openInputStream(uri)?.use {
                    BufferedInputStream(it).use { s ->
                        controller.openTorrentFile(s.readBytes())
                    }
                }
            }

        }
    }

}
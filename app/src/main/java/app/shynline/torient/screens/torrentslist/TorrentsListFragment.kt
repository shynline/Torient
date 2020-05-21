package app.shynline.torient.screens.torrentslist

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelperImpl
import app.shynline.torient.screens.common.requesthelper.REQUEST_ID_OPEN_TORRENT_FILE
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope
import java.io.BufferedInputStream


class TorrentsListFragment : BaseFragment<TorrentsListController>() {
    private val viewMvcFactory by inject<ViewMvcFactory>()
    override val controller: TorrentsListController
        get() = lifecycleScope.get()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getTorrentListViewMvc(inflater, container)
        controller.bind(
            viewMvc,
            FragmentRequestHelperImpl(this),
            PageNavigationHelper(findNavController())
        )
        return viewMvc.getRootView()
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
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

}
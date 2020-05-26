package app.shynline.torient.screens.torrentoverview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.basefragment.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class TorrentOverviewFragment : BaseFragment<TorrentOverviewController>() {

    private val viewMvcFactory by inject<ViewMvcFactory>()

    override val controller: TorrentOverviewController
        get() = lifecycleScope.get()
    private val navArgs by navArgs<TorrentOverviewFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getTorrentOverViewViewMvc(inflater, container)
        controller.bind(
            viewMvc
        )
        controller.setTorrent(navArgs.infohash)
        return viewMvc.getRootView()
    }

}
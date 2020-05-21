package app.shynline.torient.screens.torrentpreference

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class TorrentPreferenceFragment : BaseFragment<TorrentPreferenceController>() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    override val controller: TorrentPreferenceController
        get() = lifecycleScope.get()
    private val navArgs by navArgs<TorrentPreferenceFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getTorrentPreferenceViewMvc(inflater, container)
        controller.bind(
            viewMvc
        )
        controller.setTorrent(navArgs.infohash)
        return viewMvc.getRootView()
    }

}
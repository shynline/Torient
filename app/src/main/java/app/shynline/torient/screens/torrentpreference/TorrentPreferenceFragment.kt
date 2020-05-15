package app.shynline.torient.screens.torrentpreference

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class TorrentPreferenceFragment : BaseFragment() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val controller by lifecycleScope.inject<TorrentPreferenceController>()
    private val navArgs by navArgs<TorrentPreferenceFragmentArgs>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewMvc = viewMvcFactory.getTorrentPreferenceViewMvc(inflater, container)
        controller.bind(
            viewMvc
        )
        controller.setTorrent(navArgs.infohash)
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

    override fun onDestroyView() {
        super.onDestroyView()
        controller.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.onDestroy()
    }
}
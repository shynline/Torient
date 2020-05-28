package app.shynline.torient.screens.torrentfiles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.basefragment.BaseFragment
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelperImpl
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class TorrentFilesFragment : BaseFragment<TorrentFilesController>() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    override val controller: TorrentFilesController
        get() = lifecycleScope.get()

    private val navArgs by navArgs<TorrentFilesFragmentArgs>()
    private lateinit var infoHash: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        infoHash = navArgs.infohash
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getTorrentFilesViewMvc(inflater, container)
        controller.bind(
            viewMvc,
            FragmentRequestHelperImpl(this)
        )
        controller.setTorrent(infoHash)
        return viewMvc.getRootView()
    }
}
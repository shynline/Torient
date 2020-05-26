package app.shynline.torient.screens.newtorrent

import android.graphics.PointF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.basefragment.BaseDialogFragment
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelperImpl
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope


class NewTorrentFragment : BaseDialogFragment<NewTorrentController>() {

    private val viewMvcFactory by inject<ViewMvcFactory>()

    private val args by navArgs<NewTorrentFragmentArgs>()
    private val infoHash by lazy { args.infoHash }

    override val controller: NewTorrentController
        get() = lifecycleScope.get()

    override val portraitRatioWH: PointF
        get() = PointF(0.8f, 0.6f)
    override val landscapeRatioWH: PointF
        get() = PointF(0.6f, 0.8f)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getNewTorrentViewMvc(inflater, container)
        controller.bind(
            viewMvc, PageNavigationHelper(findNavController()),
            FragmentRequestHelperImpl(this)
        )
        controller.showTorrent(infoHash)
        return viewMvc.getRootView()
    }

}
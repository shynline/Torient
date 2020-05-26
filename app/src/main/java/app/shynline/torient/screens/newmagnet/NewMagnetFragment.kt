package app.shynline.torient.screens.newmagnet

import android.graphics.PointF
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.basefragment.BaseDialogFragment
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class NewMagnetFragment : BaseDialogFragment<NewMagnetController>() {

    private val magnet: String by lazy {
        args.magnet
    }
    private val args by navArgs<NewMagnetFragmentArgs>()
    private val viewMvcFactory by inject<ViewMvcFactory>()

    override val controller: NewMagnetController
        get() = lifecycleScope.get()

    override val landscapeRatioWH: PointF
        get() = PointF(0.4f, 0.7f)

    override val portraitRatioWH: PointF
        get() = PointF(0.7f, 0.4f)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getNewMagnetViewMvc(
            inflater, container
        )
        controller.bind(
            viewMvc,
            PageNavigationHelper(findNavController())
        )
        controller.showTorrent(magnet)
        return viewMvc.getRootView()
    }

}
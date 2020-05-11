package app.shynline.torient.screens.torrentdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class TorrentDetailFragment : BaseFragment() {
    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val controller by lifecycleScope.inject<TorrentDetailController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewMvc = viewMvcFactory.getTorrentDetailViewMvc(inflater, container)
        controller.bind(
            viewMvc,
            PageNavigationHelper(findNavController())
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

    override fun onDestroyView() {
        super.onDestroyView()
        controller.unbind()
    }

    override fun onDestroy() {
        super.onDestroy()
        controller.onDestroy()
    }


}
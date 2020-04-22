package app.shynline.torient.screens.torrentslist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class TorrentsListFragment : BaseFragment() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val controller by lifecycleScope.inject<TorrentsListController>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewMvc = viewMvcFactory.getTorrentListViewMvc(inflater, container)
        controller.bind(viewMvc)
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

}
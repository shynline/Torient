package app.shynline.torient.screens.preference

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.BaseFragment
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class PreferenceFragment : BaseFragment<PreferenceController>() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    override val controller: PreferenceController
        get() = lifecycleScope.get()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View {
        val viewMvc = viewMvcFactory.getPreferenceViewMvc(inflater, container)
        controller.bind(
            viewMvc
        )
        return viewMvc.getRootView()
    }
}
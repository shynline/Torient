package app.shynline.torient.screens.common.view

import android.content.Context
import android.view.View
import androidx.annotation.StringRes

abstract class BaseViewMvc : ViewMvc {
    private lateinit var rootView: View
    override fun getRootView(): View {
        return rootView
    }

    protected fun setRootView(rootView: View) {
        this.rootView = rootView
    }

    protected fun <T : View> findViewById(id: Int): T {
        return getRootView().findViewById(id)
    }

    protected fun getString(@StringRes id: Int) = getContext().getString(id)

    protected fun getContext(): Context {
        return rootView.context
    }
}
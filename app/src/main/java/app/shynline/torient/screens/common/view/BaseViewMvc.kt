package app.shynline.torient.screens.common.view

import android.content.Context
import android.view.View
import androidx.annotation.StringRes

abstract class BaseViewMvc : ViewMvc {
    private lateinit var mRootView: View
    override fun getRootView(): View {
        return mRootView
    }

    protected fun setRootView(rootView: View) {
        mRootView = rootView
    }

    protected fun <T : View> findViewById(id: Int): T {
        return getRootView().findViewById(id)
    }

    protected fun getString(@StringRes id: Int) = getContext().getString(id)

    protected fun getContext(): Context {
        return mRootView.context
    }
}
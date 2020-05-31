package app.shynline.torient.screens.common.basefragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.shynline.torient.screens.common.BaseController

abstract class BaseFragment<CONTROLLER : BaseController> : Fragment() {
    abstract val controller: CONTROLLER

    companion object {
        const val CONTROLLER_STATE = "controllerstate"
    }

    abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        controller.onCreateView()
        return onCreateView(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            // Load state
            savedInstanceState.getSerializable(CONTROLLER_STATE)?.let {
                @Suppress("UNCHECKED_CAST")
                controller.loadState(it as HashMap<String, Any>)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        controller.saveState()?.let {
            outState.putSerializable(CONTROLLER_STATE, it)
        }
    }

    override fun onStart() {
        super.onStart()
        controller.onStart()
    }

    override fun onStop() {
        controller.onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        controller.onViewDestroy()
    }
}
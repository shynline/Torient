package app.shynline.torient.screens.common

import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

abstract class BaseDialogFragment<CONTROLLER : BaseController> : DialogFragment() {
    abstract val controller: CONTROLLER
    open val backGroundColor = android.R.color.transparent

    companion object {
        const val CONTROLLER_STATE = "controllerstate"
    }

    private var width = 0
    private var height = 0
    abstract val portraitRatioWH: PointF
    abstract val landscapeRatioWH: PointF

    abstract fun onCreateView(inflater: LayoutInflater, container: ViewGroup?): View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        controller.onCreateView()
        requireActivity().resources.displayMetrics.let {
            width =
                (it.widthPixels * if (it.widthPixels > it.heightPixels) landscapeRatioWH.x else portraitRatioWH.x).toInt()
            height =
                (it.heightPixels * if (it.widthPixels > it.heightPixels) landscapeRatioWH.y else portraitRatioWH.y).toInt()
        }
        return onCreateView(inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (savedInstanceState != null) {
            // Load state
            (savedInstanceState.getSerializable(CONTROLLER_STATE) as? HashMap<String, Any>)?.let {
                controller.loadState(it)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CONTROLLER_STATE, controller.saveState())
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
        controller.unbind()
        super.onDestroyView()
        controller.onViewDestroy()
    }

    override fun onResume() {
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window!!.setBackgroundDrawableResource(backGroundColor)
        super.onResume()
    }

}
package app.shynline.torient.screens.newmagnet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope

class NewMagnetFragment : DialogFragment() {
    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val controller by lifecycleScope.inject<NewMagnetController>()
    private val args by navArgs<NewMagnetFragmentArgs>()
    private lateinit var magnet: String

    private var width = 0
    private var height = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.magnet?.let {
            magnet = it
            return
        }
        findNavController().navigateUp()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireActivity().resources.displayMetrics.let {
            width = (it.widthPixels * if (it.widthPixels > it.heightPixels) 0.4f else 0.7f).toInt()
            height =
                (it.heightPixels * if (it.widthPixels > it.heightPixels) 0.7f else 0.4f).toInt()
        }
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

    override fun onResume() {
        dialog!!.window!!.setLayout(width, height)
        dialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        super.onResume()
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
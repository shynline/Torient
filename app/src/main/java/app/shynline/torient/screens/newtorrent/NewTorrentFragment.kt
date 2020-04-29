package app.shynline.torient.screens.newtorrent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import app.shynline.torient.common.di.viewfactory.ViewMvcFactory
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelperImpl
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.lifecycleScope


class NewTorrentFragment : DialogFragment() {

    private val viewMvcFactory by inject<ViewMvcFactory>()
    private val controller by lifecycleScope.inject<NewTorrentController>()
    private val args by navArgs<NewTorrentFragmentArgs>()
    private lateinit var infoHash: String
    private var width = 0
    private var height = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.infoHash?.let {
            infoHash = it
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
            width = (it.widthPixels * 0.9f).toInt()
            height = (it.heightPixels * 0.8f).toInt()
        }
        val viewMvc = viewMvcFactory.getNewTorrentViewMvc(inflater, container)
        controller.bind(
            viewMvc, PageNavigationHelper(findNavController()),
            FragmentRequestHelperImpl(this)
        )
        controller.showTorrent(infoHash)
        return viewMvc.getRootView()
    }

    override fun onResume() {
        dialog!!.window!!.setLayout(width, height)
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
}
package app.shynline.torient.screens.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.shynline.torient.R
import kotlinx.android.synthetic.main.fragment_about.*

class AboutFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ver = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        version.text = "Version ${ver.versionName} (${ver.longVersionCode})"

        description.text =
            "Torient is a free and open source torrent client \nfor Android 5.0 and above."

        licence.text = "Distributed under the GNU GPL v3. Created by Shynline."

        sourceCode.text = "Source code is available at\nhttps://github.com/shynline/Torient"
    }
}
package app.shynline.torient.screens.newmagnet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.domain.torrentmanager.utils.Magnet

class NewMagnetViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<NewMagnetViewMvc.Listener>(), NewMagnetViewMvc {

    private val downloadBtn: Button
    private val nameTv: TextView
    private val infoHashTv: TextView

    init {
        setRootView(inflater.inflate(R.layout.fragment_new_magnet, parent, false))
        downloadBtn = findViewById(R.id.downloadBtn)
        nameTv = findViewById(R.id.name)
        infoHashTv = findViewById(R.id.infoHash)

        downloadBtn.setOnClickListener {
            getListeners().forEach {
                it.onDownloadClicked()
            }
        }
    }

    override fun showMagnet(magnet: Magnet) {
        nameTv.text = magnet.name
        infoHashTv.text = magnet.infoHash
    }

}
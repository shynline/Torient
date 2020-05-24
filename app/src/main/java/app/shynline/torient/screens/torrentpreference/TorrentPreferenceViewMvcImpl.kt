package app.shynline.torient.screens.torrentpreference

import android.view.LayoutInflater
import android.view.ViewGroup
import app.shynline.torient.R
import app.shynline.torient.database.entities.TorrentPreferenceSchema
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

class TorrentPreferenceViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentPreferenceViewMvc.Listener>(), TorrentPreferenceViewMvc {

    private val honorGlobalSpeed: MaterialCheckBox
    private val downloadRateLimit: MaterialCheckBox
    private val uploadRateLimit: MaterialCheckBox
    private val downloadRateET: TextInputEditText
    private val uploadRateET: TextInputEditText
    private val maximumPeerET: TextInputEditText
    private val honorGlobalMaximumPeer: MaterialCheckBox

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_preference, parent, false))
        honorGlobalSpeed = findViewById(R.id.honorGlobalLimitCheckBox)
        downloadRateLimit = findViewById(R.id.limitDownloadLimitCheckBox)
        uploadRateLimit = findViewById(R.id.limitUploadLimitCheckBox)
        downloadRateET = findViewById(R.id.limitDownloadRateTextInputEditText)
        uploadRateET = findViewById(R.id.limitUploadRateTextInputEditText)
        maximumPeerET = findViewById(R.id.maximumPeerTextInputEditText)
        honorGlobalMaximumPeer = findViewById(R.id.honorGlobalPeerConnection)
    }

    override fun updateUi(preferenceSchema: TorrentPreferenceSchema) {
        honorGlobalSpeed.isChecked = preferenceSchema.honorGlobalRate
        downloadRateLimit.isChecked = preferenceSchema.downloadRateLimit
        uploadRateLimit.isChecked = preferenceSchema.uploadRateLimit
        downloadRateET.setText(preferenceSchema.downloadRate.toString())
        uploadRateET.setText(preferenceSchema.uploadRate.toString())
        honorGlobalMaximumPeer.isChecked = preferenceSchema.honorMaxConnection
        maximumPeerET.setText(preferenceSchema.maxConnection.toString())
    }
}
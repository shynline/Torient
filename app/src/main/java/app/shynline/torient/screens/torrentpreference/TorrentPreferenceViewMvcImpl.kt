package app.shynline.torient.screens.torrentpreference

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import app.shynline.torient.R
import app.shynline.torient.domain.database.entities.TorrentPreferenceSchema
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.utils.DefaultTextWatcher
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

class TorrentPreferenceViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentPreferenceViewMvc.Listener>(), TorrentPreferenceViewMvc,
    CompoundButton.OnCheckedChangeListener {

    private val honorGlobalSpeed: MaterialCheckBox
    private val downloadRateLimit: MaterialCheckBox
    private val uploadRateLimit: MaterialCheckBox
    private val downloadRateET: TextInputEditText
    private val uploadRateET: TextInputEditText
    private lateinit var downloadRateTextWatcher: TextWatcher
    private lateinit var uploadRateTextWatcher: TextWatcher

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_preference, parent, false))
        honorGlobalSpeed = findViewById(R.id.honorGlobalLimitCheckBox)
        downloadRateLimit = findViewById(R.id.limitDownloadLimitCheckBox)
        uploadRateLimit = findViewById(R.id.limitUploadLimitCheckBox)
        downloadRateET = findViewById(R.id.limitDownloadRateTextInputEditText)
        uploadRateET = findViewById(R.id.limitUploadRateTextInputEditText)
        honorGlobalSpeed.setOnCheckedChangeListener(this)
        downloadRateLimit.setOnCheckedChangeListener(this)
        uploadRateLimit.setOnCheckedChangeListener(this)
        initiateTextWatchers()
    }

    private fun initiateTextWatchers() {
        downloadRateTextWatcher = object : DefaultTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = downloadRateET.text.toString()
                try {
                    getListeners().forEach { listener ->
                        listener.onDownloadLimitChanged(str.toString().toInt())
                    }
                } catch (e: Exception) {

                }

            }
        }
        uploadRateTextWatcher = object : DefaultTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = uploadRateET.text.toString()
                try {
                    getListeners().forEach { listener ->
                        listener.onUploadLimitChanged(str.toString().toInt())
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun onListenerRegistered() {
        super.onListenerRegistered()
        downloadRateET.addTextChangedListener(downloadRateTextWatcher)
        uploadRateET.addTextChangedListener(uploadRateTextWatcher)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == null)
            return
        when (buttonView.id) {
            R.id.honorGlobalLimitCheckBox -> {
                getListeners().forEach { listener ->
                    listener.onHonorGlobalLimitChanged(isChecked)
                }
            }
            R.id.limitDownloadLimitCheckBox -> {
                getListeners().forEach { listener ->
                    listener.onLimitDownloadRateChanged(isChecked)
                }
            }
            R.id.limitUploadLimitCheckBox -> {
                getListeners().forEach { listener ->
                    listener.onLimitUploadRateChanged(isChecked)
                }
            }
        }
    }

    override fun onListenerUnRegistered() {
        super.onListenerUnRegistered()
        downloadRateET.removeTextChangedListener(downloadRateTextWatcher)
        uploadRateET.removeTextChangedListener(uploadRateTextWatcher)
    }

    override fun updateUi(preferenceSchema: TorrentPreferenceSchema) {
        honorGlobalSpeed.isChecked = preferenceSchema.honorGlobalRate
        downloadRateLimit.isChecked = preferenceSchema.downloadRateLimit
        uploadRateLimit.isChecked = preferenceSchema.uploadRateLimit
        downloadRateET.setText(preferenceSchema.downloadRate.toString())
        uploadRateET.setText(preferenceSchema.uploadRate.toString())
    }
}
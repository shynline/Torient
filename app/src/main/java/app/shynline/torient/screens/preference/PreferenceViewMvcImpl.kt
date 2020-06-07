package app.shynline.torient.screens.preference

import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.CompoundButton
import app.shynline.torient.R
import app.shynline.torient.domain.userpreference.UserPreference
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.utils.DefaultTextWatcher
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

class PreferenceViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<PreferenceViewMvc.Listener>(), PreferenceViewMvc,
    CompoundButton.OnCheckedChangeListener {

    private val downloadRateLimit: MaterialCheckBox
    private val uploadRateLimit: MaterialCheckBox
    private val downloadRateET: TextInputEditText
    private val uploadRateET: TextInputEditText
    private val maximumPeerET: TextInputEditText
    private lateinit var downloadRateTextWatcher: TextWatcher
    private lateinit var uploadRateTextWatcher: TextWatcher
    private lateinit var maximumPeerTextWatcher: TextWatcher

    init {
        setRootView(inflater.inflate(R.layout.fragment_preference, parent, false))
        downloadRateLimit = findViewById(R.id.limitDownloadLimitCheckBox)
        uploadRateLimit = findViewById(R.id.limitUploadLimitCheckBox)
        downloadRateET = findViewById(R.id.limitDownloadRateTextInputEditText)
        uploadRateET = findViewById(R.id.limitUploadRateTextInputEditText)
        maximumPeerET = findViewById(R.id.maximumPeerTextInputEditText)
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
                        listener.onDownloadLimitChanged(str.toInt())
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
                        listener.onUploadLimitChanged(str.toInt())
                    }
                } catch (e: Exception) {

                }
            }
        }
        maximumPeerTextWatcher = object : DefaultTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val str = maximumPeerET.text.toString()
                try {
                    getListeners().forEach { listener ->
                        listener.onMaximumPeerChanged(str.toInt())
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    override fun onListenerRegistered() {
        super.onListenerRegistered()
        // Only add text change listeners if the first listener is registered
        if (getListeners().size == 1) {
            downloadRateET.addTextChangedListener(downloadRateTextWatcher)
            uploadRateET.addTextChangedListener(uploadRateTextWatcher)
            maximumPeerET.addTextChangedListener(maximumPeerTextWatcher)
        }
    }

    override fun onListenerUnRegistered() {
        super.onListenerUnRegistered()
        // Remove the text change listeners if all listeners has been removed
        if (getListeners().isEmpty()) {
            downloadRateET.removeTextChangedListener(downloadRateTextWatcher)
            uploadRateET.removeTextChangedListener(uploadRateTextWatcher)
            maximumPeerET.removeTextChangedListener(maximumPeerTextWatcher)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (buttonView == null)
            return
        when (buttonView.id) {
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

    override fun updateUi(userPreference: UserPreference) {
        downloadRateLimit.isChecked = userPreference.globalDownloadRateLimit
        uploadRateLimit.isChecked = userPreference.globalUploadRateLimit
        downloadRateET.setText(userPreference.globalDownloadRate.toString())
        uploadRateET.setText(userPreference.globalUploadRate.toString())
        maximumPeerET.setText(userPreference.globalMaxConnection.toString())
    }

}
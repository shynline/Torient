package app.shynline.torient.screens.addmagnet

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import app.shynline.torient.R
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AddMagnetViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<AddMagnetViewMvc.Listener>(), AddMagnetViewMvc {

    private val inputLayout: TextInputLayout
    private val inputEditText: TextInputEditText
    private val addBtn: Button
    private val pasteBtn: ImageView

    init {
        setRootView(inflater.inflate(R.layout.fragment_add_magnet, parent, false))
        addBtn = findViewById(R.id.addBtn)
        inputEditText = findViewById(R.id.inputEditText)
        inputLayout = findViewById(R.id.inputLayout)
        pasteBtn = findViewById(R.id.PasteBtn)
        addBtn.setOnClickListener {
            getListeners().forEach {
                it.addMagnet(inputEditText.text.toString())
            }
        }
        pasteBtn.setOnClickListener {
            getListeners().forEach {
                it.pasteClipBoard()
            }
        }
    }

    override fun showClipBoard(string: String) {
        inputEditText.setText(string)
    }

    override fun showError(error: String) {
        inputLayout.error = error
    }
}
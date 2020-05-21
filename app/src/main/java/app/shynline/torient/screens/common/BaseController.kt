package app.shynline.torient.screens.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseController {
    protected lateinit var controllerScope: CoroutineScope

    abstract fun saveState(): HashMap<String, Any>?

    abstract fun loadState(state: HashMap<String, Any>?)

    abstract fun unbind()

    fun onCreateView() {
        controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    }

    abstract fun onStart()

    abstract fun onStop()

    fun onViewDestroy() {
        controllerScope.cancel()
    }

}
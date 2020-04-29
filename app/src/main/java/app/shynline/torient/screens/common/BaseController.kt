package app.shynline.torient.screens.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseController {
    protected val controllerScope by lazy {
        CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    abstract fun onStart()

    open fun onStop() {
        controllerScope.cancel()
    }
}
package app.shynline.torient.screens.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseController {
    protected val controllerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    abstract fun onStart()

    abstract fun onStop()

    fun onDestroy() {
        controllerScope.cancel()
    }
}
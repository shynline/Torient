package app.shynline.torient.screens.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseController(
    private val coroutineDispatcher: CoroutineDispatcher
) {
    protected lateinit var controllerScope: CoroutineScope

    abstract fun saveState(): HashMap<String, Any>?

    abstract fun loadState(state: HashMap<String, Any>?)

    fun onCreateView() {
        controllerScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)
    }

    abstract fun onStart()

    abstract fun onStop()

    fun onViewDestroy() {
        controllerScope.cancel()
    }

}
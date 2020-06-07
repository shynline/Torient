package app.shynline.torient.domain.torrentmanager.service

interface ActivityCycle {
    fun onActivityStart()
    fun onActivityStop()
}
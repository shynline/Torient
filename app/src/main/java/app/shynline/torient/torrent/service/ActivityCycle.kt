package app.shynline.torient.torrent.service

interface ActivityCycle {
    fun onActivityStart()
    fun onActivityStop()
}
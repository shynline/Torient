package app.shynline.torient.screens.common.navigationhelper

import androidx.navigation.NavController
import app.shynline.torient.R
import app.shynline.torient.screens.torrentslist.TorrentsListFragmentDirections


class PageNavigationHelper(
    private val navController: NavController
) {
    fun showNewTorrentDialog(infoHash: String) {
        val action =
            TorrentsListFragmentDirections.actionTorrentListFragmentToNewTorrentFragment(infoHash)
        // This condition help to not reNavigation to same destination
        // Which cause exception
        if (navController.currentDestination?.id == R.id.torrent_list_fragment)
            navController.navigate(action)
    }

    fun showNewTorrentDialogByMagnet(magnet: String) {
        val action =
            TorrentsListFragmentDirections.actionTorrentListFragmentToNewMagnetFragment(magnet)
        // This condition help to not reNavigation to same destination
        // Which cause exception
        if (navController.currentDestination?.id == R.id.torrent_list_fragment)
            navController.navigate(action)
    }

    fun back() {
        navController.navigateUp()
    }

    fun showAddMagnetDialog() {
        val action = TorrentsListFragmentDirections
            .actionTorrentListFragmentToAddMagnetFragment()
        if (navController.currentDestination?.id == R.id.torrent_list_fragment)
            navController.navigate(action)
    }

    fun showTorrentDetail(infoHash: String) {
        val action = TorrentsListFragmentDirections
            .actionTorrentListFragmentToTorrentDetailFragment(infoHash)
        if (navController.currentDestination?.id == R.id.torrent_list_fragment)
            navController.navigate(action)
    }
}
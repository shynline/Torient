package app.shynline.torient.screens.common.navigationhelper

import androidx.navigation.NavController
import app.shynline.torient.screens.torrentslist.TorrentsListFragmentDirections


class PageNavigationHelper(
    private val navController: NavController
) {
    fun showNewTorrentDialog(infoHash: String) {
        val action =
            TorrentsListFragmentDirections.actionTorrentListFragmentToNewTorrentFragment(infoHash)
        navController.navigate(action)
    }

    fun back() {
        navController.navigateUp()
    }
}
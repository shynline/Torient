package app.shynline.torient.screens.common.navigationhelper

import androidx.core.os.bundleOf
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

    fun showTorrentOverView(infoHash: String) {
        if (navController.currentDestination!!.id in listOf(
                R.id.torrentFilesFragment, R.id.torrentPreferenceFragment
            )
        ) {
            navController.navigateUp()
        }
        if (navController.currentDestination?.id != R.id.torrentOverviewFragment)
            navController.navigate(R.id.torrentOverviewFragment, bundleOf("infohash" to infoHash))
    }

    fun showTorrentFiles(infoHash: String) {
        if (navController.currentDestination!!.id in listOf(
                R.id.torrentOverviewFragment,
                R.id.torrentPreferenceFragment
            )
        ) {
            navController.navigateUp()
        }
        if (navController.currentDestination?.id != R.id.torrentFilesFragment)
            navController.navigate(R.id.torrentFilesFragment, bundleOf("infohash" to infoHash))
    }

    fun showTorrentPreference(infoHash: String) {
        if (navController.currentDestination!!.id in listOf(
                R.id.torrentOverviewFragment,
                R.id.torrentFilesFragment
            )
        ) {
            navController.navigateUp()
        }
        if (navController.currentDestination?.id != R.id.torrentPreferenceFragment)
            navController.navigate(R.id.torrentPreferenceFragment, bundleOf("infohash" to infoHash))
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

}
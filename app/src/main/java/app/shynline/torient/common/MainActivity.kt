package app.shynline.torient.common

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import app.shynline.torient.R
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.service.ActivityCycle
import app.shynline.torient.torrent.utils.Magnet
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.io.BufferedInputStream

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    data class LastTorrentArgs(val infoHash: String, val name: String)

    private val torrentController by inject<ActivityCycle>()
    private val torrentMediator by inject<TorrentMediator>()
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var lastTorrentArgs: LastTorrentArgs
    private lateinit var toolbar: Toolbar
    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }
    private val pageNavigationHelper by lazy {
        PageNavigationHelper(navController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupToolbar()
        handleIntent()


        bottomNavigation = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.torrentOverviewFragment -> pageNavigationHelper.showTorrentOverView(
                    lastTorrentArgs.infoHash,
                    lastTorrentArgs.name
                )
                R.id.torrentFilesFragment -> pageNavigationHelper.showTorrentFiles(
                    lastTorrentArgs.infoHash,
                    lastTorrentArgs.name
                )
                R.id.torrentPreferenceFragment -> pageNavigationHelper.showTorrentPreference(
                    lastTorrentArgs.infoHash,
                    lastTorrentArgs.name
                )
            }
            true
        }
        navController.addOnDestinationChangedListener(this)
    }

    private fun setupToolbar() {
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = "Torient"
        NavigationUI.setupWithNavController(toolbar, navController)
        toolbar.inflateMenu(R.menu.main_menu)
        toolbar.setOnMenuItemClickListener {
            lifecycleScope.launch {
                when (it.itemId) {
                    R.id.main_menu_preference -> {
                        pageNavigationHelper.showPreference()
                    }
                    R.id.main_menu_about -> {
                        pageNavigationHelper.showAbout()
                    }
                }
            }
            true
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id in listOf(
                R.id.torrentOverviewFragment,
                R.id.torrentFilesFragment, R.id.torrentPreferenceFragment
            )
        ) {
            if (arguments?.containsKey("infohash") == true) {
                lastTorrentArgs = LastTorrentArgs(
                    infoHash = arguments.getString("infohash")!!,
                    name = arguments.getString("name")!!
                )
                bottomNavigation.menu.getItem(0).isChecked = true
            }
            bottomNavigation.visibility = View.VISIBLE
        } else {
            bottomNavigation.visibility = View.GONE
        }
        updateToolbar(destination.id)

    }

    private fun updateToolbar(destination: Int) {
        when (destination) {
            R.id.torrentOverviewFragment,
            R.id.torrentFilesFragment,
            R.id.torrentPreferenceFragment -> {
                var title = lastTorrentArgs.name
                if (title.length > 30) {
                    title = title.substring(0, 25) + "..."
                }
                toolbar.title = title
                toolbar.menu.findItem(R.id.main_menu_preference).isVisible = false
                toolbar.menu.findItem(R.id.main_menu_about).isVisible = false
            }
            R.id.aboutFragment -> {
                toolbar.menu.findItem(R.id.main_menu_preference).isVisible = false
                toolbar.menu.findItem(R.id.main_menu_about).isVisible = false
                toolbar.title = "About"
            }
            R.id.preferenceFragment -> {
                toolbar.menu.findItem(R.id.main_menu_preference).isVisible = false
                toolbar.menu.findItem(R.id.main_menu_about).isVisible = false
                toolbar.title = "Preference"
            }
            R.id.torrent_list_fragment -> {
                toolbar.title = getString(R.string.app_name)
                toolbar.menu.findItem(R.id.main_menu_about).isVisible = true
                toolbar.menu.findItem(R.id.main_menu_preference).isVisible = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        navController.removeOnDestinationChangedListener(this)
    }

    override fun onStart() {
        super.onStart()
        torrentController.onActivityStart()
    }


    override fun onStop() {
        super.onStop()
        torrentController.onActivityStop()
    }

    private fun handleIntent() {
        if (intent.type == "application/x-bittorrent" ||
            intent.type?.startsWith("text/") == true
        ) { // torrent file handling

            intent.data?.let { uri ->
                GlobalScope.launch {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val data = BufferedInputStream(inputStream).readBytes()
                        torrentMediator.getTorrentModel(torrentFile = data)?.let { torrentDetail ->
                            pageNavigationHelper.showNewTorrentDialog(torrentDetail.infoHash)
                        }
                    }
                }
            }
            return
        }

        // magnet link
        intent.dataString?.let {
            if (it.startsWith("magnet")) {
                val magnet = Magnet.parse(it)
                magnet?.let { mg ->
                    if (torrentMediator.isTorrentFileCached(mg.infoHash!!)) {
                        pageNavigationHelper.showNewTorrentDialog(mg.infoHash!!)
                    } else {
                        pageNavigationHelper.showNewTorrentDialogByMagnet(it)
                    }
                }
            }
        }
    }
}

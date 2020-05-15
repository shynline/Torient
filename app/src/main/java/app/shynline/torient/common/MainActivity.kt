package app.shynline.torient.common

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import app.shynline.torient.R
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.torrent.Torrent
import app.shynline.torient.torrent.torrent.TorrentController
import app.shynline.torient.torrent.utils.Magnet
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.io.BufferedInputStream

class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private lateinit var torrentController: TorrentController
    private val torrentMediator by inject<TorrentMediator>()
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var lastTorrentInfoHash: String
    private val navController by lazy {
        findNavController(R.id.nav_host_fragment)
    }
    private val pageNavigationHelper by lazy {
        PageNavigationHelper(navController)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        torrentController = get<Torrent>() as TorrentController
        handleIntent()


        bottomNavigation = findViewById(R.id.bottomNavigationView)
        bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.torrentOverviewFragment -> pageNavigationHelper.showTorrentOverView(
                    lastTorrentInfoHash
                )
                R.id.torrentFilesFragment -> pageNavigationHelper.showTorrentFiles(
                    lastTorrentInfoHash
                )
                R.id.torrentPreferenceFragment -> pageNavigationHelper.showTorrentPreference(
                    lastTorrentInfoHash
                )
            }
            true
        }
        navController.addOnDestinationChangedListener(this)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        if (destination.id in listOf(
                R.id.torrentOverviewFragment,
                R.id.torrentFilesFragment,
                R.id.torrentPreferenceFragment
            )
        ) {
            if (arguments?.containsKey("infohash") == true) {
                lastTorrentInfoHash = arguments.getString("infohash")!!
                bottomNavigation.menu.getItem(0).isChecked = true
            }
            bottomNavigation.visibility = View.VISIBLE
        } else {
            bottomNavigation.visibility = View.GONE
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

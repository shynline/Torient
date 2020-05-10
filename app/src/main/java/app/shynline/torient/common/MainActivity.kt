package app.shynline.torient.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import app.shynline.torient.R
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import app.shynline.torient.torrent.torrent.Torrent
import app.shynline.torient.torrent.torrent.TorrentController
import app.shynline.torient.torrent.utils.Magnet
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import java.io.BufferedInputStream

class MainActivity : AppCompatActivity() {

    private lateinit var torrentController: TorrentController
    private val torrentMediator by inject<TorrentMediator>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        torrentController = get<Torrent>() as TorrentController
        handleIntent()
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
                        torrentMediator.getTorrentDetail(torrentFile = data)?.let { torrentDetail ->
                            PageNavigationHelper(findNavController(R.id.nav_host_fragment))
                                .showNewTorrentDialog(torrentDetail.infoHash)
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
                        PageNavigationHelper(findNavController(R.id.nav_host_fragment))
                            .showNewTorrentDialog(mg.infoHash!!)
                    } else {
                        PageNavigationHelper(findNavController(R.id.nav_host_fragment))
                            .showNewTorrentDialogByMagnet(it)
                    }
                }
            }
        }
    }
}

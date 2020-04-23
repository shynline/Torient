package app.shynline.torient.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.shynline.torient.R
import app.shynline.torient.torrent.torrent.Torrent
import app.shynline.torient.torrent.torrent.TorrentController
import org.koin.android.ext.android.get

class MainActivity : AppCompatActivity() {

    private lateinit var torrentController: TorrentController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        torrentController = get<Torrent>() as TorrentController
    }

    override fun onStart() {
        super.onStart()
        torrentController.onActivityStart()
    }


    override fun onStop() {
        super.onStop()
        torrentController.onActivityStop()
    }
}

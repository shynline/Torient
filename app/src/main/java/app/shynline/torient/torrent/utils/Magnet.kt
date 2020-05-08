package app.shynline.torient.torrent.utils

import com.frostwire.jlibtorrent.TorrentInfo
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder


data class Magnet(
    var infoHash: String? = null,
    var name: String? = null,
    var announce: MutableList<URI>? = null
) {

    fun addAnnounce(uri: URI) {
        if (announce == null) announce = ArrayList()
        announce!!.add(uri)
    }

    fun write(): String {
        var magnet = ("magnet:?xt=urn:btih:" + infoHash
                + "&dn=" + URLEncoder.encode(name, "UTF-8"))
        for (uri in announce!!) {
            magnet += "&tr=" + URLEncoder.encode(uri.toString(), "UTF-8")
        }
        return magnet
    }

    companion object {
        fun parse(magnet: String): Magnet? {
            var magnetUri = magnet
            if (!magnetUri.startsWith("magnet:?")) return null
            magnetUri = magnetUri.substring(8)
            val args = magnetUri.split("&").toTypedArray()
            val m = Magnet()
            for (arg in args) {
                when {
                    arg.startsWith("xt=urn:btih:") -> {
                        m.infoHash = arg.substring(12)
                    }
                    arg.startsWith("dn=") -> {
                        m.name = URLDecoder.decode(arg.substring(3), "UTF-8")
                    }
                    arg.startsWith("tr=") -> {
                        m.addAnnounce(URI(URLDecoder.decode(arg.substring(3), "UTF-8")))
                    }
                }
            }
            return m
        }

        fun load(torrent: TorrentInfo): Magnet {
            val m = Magnet()
            m.infoHash = torrent.infoHash().toHex()
            m.name = torrent.name()
            for (entry in torrent.trackers()) {
                m.addAnnounce(URI(URLDecoder.decode(entry.url(), "UTF-8")))
            }
            return m
        }
    }
}
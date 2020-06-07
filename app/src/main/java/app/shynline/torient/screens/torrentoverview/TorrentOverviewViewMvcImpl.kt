package app.shynline.torient.screens.torrentoverview

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import app.shynline.torient.R
import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.models.TorrentOverview
import app.shynline.torient.screens.common.view.BaseObservableViewMvc
import app.shynline.torient.utils.toByteRepresentation
import java.util.*

class TorrentOverviewViewMvcImpl(
    inflater: LayoutInflater,
    parent: ViewGroup?
) : BaseObservableViewMvc<TorrentOverviewViewMvc.Listener>(), TorrentOverviewViewMvc {
    private val infoHashTv: TextView
    private val sizeTv: TextView
    private val haveTv: TextView
    private val stateTv: TextView
    private val nameTv: TextView
    private val creatorTv: TextView
    private val commentTv: TextView
    private val createdDateTv: TextView
    private val privacyTv: TextView
    private val lastSeenCompleteTv: TextView

    init {
        setRootView(inflater.inflate(R.layout.fragment_torrent_overview, parent, false))
        infoHashTv = findViewById(R.id.torrentInfoHash)
        sizeTv = findViewById(R.id.torrentSize)
        haveTv = findViewById(R.id.torrentHave)
        stateTv = findViewById(R.id.torrentState)
        nameTv = findViewById(R.id.torrentName)
        creatorTv = findViewById(R.id.torrentCreator)
        commentTv = findViewById(R.id.torrentComment)
        createdDateTv = findViewById(R.id.torrentCreatedDate)
        privacyTv = findViewById(R.id.torrentPrivacy)
        lastSeenCompleteTv = findViewById(R.id.torrentLastSeenComplete)
    }

    override fun updateUi(torrentOverview: TorrentOverview) {
        infoHashTv.text = torrentOverview.infoHash
        stateTv.text = if (torrentOverview.userState == TorrentUserState.ACTIVE) {
            "Active"
        } else {
            "Paused"
        }
        val pieces = if (torrentOverview.numPiece > 1) "s" else ""
        sizeTv.text =
            "${torrentOverview.size.toByteRepresentation()} (${torrentOverview.numPiece} " +
                    "piece$pieces) @ ${torrentOverview.pieceLength.toByteRepresentation()}"

        haveTv.text = "${(torrentOverview.size * torrentOverview.progress).toLong()
            .toByteRepresentation()} " +
                "(${(torrentOverview.progress * 100).toInt()}%)"
        nameTv.text = torrentOverview.name
        creatorTv.text = torrentOverview.creator
        commentTv.text = torrentOverview.comment
        privacyTv.text = if (torrentOverview.private) {
            "Private"
        } else {
            "Public"
        }
        createdDateTv.text =
            if (torrentOverview.createdDate == 0L) "" else Date(torrentOverview.createdDate).toString()
        lastSeenCompleteTv.text =
            if (torrentOverview.lastSeenComplete == 0L) "" else Date(torrentOverview.lastSeenComplete).toString()

    }
}

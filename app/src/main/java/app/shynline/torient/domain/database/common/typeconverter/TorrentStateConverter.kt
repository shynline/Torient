package app.shynline.torient.domain.database.common.typeconverter

import androidx.room.TypeConverter
import app.shynline.torient.domain.database.common.states.TorrentUserState
import java.util.*


class TorrentStateConverter {
    @TypeConverter
    fun toTorrentState(id: Int): TorrentUserState {
        TorrentUserState.values().forEach {
            if (id == it.id) {
                return it
            }
        }
        throw NoSuchElementException("Invalid enum id.")
    }

    @TypeConverter
    fun toInteger(userState: TorrentUserState): Int {
        return userState.id
    }
}
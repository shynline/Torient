package app.shynline.torient.database.typeconverter

import androidx.room.TypeConverter
import app.shynline.torient.database.TorrentState
import java.util.*


class TorrentStateConverter {
    @TypeConverter
    fun toTorrentState(id: Int): TorrentState {
        TorrentState.values().forEach {
            if (id == it.id) {
                return it
            }
        }
        throw NoSuchElementException("Invalid enum id.")
    }

    @TypeConverter
    fun toInteger(state: TorrentState): Int {
        return state.id
    }
}
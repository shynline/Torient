package app.shynline.torient.database.typeconverter

import androidx.room.TypeConverter
import app.shynline.torient.database.TorrentUserState
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
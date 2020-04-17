package app.shynline.torient.torrent.bencoding

import com.google.common.truth.Truth.assertThat
import dataset.BDictSamples
import org.junit.Test

class BDictTest {
    @Test
    fun encode_bencoded() {
        var bi: BDict
        BDictSamples.data.forEach {
            bi = BDict(bencoded = it.first)
            assertThat(it.first).isEqualTo(bi.encode())
        }
    }

    @Test
    fun encode_rawValue() {
        var bi: BDict
        BDictSamples.data.forEach {
            bi = BDict(item = it.second)
            assertThat(it.first).isEqualTo(bi.encode())
        }
    }

    @Test
    fun decode_bencoded() {
        var bi: BDict
        BDictSamples.data.forEach {
            bi = BDict(item = it.second)
            if (it.second.isNotEmpty())
                assertThat(it.second).containsExactlyEntriesIn(bi.value())
            else
                assertThat(it.second.size).isEqualTo(bi.value().size)
        }
    }

    @Test
    fun decode_rawValue() {
        var bi: BDict
        BDictSamples.data.forEach {
            bi = BDict(bencoded = it.first)
            if (it.second.isNotEmpty()) {
                assertThat(it.second).containsExactlyEntriesIn(bi.value())
            } else
                assertThat(it.second.size).isEqualTo(bi.value().size)
        }
    }
}
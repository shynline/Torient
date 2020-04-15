package app.shynline.torient.torrent.bencoding

import com.google.common.truth.Truth
import dataset.BListSamples
import org.junit.Test

class BListTest {
    @Test
    fun encode_bencoded() {
        var bi: BList
        BListSamples.data.forEach {
            bi = BList(bencoded = it.first)
            Truth.assertThat(it.first).matches(bi.encode())
        }
    }

    @Test
    fun encode_rawValue() {
        var bi: BList
        BListSamples.data.forEach {
            bi = BList(item = it.second)
            Truth.assertThat(it.first).matches(bi.encode())
        }
    }

    @Test
    fun decode_bencoded() {
        var bi: BList
        BListSamples.data.forEach {
            bi = BList(item = it.second)
            if (it.second.isNotEmpty())
                Truth.assertThat(it.second).containsAnyIn(bi.value())
            else
                Truth.assertThat(it.second.size).isEqualTo(bi.value().size)
        }
    }

    @Test
    fun decode_rawValue() {
        var bi: BList
        BListSamples.data.forEach {
            bi = BList(bencoded = it.first)
            if (it.second.isNotEmpty())
                Truth.assertThat(it.second).containsAnyIn(bi.value())
            else
                Truth.assertThat(it.second.size).isEqualTo(bi.value().size)
        }
    }
}
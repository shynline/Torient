package app.shynline.torient.torrent.bencoding

import com.google.common.truth.Truth
import dataset.BStringSamples
import org.junit.Test

class BStringTest {
    @Test
    fun encode_bencoded() {
        var bi: BString
        BStringSamples.data.forEach {
            bi = BString(bencoded = it.first)
            Truth.assertThat(it.first).matches(bi.encode())
        }
    }

    @Test
    fun encode_rawValue() {
        var bi: BString
        BStringSamples.data.forEach {
            bi = BString(item = it.second)
            Truth.assertThat(it.first).matches(bi.encode())
        }
    }

    @Test
    fun decode_bencoded() {
        var bi: BString
        BStringSamples.data.forEach {
            bi = BString(item = it.second)
            Truth.assertThat(it.second).isEqualTo(bi.value())
        }
    }

    @Test
    fun decode_rawValue() {
        var bi: BString
        BStringSamples.data.forEach {
            bi = BString(bencoded = it.first)
            Truth.assertThat(it.second).isEqualTo(bi.value())
        }
    }
}
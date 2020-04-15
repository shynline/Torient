package app.shynline.torient.torrent.bencoding

import com.google.common.truth.Truth.assertThat
import dataset.BIntegerSamples
import org.junit.Test


class BIntegerTest {

    @Test
    fun encode_bencoded() {
        var bi: BInteger
        BIntegerSamples.data.forEach {
            bi = BInteger(bencoded = it.first)
            assertThat(it.first).matches(bi.encode())
        }
    }

    @Test
    fun encode_rawValue() {
        var bi: BInteger
        BIntegerSamples.data.forEach {
            bi = BInteger(item = it.second)
            assertThat(it.first).matches(bi.encode())
        }
    }

    @Test
    fun decode_bencoded() {
        var bi: BInteger
        BIntegerSamples.data.forEach {
            bi = BInteger(item = it.second)
            assertThat(it.second).isEqualTo(bi.value())
        }
    }

    @Test
    fun decode_rawValue() {
        var bi: BInteger
        BIntegerSamples.data.forEach {
            bi = BInteger(bencoded = it.first)
            assertThat(it.second).isEqualTo(bi.value())
        }
    }
}
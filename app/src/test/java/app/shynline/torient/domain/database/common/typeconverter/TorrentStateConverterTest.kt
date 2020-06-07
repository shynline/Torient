package app.shynline.torient.domain.database.common.typeconverter

import app.shynline.torient.domain.database.common.states.TorrentUserState
import com.google.common.truth.Truth
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class TorrentStateConverterTest {

    private lateinit var SUT: TorrentStateConverter

    @Before
    fun setup() {
        SUT = TorrentStateConverter()
    }

    @Test
    fun test_toInteger_returnsTheIdOfEnum() {
        val result = SUT.toInteger(TorrentUserState.PAUSED)
        Truth.assertThat(result).isEqualTo(TorrentUserState.PAUSED.id)
    }


    @Test
    fun test_toTorrentState_returnsTheEnumValue() {
        val result = SUT.toTorrentState(TorrentUserState.PAUSED.id)
        Truth.assertThat(result).isEqualTo(TorrentUserState.PAUSED)
    }

    @Test
    fun test_toTorrentState_wrongIdThrowsException() {
        try {
            SUT.toTorrentState(666)
            fail("Should have thrown NoSuchElement exception");
        } catch (e: NoSuchElementException) {
            // Succeed
        }

    }
}
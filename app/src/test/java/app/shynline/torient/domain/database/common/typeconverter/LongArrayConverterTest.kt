package app.shynline.torient.domain.database.common.typeconverter

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test


class LongArrayConverterTest {

    private lateinit var SUT: LongArrayConverter

    @Before
    fun setup() {
        SUT = LongArrayConverter
    }

    @Test
    fun test_toLongArray_returnsNullWhenParameterIsNull() {
        val result = SUT.toLongArray(null)
        assertThat(result).isNull()
    }

    @Test
    fun test_toString_returnsPredictedResult() {
        val result = SUT.toString(sampleArray)
        assertThat(result).isEqualTo(sampleString)
    }

    @Test
    fun test_toLongArray_returnsPredictedResult() {
        val result = SUT.toLongArray(sampleString)
        assertThat(result).isEqualTo(sampleArray)
    }

    @Test
    fun test_toString_returnsNullWhenParameterIsNull() {
        val result = SUT.toString(null)
        assertThat(result).isNull()
    }


    private val sampleArray = listOf(1L, 300L, 1444L)
    private val sampleString = "1,300,1444"
}
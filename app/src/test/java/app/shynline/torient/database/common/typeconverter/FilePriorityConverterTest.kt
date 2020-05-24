package app.shynline.torient.database.common.typeconverter

import app.shynline.torient.model.FilePriority
import app.shynline.torient.model.TorrentFilePriority
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test

class FilePriorityConverterTest {

    private lateinit var SUT: FilePriorityConverter

    @Before
    fun setup() {
        SUT = FilePriorityConverter()
    }

    @Test
    fun test_toFilePriorityArray_providingNullShouldReturnNull() {
        val result = SUT.toFilePriorityArray(null)
        assertThat(result).isNull()
    }

    @Test
    fun test_toFilePriorityArray_ShouldConvertToExpectedString() {
        val result1 = SUT.toFilePriorityArray(sample1FilePriorityString)
        val result2 = SUT.toFilePriorityArray(sample2FilePriorityString)
        assertThat(result1).isEqualTo(sample1FilePriorityList)
        assertThat(result2).isEqualTo(sample2FilePriorityList)
    }

    @Test
    fun test_toString_providingNullShouldReturnNull() {
        val result = SUT.toString(null)
        assertThat(result).isNull()
    }

    @Test
    fun test_toString_ShouldConvertToExpectedTorrentFilePriority() {
        val result1 = SUT.toString(sample1FilePriorityList)
        val result2 = SUT.toString(sample2FilePriorityList)
        assertThat(result1).isEqualTo(sample1FilePriorityString)
        assertThat(result2).isEqualTo(sample2FilePriorityString)
    }


    private val sample1FilePriorityList = listOf(
        TorrentFilePriority(
            active = true,
            priority = FilePriority.NORMAL
        )
    )
    private val sample1FilePriorityString = "1|${FilePriority.NORMAL.id}"
    private val sample2FilePriorityList = listOf(
        TorrentFilePriority(
            active = true,
            priority = FilePriority.NORMAL
        ),
        TorrentFilePriority(
            active = false,
            priority = FilePriority.HIGH
        )
    )
    private val sample2FilePriorityString = "1|${FilePriority.NORMAL.id},0|${FilePriority.HIGH.id}"
}
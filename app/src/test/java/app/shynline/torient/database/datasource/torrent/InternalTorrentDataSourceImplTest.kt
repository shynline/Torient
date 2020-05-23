package app.shynline.torient.database.datasource.torrent

import app.shynline.torient.database.TorrentDao
import app.shynline.torient.database.typeconverter.LongArrayConverter
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.util.*

class InternalTorrentDataSourceImplTest {

    private lateinit var SUT: InternalTorrentDataSource
    private val torrentDao = mockk<TorrentDao>()

    @Before
    fun setUp() {
        SUT = InternalTorrentDataSourceImpl(torrentDao, Dispatchers.Unconfined)
    }

    @Test
    fun test_setTorrentProgress_shouldCallSetTorrentProgressAndSetTorrentFileProgressFromDao() =
        runBlocking {
            // Arrange
            coEvery { torrentDao.setTorrentProgress(any(), any(), any()) }.returns(Unit)
            coEvery { torrentDao.setTorrentFileProgress(any(), any()) }.returns(Unit)
            // Act
            SUT.setTorrentProgress(INFO_HASH, PROGRESS, LAST_SEEN_COMPLETE, FILE_PROGRESS)
            // Assert
            coVerify(exactly = 1) {
                torrentDao.setTorrentProgress(
                    INFO_HASH,
                    PROGRESS,
                    LAST_SEEN_COMPLETE
                )
            }
            coVerify(exactly = 1) {
                torrentDao.setTorrentFileProgress(
                    INFO_HASH,
                    LongArrayConverter.toString(FILE_PROGRESS.toList())!!
                )
            }
            confirmVerified(torrentDao)
        }

    @Test
    fun test_setTorrentFinished_shouldCallSetTorrentFinishedAndSetTorrentFileProgressFromDao() =
        runBlocking {
            // Arrange
            coEvery {
                torrentDao.setTorrentFinished(
                    infoHash = any(),
                    finished = any()
                )
            }.returns(Unit)
            coEvery {
                torrentDao.setTorrentFileProgress(
                    infoHash = any(),
                    fileProgress = any()
                )
            }.returns(Unit)
            // Act
            SUT.setTorrentFinished(INFO_HASH, FINISHED, FILE_PROGRESS)
            // Assert
            coVerify(exactly = 1) {
                torrentDao.setTorrentFinished(
                    infoHash = INFO_HASH,
                    finished = FINISHED
                )
            }
            coVerify(exactly = 1) {
                torrentDao.setTorrentFileProgress(
                    infoHash = INFO_HASH,
                    fileProgress = LongArrayConverter.toString(FILE_PROGRESS.toList())!!
                )
            }
            confirmVerified(torrentDao)
        }


    companion object {
        private const val INFO_HASH = "infohash"
        private const val FINISHED = true
        private const val PROGRESS = 87f
        private val LAST_SEEN_COMPLETE = Date().time
        private val FILE_PROGRESS = longArrayOf(2, 300, 5, 3500)
    }
}
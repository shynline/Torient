package app.shynline.torient.domain.database.datasource.torrent

import app.shynline.torient.domain.database.common.typeconverter.LongArrayConverter
import app.shynline.torient.domain.database.dao.TorrentDao
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
            coEvery { torrentDao.setTorrentProgress(any(), any()) }.returns(Unit)
            coEvery { torrentDao.setTorrentLastSeenComplete(any(), any()) }.returns(Unit)
            coEvery { torrentDao.setTorrentFileProgress(any(), any()) }.returns(Unit)
            // Act
            SUT.setTorrentProgress(INFO_HASH, PROGRESS, LAST_SEEN_COMPLETE, FILE_PROGRESS)
            // Assert
            coVerify(exactly = 1) {
                torrentDao.setTorrentProgress(
                    INFO_HASH,
                    PROGRESS
                )
            }
            coVerify(exactly = 1) {
                torrentDao.setTorrentLastSeenComplete(
                    INFO_HASH,
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
                torrentDao.setTorrentProgress(
                    infoHash = any(),
                    progress = any()
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
                torrentDao.setTorrentProgress(
                    infoHash = INFO_HASH,
                    progress = 100f
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
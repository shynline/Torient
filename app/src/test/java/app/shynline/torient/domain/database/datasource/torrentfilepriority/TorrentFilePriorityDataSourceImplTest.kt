package app.shynline.torient.domain.database.datasource.torrentfilepriority

import app.shynline.torient.domain.database.dao.TorrentFilePriorityDao
import com.google.common.truth.Truth.assertThat
import dataset.TorrentFilePrioritySchemaUtils
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class TorrentFilePriorityDataSourceImplTest {

    private lateinit var SUT: TorrentFilePriorityDataSource
    private val torrentFilePriorityDao = mockk<TorrentFilePriorityDao>()

    @Before
    fun setUp() {
        SUT = TorrentFilePriorityDataSourceImpl(torrentFilePriorityDao, Dispatchers.Unconfined)
    }

    @Test
    fun test_setPriority_onlyCallSetTorrentPrioritiesFromDaoIfSchemaExists() = runBlocking {
        // Arrange
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        coEvery { torrentFilePriorityDao.setTorrentFilePriorities(any(), any()) }.returns(1)
        // Act
        SUT.setPriority(sample)
        // Assert
        coVerify(exactly = 1) {
            torrentFilePriorityDao.setTorrentFilePriorities(
                sample.infoHash,
                sample.filePriority
            )
        }
        confirmVerified(torrentFilePriorityDao)
    }

    @Test
    fun test_setPriority_callSetTorrentPrioritiesFromDaoAndInsertSchemaIfNotExists() = runBlocking {
        // Arrange
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        coEvery { torrentFilePriorityDao.setTorrentFilePriorities(any(), any()) }.returns(0)
        coEvery { torrentFilePriorityDao.insertTorrent(any()) }.returns(Unit)
        // Act
        SUT.setPriority(sample)
        // Assert
        coVerify(exactly = 1) {
            torrentFilePriorityDao.setTorrentFilePriorities(
                sample.infoHash,
                sample.filePriority
            )
        }
        coVerify(exactly = 1) { torrentFilePriorityDao.insertTorrent(sample) }
        confirmVerified(torrentFilePriorityDao)
    }

    @Test
    fun test_removeTorrentFilePriority() = runBlocking {
        // Arrange
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        coEvery { torrentFilePriorityDao.removeTorrentFilePriority(any()) }.returns(Unit)
        // Act
        SUT.removeTorrentFilePriority(sample.infoHash)
        // Assert
        coVerify(exactly = 1) { torrentFilePriorityDao.removeTorrentFilePriority(sample.infoHash) }
        confirmVerified(torrentFilePriorityDao)
    }

    @Test
    fun test_getPriorities_returnsSchemaIfExists() = runBlocking {
        // Arrange
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        coEvery { torrentFilePriorityDao.getTorrentFilePrioritySchema(any()) }.returns(sample)
        // Act
        val result = SUT.getPriority(sample.infoHash)
        // Assert
        coVerify(exactly = 1) { torrentFilePriorityDao.getTorrentFilePrioritySchema(sample.infoHash) }
        confirmVerified(torrentFilePriorityDao)
        assertThat(result).isEqualTo(sample)
    }

    @Test
    fun test_getPriorities_returnsSchemaInsertIfNotExists() = runBlocking {
        // Arrange
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        coEvery { torrentFilePriorityDao.getTorrentFilePrioritySchema(any()) }.returns(null)
        coEvery { torrentFilePriorityDao.insertTorrent(any()) }.returns(Unit)
        // Act
        val result = SUT.getPriority(sample.infoHash)
        // Assert
        coVerify(exactly = 1) { torrentFilePriorityDao.getTorrentFilePrioritySchema(sample.infoHash) }
        coVerify(exactly = 1) { torrentFilePriorityDao.insertTorrent(any()) }
        confirmVerified(torrentFilePriorityDao)
        assertThat(result.infoHash).isEqualTo(sample.infoHash)
        assertThat(result.filePriority).isNull()
    }
}
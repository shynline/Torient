package app.shynline.torient.domain.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.shynline.torient.domain.database.TorrentDataBase
import com.google.common.truth.Truth.assertThat
import dataset.TorrentFilePrioritySchemaUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TorrentFilePriorityDaoTest {

    private lateinit var database: TorrentDataBase
    private lateinit var SUT: TorrentFilePriorityDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TorrentDataBase::class.java).build()
        SUT = database.torrentFilePriorityDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun test_insertTorrent_insertAndGetTheSameSchema() = runBlocking {
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        SUT.insertTorrent(sample)
        val schema = SUT.getTorrentFilePrioritySchema(sample.infoHash)
        assertThat(schema).isNotNull()
        assertThat(schema).isEqualTo(sample)
    }

    @Test
    fun test_removeTorrentFilePriority_returnSchemaAfterDeletionShouldBeNull() = runBlocking {
        val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
        SUT.insertTorrent(sample)
        SUT.removeTorrentFilePriority(sample.infoHash)
        val schema = SUT.getTorrentFilePrioritySchema(sample.infoHash)
        assertThat(schema).isNull()
    }

    @Test
    fun test_setTorrentFilePriorities_schemaWithNewTorrentPrioritiesShouldBeReturned() =
        runBlocking {
            val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
            SUT.insertTorrent(sample)
            val newPriority = TorrentFilePrioritySchemaUtils.getARandomFilePriorityList()
            val result = SUT.setTorrentFilePriorities(sample.infoHash, newPriority)
            val schema = SUT.getTorrentFilePrioritySchema(sample.infoHash)!!
            assertThat(result).isEqualTo(1)
            assertThat(schema.filePriority).isEqualTo(newPriority)
        }

    @Test
    fun test_setTorrentFilePriorities_returnsZeroWhenThereIsNoSchemaInDataBase() =
        runBlocking {
            val sample = TorrentFilePrioritySchemaUtils.getFilePrioritySchema()
            val newPriority = TorrentFilePrioritySchemaUtils.getARandomFilePriorityList()
            val result = SUT.setTorrentFilePriorities(sample.infoHash, newPriority)
            assertThat(result).isEqualTo(0)
        }

}
package app.shynline.torient.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.shynline.torient.database.TorrentDataBase
import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.common.typeconverter.LongArrayConverter
import com.google.common.truth.Truth.assertThat
import dataset.TorrentSchemaUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@RunWith(AndroidJUnit4::class)
class TorrentDaoTest {

    private lateinit var dataBase: TorrentDataBase

    private lateinit var SUT: TorrentDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        dataBase = Room.inMemoryDatabaseBuilder(context, TorrentDataBase::class.java).build()
        SUT = dataBase.torrentDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        dataBase.close()
    }

    @Test
    fun test_insertSchema_successfullyGettingTheSchema() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        SUT.insertTorrent(sample)
        val torrentSchema = SUT.getTorrent(sample.infoHash)
        assertThat(torrentSchema).isNotNull()
        assertThat(torrentSchema).isEqualTo(sample)
    }

    @Test
    fun test_deleteAllTorrents_torrentsShouldBeRemoved() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        SUT.insertTorrent(sample)
        SUT.deleteAllTorrents()
        val torrentSchema = SUT.getTorrent(sample.infoHash)
        assertThat(torrentSchema).isNull()
    }

    @Test
    fun test_removeTorrent_torrentShouldBeRemoved() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        SUT.insertTorrent(sample)
        SUT.removeTorrent(sample.infoHash)
        val torrentSchema = SUT.getTorrent(sample.infoHash)
        assertThat(torrentSchema).isNull()
    }

    @Test
    fun test_getTorrentState_stateShouldBeReturned() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        SUT.insertTorrent(sample)
        val state = SUT.getTorrentState(sample.infoHash)
        assertThat(state).isEqualTo(sample.userState)
    }

    @Test
    fun test_setTorrentFileProgress_fileProgressShouldChangeInOrder() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        SUT.insertTorrent(sample)
        val random = Random()
        val fileProgress = List(10) { random.nextLong() }
        SUT.setTorrentFileProgress(sample.infoHash, LongArrayConverter.toString(fileProgress)!!)
        val torrentSchema = SUT.getTorrent(sample.infoHash)!!
        assertThat(torrentSchema.fileProgress).containsExactlyElementsIn(fileProgress).inOrder()
    }

    @Test
    fun test_setTorrentFinished_torrentFinishedFlagShouldBeTrue() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        sample.isFinished = false
        SUT.insertTorrent(sample)
        SUT.setTorrentFinished(sample.infoHash, true)
        val torrentSchema = SUT.getTorrent(sample.infoHash)!!
        assertThat(torrentSchema.isFinished).isTrue()
    }

    @Test
    fun test_setTorrentState_torrentStateShouldChange() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        sample.userState = TorrentUserState.ACTIVE
        SUT.insertTorrent(sample)
        SUT.setTorrentState(sample.infoHash, TorrentUserState.PAUSED)
        val torrentSchema = SUT.getTorrent(sample.infoHash)!!
        assertThat(torrentSchema.userState).isEqualTo(TorrentUserState.PAUSED)
    }

    @Test
    fun test_setTorrentProgress_BothProgressAndLastSeenCompleteShouldBeUpdated() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        sample.progress = 80f
        SUT.insertTorrent(sample)
        val lastSeen = Date().time + 100
        SUT.setTorrentProgress(sample.infoHash, 90f, lastSeen)
        val torrentSchema = SUT.getTorrent(sample.infoHash)!!
        assertThat(torrentSchema.progress).isEqualTo(90f)
        assertThat(torrentSchema.lastSeenComplete).isEqualTo(lastSeen)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun test_getTorrents_returnAllTorrents() = runBlocking {
        val sample = TorrentSchemaUtils.getSchema()
        SUT.insertTorrent(sample)
        val schemas = SUT.getTorrents().take(1).toList().first()
        assertThat(schemas.size).isEqualTo(1)
        assertThat(schemas[0]).isEqualTo(sample)
    }

}
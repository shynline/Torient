package app.shynline.torient.domain.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.shynline.torient.domain.database.TorrentDataBase
import com.google.common.truth.Truth.assertThat
import dataset.TorrentPreferenceUtils
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class TorrentPreferenceDaoTest {

    private lateinit var database: TorrentDataBase
    private lateinit var SUT: TorrentPreferenceDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, TorrentDataBase::class.java).build()
        SUT = database.torrentPreferenceDao()
    }

    @Throws(IOException::class)
    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun test_insertPreference_shouldSuccessfullySaveTheSchemaToDatabase() = runBlocking {
        // Arrange
        val sample = TorrentPreferenceUtils.getSchema()
        // Act
        SUT.insertPreference(sample)
        // Assert
        val schema = SUT.getPreference(sample.infoHash)
        assertThat(schema).isNotNull()
        assertThat(schema).isEqualTo(sample)
    }

    @Test
    fun test_getPreference_returnsNullIfNoSuchPreferenceExists() = runBlocking {
        // Arrange
        // Act
        val schema = SUT.getPreference("SOmeRandomINFOHASH")
        // Assert
        assertThat(schema).isNull()
    }

    @Test
    fun test_updatePreference_returnsNullIfNoSuchPreferenceExists() = runBlocking {
        // Arrange
        val sample = TorrentPreferenceUtils.getSchema()
        SUT.insertPreference(sample)
        // Act
        sample.downloadRateLimit = true
        sample.uploadRateLimit = false
        sample.uploadRate = 340
        val row = SUT.updateSchema(sample)
        // Assert
        assertThat(row).isEqualTo(1)
        val schema = SUT.getPreference(sample.infoHash)!!
        assertThat(schema).isEqualTo(sample)
    }
}
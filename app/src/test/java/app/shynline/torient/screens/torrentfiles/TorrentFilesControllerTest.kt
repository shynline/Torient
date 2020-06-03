package app.shynline.torient.screens.torrentfiles

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.entities.TorrentFilePrioritySchema
import app.shynline.torient.database.entities.TorrentSchema
import app.shynline.torient.domain.helper.timer.TimerController
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.torrent.mediator.usecases.GetTorrentFilePriorityUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentModelUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentSchemeUseCase
import app.shynline.torient.torrent.mediator.usecases.UpdateTorrentFilePriorityUseCase
import com.google.common.truth.Truth.assertThat
import dataset.TorrentModelUtils
import dataset.TorrentSchemaUtils
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.random.Random

class TorrentFilesControllerTest {

    // region dependencies

    private lateinit var timerController: TimerController
    private lateinit var viewMvc: TorrentFilesViewMvc
    private lateinit var getTorrentModelUseCase: GetTorrentModelUseCase
    private lateinit var fragmentRequestHelper: FragmentRequestHelper
    private lateinit var getTorrentSchemeUseCase: GetTorrentSchemeUseCase
    private lateinit var getTorrentFilePriorityUseCase: GetTorrentFilePriorityUseCase
    private lateinit var updateTorrentFilePriorityUseCase: UpdateTorrentFilePriorityUseCase

    // endregion dependencies

    private lateinit var sut: TorrentFilesController

    @Before
    fun setUp() {
        timerController = mockk()
        viewMvc = mockk()
        getTorrentModelUseCase = mockk()
        fragmentRequestHelper = mockk()
        getTorrentSchemeUseCase = mockk()
        getTorrentFilePriorityUseCase = mockk()
        updateTorrentFilePriorityUseCase = mockk()
        sut = TorrentFilesController(
            Dispatchers.Unconfined,
            getTorrentSchemeUseCase,
            updateTorrentFilePriorityUseCase,
            getTorrentFilePriorityUseCase,
            getTorrentModelUseCase,
            timerController
        )
        sut.onCreateView()
        sut.bind(viewMvc, fragmentRequestHelper)
    }

    @Test
    fun meta_data_not_exists_view_should_not_update() = runBlocking {
        // Arrange
        metaDataNotExists()
        nullFilePriority()
        every { viewMvc.showTorrent(any()) }.returns(Unit)

        // Act
        sut.setTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 0) { viewMvc.showTorrent(any()) }
    }

    @Test
    fun meta_data_exist_file_priority_is_null_view_should_not_be_updated() = runBlocking {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET).apply {
            numFiles = NUM_FILES
        }
        every { viewMvc.showTorrent(any()) }.returns(Unit)
        metaDataIsAvailable(model)
        nullFilePriority()

        // Act
        sut.setTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 0) { viewMvc.showTorrent(any()) }
    }

    @Test
    fun view_and_progress_and_priority_should_be_updated() = runBlocking {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
            .apply { numFiles = NUM_FILES }
        val torrentPriority =
            TorrentFilePrioritySchema(INFO_HASH, List(NUM_FILES) { TorrentFilePriority.default() })
        val random = Random(Date().time)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.ACTIVE).apply {
            fileProgress = List(NUM_FILES) { random.nextLong() }
        }
        notNullTorrentScheme(scheme)
        metaDataIsAvailable(model)
        viewMethodsSuccess()
        notNullFilePriority(torrentPriority)

        // Act
        sut.setTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 1) { viewMvc.showTorrent(model) }
        coVerify(exactly = 1) { viewMvc.loadState(any()) }
        coVerify(exactly = 1) { viewMvc.updateFilePriority(torrentPriority.filePriority!!) }
        coVerify(exactly = 1) { viewMvc.updateFileProgress(scheme.fileProgress!!) }

    }


    @Test
    fun on_start_register_to_view() {
        // Arrange
        every { viewMvc.registerListener(any()) }.returns(Unit)
        successTimerController()

        // Act
        sut.onStart()

        // Assert
        verify(exactly = 1) { viewMvc.registerListener(sut) }
        verify(exactly = 1) { timerController.schedule(any(), any(), any(), any()) }
        confirmVerified(viewMvc, timerController)
    }

    @Test
    fun on_stop_unregister_to_view() {
        // Arrange
        every { viewMvc.unRegisterListener(any()) }.returns(Unit)
        successTimerController()

        // Act
        sut.onStop()

        // Assert
        verify(exactly = 1) { viewMvc.unRegisterListener(sut) }
        verify(exactly = 1) { timerController.cancel(any()) }
        confirmVerified(viewMvc, timerController)
    }

    @Test
    fun on_periodic_task_meta_data_not_exists_view_should_not_update() = runBlocking {
        // Arrange
        setInfoHashForController()
        val periodicTask = capturePeriodicTask()
        metaDataNotExists()
        nullFilePriority()
        viewMethodsSuccess()

        // Act
        periodicTask.captured.invoke()

        // Assert
        coVerify(exactly = 0) { viewMvc.showTorrent(any()) }
        confirmVerified(viewMvc)
    }

    @Test
    fun on_periodic_task_meta_data_exists_file_progress_should_be_updated() = runBlocking {
        // Arrange
        setInfoHashForController()
        val periodicTask = capturePeriodicTask()
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
            .apply { numFiles = NUM_FILES }
        setupTorrentFiles()
        viewMethodsSuccess()
        val random = Random(Date().time)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.ACTIVE).apply {
            fileProgress = List(NUM_FILES) { random.nextLong() }
        }
        notNullTorrentScheme(scheme)

        // Act
        periodicTask.captured.invoke()

        // Assert
        coVerify(exactly = 1) { viewMvc.updateFileProgress(scheme.fileProgress!!) }
        confirmVerified(viewMvc)
    }

    @Test
    fun active_state_in_torrent_priority_have_to_change() = runBlocking {
        // Arrange
        val torrentPriority =
            TorrentFilePrioritySchema(INFO_HASH, List(NUM_FILES) { TorrentFilePriority.default() })
        setupTorrentFiles(torrentPriority = torrentPriority)
        successUpdateTorrentFilePriority()

        // Act
        sut.onDownloadCheckBoxClicked(66, true)
        sut.onDownloadCheckBoxClicked(55, false)
        sut.onDownloadCheckBoxClicked(0, false)

        // Assert
        assertThat(torrentPriority.filePriority!![66].active).isTrue()
        assertThat(torrentPriority.filePriority!![55].active).isFalse()
        assertThat(torrentPriority.filePriority!![0].active).isFalse()
    }

    @Test
    fun priority_should_change_and_view_should_be_updated() {
        // Arrange
        val torrentPriority =
            TorrentFilePrioritySchema(INFO_HASH, List(NUM_FILES) { TorrentFilePriority.default() })
        setupTorrentFiles(torrentPriority = torrentPriority)
        successUpdateTorrentFilePriority()
        viewMethodsSuccess()
        val lastP = torrentPriority.filePriority!![30].priority

        // Act
        sut.onPriorityClicked(30)

        // Assert
        coVerify(exactly = 1) { viewMvc.updateFilePriority(torrentPriority.filePriority!!) }
        assertThat(lastP).isNotEqualTo(torrentPriority.filePriority!![30].priority)
        confirmVerified(viewMvc)
    }

    @Test
    fun progress_is_not_complete_isFileCompleted_should_return_false() {
        // Arrange
        val periodicTask = capturePeriodicTask()
        val random = Random(Date().time)
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
            .apply {
                numFiles = NUM_FILES
                filesSize = List(NUM_FILES) { random.nextLong() + 1000 }
            }
        val schema = setupTorrentFiles(model = model)
        schema.fileProgress = schema.fileProgress!!.toMutableList().apply {
            set(40, model.filesSize!![40] - 1)
        }
        notNullTorrentScheme(schema)
        viewMethodsSuccess()
        periodicTask.captured.invoke()

        // Act
        val result = sut.isFileCompleted(40)

        // Assert
        assertThat(result).isFalse()
    }

    @Test
    fun progress_is_complete_isFileCompleted_should_return_true() {
        // Arrange
        val periodicTask = capturePeriodicTask()
        val random = Random(Date().time)
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
            .apply {
                numFiles = NUM_FILES
                filesSize = List(NUM_FILES) { random.nextLong() + 1000 }
            }
        val schema = setupTorrentFiles(model = model)
        schema.fileProgress = schema.fileProgress!!.toMutableList().apply {
            set(2, model.filesSize!![2])
        }
        notNullTorrentScheme(schema)
        viewMethodsSuccess()
        periodicTask.captured.invoke()

        // Act
        val result = sut.isFileCompleted(2)

        // Assert
        assertThat(result).isTrue()
    }

    @Test
    fun on_save_a_file_request_helper_must_be_called() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
            .apply {
                numFiles = NUM_FILES
                filesPath = List(NUM_FILES) { it.toString() }
            }
        setupTorrentFiles(model = model)
        coEvery { fragmentRequestHelper.saveToDownload(any(), any()) }.returns(Unit)

        // Act
        sut.saveFile(5)

        // Arrange
        coVerify(exactly = 1) { fragmentRequestHelper.saveToDownload(5.toString(), INFO_HASH) }
        confirmVerified(fragmentRequestHelper)
    }


    // region helper functions

    private fun setInfoHashForController() {
        metaDataNotExists()
        nullFilePriority()
        viewMethodsSuccess()
        sut.setTorrent(INFO_HASH)
        clearAllMocks()
    }

    private fun setupTorrentFiles(
        model: TorrentModel = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
            .apply { numFiles = NUM_FILES },
        torrentPriority: TorrentFilePrioritySchema =
            TorrentFilePrioritySchema(INFO_HASH, List(NUM_FILES) { TorrentFilePriority.default() })
    ): TorrentSchema {
        val random = Random(Date().time)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.ACTIVE).apply {
            fileProgress = List(NUM_FILES) { random.nextLong() }
        }
        notNullTorrentScheme(scheme)
        metaDataIsAvailable(model)
        viewMethodsSuccess()
        notNullFilePriority(torrentPriority)
        sut.setTorrent(INFO_HASH)
        clearAllMocks()
        return scheme
    }

    private fun metaDataNotExists() {
        coEvery { getTorrentModelUseCase.invoke(any()) }.returns(GetTorrentModelUseCase.Out(null))
    }

    private fun capturePeriodicTask(): CapturingSlot<() -> Unit> {
        val periodicTask = slot<() -> Unit>()
        viewMethodsSuccess()
        every { timerController.schedule(any(), any(), any(), capture(periodicTask)) }.returns(Unit)
        sut.onStart()
        clearAllMocks()
        return periodicTask
    }

    private fun successTimerController() {
        every { timerController.schedule(any(), any(), any(), any()) }.returns(Unit)
        every { timerController.cancel(any()) }.returns(Unit)
    }

    private fun successUpdateTorrentFilePriority() {
        coEvery { updateTorrentFilePriorityUseCase(any()) }.returns(UpdateTorrentFilePriorityUseCase.Out)
    }

    private fun notNullTorrentScheme(scheme: TorrentSchema) {
        coEvery { getTorrentSchemeUseCase(any()) }.returns(GetTorrentSchemeUseCase.Out(scheme))
    }


    private fun viewMethodsSuccess() {
        every { viewMvc.showTorrent(any()) }.returns(Unit)
        every { viewMvc.loadState(any()) }.returns(Unit)
        every { viewMvc.updateFilePriority(any()) }.returns(Unit)
        every { viewMvc.updateFileProgress(any()) }.returns(Unit)
        every { viewMvc.registerListener(any()) }.returns(Unit)
        every { viewMvc.unRegisterListener(any()) }.returns(Unit)
    }

    private fun nullFilePriority() {
        val torrentPriority = TorrentFilePrioritySchema(INFO_HASH, null)
        coEvery { getTorrentFilePriorityUseCase(any()) }.returns(
            GetTorrentFilePriorityUseCase.Out(
                torrentPriority
            )
        )
    }

    private fun notNullFilePriority(torrentPriority: TorrentFilePrioritySchema) =
        coEvery { getTorrentFilePriorityUseCase(any()) }.returns(
            GetTorrentFilePriorityUseCase.Out(
                torrentPriority
            )
        )

    private fun metaDataIsAvailable(model: TorrentModel) {
        coEvery { getTorrentModelUseCase.invoke(any()) }.returns(GetTorrentModelUseCase.Out(model))
    }

    // endregion


    @After
    fun tearDown() {
        sut.onViewDestroy()
    }

    companion object {
        private const val INFO_HASH = "6ca8b71b3dfc217fc2420b7b07a97117740f6f03"
        private const val NAME = "torrent_name"
        private const val NUM_FILES = 666
        private const val MAGNET = "magnet:?xt=urn:btih:${INFO_HASH}&dn=$NAME"
    }
}
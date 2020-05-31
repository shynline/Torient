package app.shynline.torient.screens.newtorrent

import app.shynline.torient.database.common.states.TorrentUserState
import app.shynline.torient.database.datasource.torrent.TorrentDataSource
import app.shynline.torient.database.datasource.torrentfilepriority.TorrentFilePriorityDataSource
import app.shynline.torient.model.TorrentFilePriority
import app.shynline.torient.model.TorrentModel
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.torrent.mediator.TorrentMediator
import dataset.TorrentFilePrioritySchemaUtils
import dataset.TorrentModelUtils
import dataset.TorrentSchemaUtils
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class NewTorrentControllerTest {

    private lateinit var torrentMediator: TorrentMediator
    private lateinit var torrentDataSource: TorrentDataSource
    private lateinit var torrentFilePriorityDataSource: TorrentFilePriorityDataSource
    private lateinit var viewMvc: NewTorrentViewMvc
    private lateinit var pageNavigationHelper: PageNavigationHelper
    private lateinit var fragmentRequestHelper: FragmentRequestHelper

    private lateinit var sut: NewTorrentController

    @Before
    fun setUp() {
        torrentMediator = mockk()
        torrentDataSource = mockk()
        torrentFilePriorityDataSource = mockk()
        viewMvc = mockk()
        pageNavigationHelper = mockk()
        fragmentRequestHelper = mockk()
        sut = NewTorrentController(
            Dispatchers.Unconfined,
            torrentMediator,
            torrentDataSource,
            torrentFilePriorityDataSource
        )
        sut.onCreateView()
        sut.bind(viewMvc, pageNavigationHelper, fragmentRequestHelper)
    }

    @Test
    fun navigate_back_when_torrent_model_does_not_exist() = runBlocking {
        // Arrange
        coEvery { torrentMediator.getTorrentModel(infoHash = any()) }.returns(null)
        pageNavigationBackSuccess()

        // Act
        sut.showTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 1) { torrentMediator.getTorrentModel(infoHash = INFO_HASH) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }

        confirmVerified(torrentMediator)
        confirmVerified(pageNavigationHelper)
    }

    @Test
    fun update_ui_when_torrent_model_exists() = runBlocking {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        coEvery { torrentMediator.getTorrentModel(infoHash = any()) }.returns(model)
        coEvery { viewMvc.showTorrent(any()) }.returns(Unit)

        // Act
        sut.showTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 1) { torrentMediator.getTorrentModel(infoHash = INFO_HASH) }
        coVerify(exactly = 1) { viewMvc.showTorrent(model) }

        confirmVerified(torrentMediator)
        confirmVerified(viewMvc)
    }

    @Test
    fun on_start_register_to_view() {
        // Arrange
        setupCurrentTorrentModel()
        every { viewMvc.registerListener(any()) }.returns(Unit)

        // Act
        sut.onStart()

        // Assert
        verify(exactly = 1) { viewMvc.registerListener(sut) }
        confirmVerified(viewMvc)
    }

    @Test
    fun on_stop_unregister_to_view() {
        // Arrange
        setupCurrentTorrentModel()
        every { viewMvc.unRegisterListener(any()) }.returns(Unit)

        // Act
        sut.onStop()

        // Assert
        verify(exactly = 1) { viewMvc.unRegisterListener(sut) }
        confirmVerified(viewMvc)
    }

    @Test
    fun add_paused_torrent_and_do_not_initiate_file_priority_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.PAUSED)
        val filePriority = TorrentFilePrioritySchemaUtils.getFilePrioritySchema(
            INFO_HASH, NUM_FILE
        )
        setupCurrentTorrentModel(model)
        noTorrentModelInDataSource()
        insertTorrentSuccess()
        pageNavigationBackSuccess()
        coEvery { torrentFilePriorityDataSource.getPriority(any()) }.returns(filePriority)

        // Act
        sut.addTorrent()

        // Assert
        coVerify(exactly = 1) { torrentDataSource.getTorrent(INFO_HASH) }
        coVerify(exactly = 1) { torrentDataSource.insertTorrent(scheme) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) { torrentFilePriorityDataSource.getPriority(INFO_HASH) }

        confirmVerified(torrentDataSource)
        confirmVerified(pageNavigationHelper)
        confirmVerified(torrentFilePriorityDataSource)
    }

    @Test
    fun add_paused_torrent_and_initiate_file_priority_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET).apply {
            numFiles = NUM_FILE
        }
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.PAUSED)
        val filePriorityNull = TorrentFilePrioritySchemaUtils
            .getFilePrioritySchemaNullFilePriority(INFO_HASH)
        val filePriority = filePriorityNull.copy(
            filePriority = MutableList(NUM_FILE) { TorrentFilePriority.default() }
        )
        setupCurrentTorrentModel(model)
        noTorrentModelInDataSource()
        insertTorrentSuccess()
        pageNavigationBackSuccess()
        coEvery { torrentFilePriorityDataSource.getPriority(any()) }.returns(filePriorityNull)
        setTorrentPrioritySuccess()

        // Act
        sut.addTorrent()

        // Assert
        coVerify(exactly = 1) { torrentDataSource.getTorrent(INFO_HASH) }
        coVerify(exactly = 1) { torrentDataSource.insertTorrent(scheme) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) { torrentFilePriorityDataSource.getPriority(INFO_HASH) }
        coVerify(exactly = 1) { torrentFilePriorityDataSource.setPriority(filePriority) }

        confirmVerified(torrentDataSource)
        confirmVerified(pageNavigationHelper)
        confirmVerified(torrentFilePriorityDataSource)
    }


    @Test
    fun do_not_add_paused_torrent_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.PAUSED)
        setupCurrentTorrentModel(model)
        coEvery { torrentDataSource.getTorrent(any()) }.returns(scheme)
        pageNavigationBackSuccess()

        // Act
        sut.addTorrent()

        // Assert
        coVerify(exactly = 1) { torrentDataSource.getTorrent(INFO_HASH) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }

        confirmVerified(torrentDataSource)
        confirmVerified(pageNavigationHelper)
    }

    @Test
    fun add_active_torrent_and_do_not_initiate_file_priority_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.ACTIVE)
        val filePriority = TorrentFilePrioritySchemaUtils.getFilePrioritySchema(
            INFO_HASH, NUM_FILE
        )
        setupCurrentTorrentModel(model)
        noTorrentModelInDataSource()
        insertTorrentSuccess()
        pageNavigationBackSuccess()
        coEvery { torrentFilePriorityDataSource.getPriority(any()) }.returns(filePriority)

        // Act
        sut.downloadTorrent()

        // Assert
        coVerify(exactly = 1) { torrentDataSource.getTorrent(INFO_HASH) }
        coVerify(exactly = 1) { torrentDataSource.insertTorrent(scheme) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) { torrentFilePriorityDataSource.getPriority(INFO_HASH) }

        confirmVerified(torrentDataSource)
        confirmVerified(pageNavigationHelper)
        confirmVerified(torrentFilePriorityDataSource)
    }

    @Test
    fun add_active_torrent_and_initiate_file_priority_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET).apply {
            numFiles = NUM_FILE
        }
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.ACTIVE)
        val filePriorityNull = TorrentFilePrioritySchemaUtils
            .getFilePrioritySchemaNullFilePriority(INFO_HASH)
        val filePriority = filePriorityNull.copy(
            filePriority = MutableList(NUM_FILE) { TorrentFilePriority.default() }
        )
        setupCurrentTorrentModel(model)
        noTorrentModelInDataSource()
        insertTorrentSuccess()
        pageNavigationBackSuccess()
        coEvery { torrentFilePriorityDataSource.getPriority(any()) }.returns(filePriorityNull)
        setTorrentPrioritySuccess()

        // Act
        sut.downloadTorrent()

        // Assert
        coVerify(exactly = 1) { torrentDataSource.getTorrent(INFO_HASH) }
        coVerify(exactly = 1) { torrentDataSource.insertTorrent(scheme) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) { torrentFilePriorityDataSource.getPriority(INFO_HASH) }
        coVerify(exactly = 1) { torrentFilePriorityDataSource.setPriority(filePriority) }

        confirmVerified(torrentDataSource)
        confirmVerified(pageNavigationHelper)
        confirmVerified(torrentFilePriorityDataSource)
    }


    @Test
    fun do_not_add_active_torrent_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        val scheme = TorrentSchemaUtils.getSchema(model, TorrentUserState.ACTIVE)
        setupCurrentTorrentModel(model)
        coEvery { torrentDataSource.getTorrent(any()) }.returns(scheme)
        pageNavigationBackSuccess()

        // Act
        sut.downloadTorrent()

        // Assert
        coVerify(exactly = 1) { torrentDataSource.getTorrent(INFO_HASH) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }

        confirmVerified(torrentDataSource)
        confirmVerified(pageNavigationHelper)
    }

    // region helper methods

    private fun insertTorrentSuccess() {
        coEvery { torrentDataSource.insertTorrent(any()) }.returns(Unit)
    }

    private fun setTorrentPrioritySuccess() {
        coEvery { torrentFilePriorityDataSource.setPriority(any()) }.returns(Unit)
    }

    private fun noTorrentModelInDataSource() {
        coEvery { torrentDataSource.getTorrent(any()) }.returns(null)
    }

    private fun pageNavigationBackSuccess() {
        coEvery { pageNavigationHelper.back() }.returns(Unit)
    }

    private fun setupCurrentTorrentModel(
        model: TorrentModel = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
    ) {
        coEvery { torrentMediator.getTorrentModel(infoHash = any()) }.returns(model)
        coEvery { viewMvc.showTorrent(any()) }.returns(Unit)
        sut.showTorrent(model.infoHash)
        clearAllMocks()
    }

    // endregion


    @After
    fun tearDown() {
        sut.onViewDestroy()
    }

    companion object {
        private const val INFO_HASH = "6ca8b71b3dfc217fc2420b7b07a97117740f6f03"
        private const val NAME = "torrent_name"
        private const val NUM_FILE = 666
        private const val MAGNET = "magnet:?xt=urn:btih:$INFO_HASH&dn=$NAME"
    }
}
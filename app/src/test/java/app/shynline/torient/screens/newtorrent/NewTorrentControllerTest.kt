package app.shynline.torient.screens.newtorrent

import app.shynline.torient.domain.database.common.states.TorrentUserState
import app.shynline.torient.domain.models.TorrentModel
import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.screens.common.requesthelper.FragmentRequestHelper
import app.shynline.torient.domain.mediator.usecases.AddTorrentToDataBaseUseCase
import app.shynline.torient.domain.mediator.usecases.GetTorrentModelUseCase
import dataset.TorrentModelUtils
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class NewTorrentControllerTest {

    private lateinit var getTorrentModelUseCase: GetTorrentModelUseCase
    private lateinit var addTorrentToDataBaseUseCase: AddTorrentToDataBaseUseCase
    private lateinit var viewMvc: NewTorrentViewMvc
    private lateinit var pageNavigationHelper: PageNavigationHelper
    private lateinit var fragmentRequestHelper: FragmentRequestHelper

    private lateinit var sut: NewTorrentController

    @Before
    fun setUp() {
        getTorrentModelUseCase = mockk()
        addTorrentToDataBaseUseCase = mockk()
        viewMvc = mockk()
        pageNavigationHelper = mockk()
        fragmentRequestHelper = mockk()
        sut = NewTorrentController(
            Dispatchers.Unconfined, getTorrentModelUseCase, addTorrentToDataBaseUseCase
        )
        sut.onCreateView()
        sut.bind(viewMvc, pageNavigationHelper, fragmentRequestHelper)
    }

    @Test
    fun navigate_back_when_torrent_model_does_not_exist() = runBlocking {
        // Arrange
        metaDataNotExists()
        pageNavigationBackSuccess()

        // Act
        sut.showTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        confirmVerified(pageNavigationHelper)
    }


    @Test
    fun update_ui_when_torrent_model_exists() = runBlocking {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        metaDataIsAvailable(model)
        coEvery { viewMvc.showTorrent(any()) }.returns(Unit)

        // Act
        sut.showTorrent(INFO_HASH)

        // Assert
        coVerify(exactly = 1) { viewMvc.showTorrent(model) }
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
    fun add_paused_torrent_and_navigate_back() {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET).apply {
            numFiles = NUM_FILES
        }
        setupCurrentTorrentModel(model)
        pageNavigationBackSuccess()
        setUpAddTorrentToDataBaseUseCase()

        // Act
        sut.addTorrent()

        // Assert
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) {
            addTorrentToDataBaseUseCase.invoke(
                AddTorrentToDataBaseUseCase.In(
                    model.infoHash,
                    model.name,
                    model.magnet,
                    TorrentUserState.PAUSED,
                    true,
                    model.numFiles
                )
            )
        }

        confirmVerified(pageNavigationHelper)
        confirmVerified(addTorrentToDataBaseUseCase)
    }

    @Test
    fun add_active_torrent_and_navigate_back() = runBlocking {
        // Arrange
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET).apply {
            numFiles = NUM_FILES
        }
        setupCurrentTorrentModel(model)
        pageNavigationBackSuccess()
        setUpAddTorrentToDataBaseUseCase()

        // Act
        sut.downloadTorrent()

        // Assert
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) {
            addTorrentToDataBaseUseCase(
                AddTorrentToDataBaseUseCase.In(
                    model.infoHash, model.name, model.magnet,
                    TorrentUserState.ACTIVE, true, model.numFiles
                )
            )
        }

        confirmVerified(pageNavigationHelper, addTorrentToDataBaseUseCase)
    }


    // region helper methods

    private fun metaDataNotExists() {
        coEvery { getTorrentModelUseCase.invoke(any()) }.returns(GetTorrentModelUseCase.Out(null))
    }

    private fun pageNavigationBackSuccess() {
        coEvery { pageNavigationHelper.back() }.returns(Unit)
    }

    private fun setupCurrentTorrentModel(
        model: TorrentModel = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
    ) {
        metaDataIsAvailable(model)
        coEvery { viewMvc.showTorrent(any()) }.returns(Unit)
        sut.showTorrent(model.infoHash)
        clearAllMocks()
    }

    private fun metaDataIsAvailable(model: TorrentModel) {
        coEvery { getTorrentModelUseCase.invoke(any()) }.returns(GetTorrentModelUseCase.Out(model))
    }

    private fun setUpAddTorrentToDataBaseUseCase() {
        coEvery {
            addTorrentToDataBaseUseCase.invoke(any())
        }.returns(
            AddTorrentToDataBaseUseCase.Out(
                true
            )
        )
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
        private const val MAGNET = "magnet:?xt=urn:btih:$INFO_HASH&dn=$NAME"
    }
}
package app.shynline.torient.screens.newmagnet

import app.shynline.torient.screens.common.navigationhelper.PageNavigationHelper
import app.shynline.torient.torrent.mediator.usecases.AddTorrentToDataBaseUseCase
import app.shynline.torient.torrent.mediator.usecases.GetTorrentModelUseCase
import app.shynline.torient.torrent.utils.Magnet
import dataset.TorrentModelUtils
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

class NewMagnetControllerTest {

    private lateinit var getTorrentModelUseCase: GetTorrentModelUseCase
    private lateinit var addTorrentToDataBaseUseCase: AddTorrentToDataBaseUseCase
    private lateinit var viewMvc: NewMagnetViewMvc
    private lateinit var pageNavigationHelper: PageNavigationHelper

    private lateinit var sut: NewMagnetController

    @Before
    fun setUp() {
        getTorrentModelUseCase = mockk()
        addTorrentToDataBaseUseCase = mockk()
        viewMvc = mockk()
        pageNavigationHelper = mockk()
        sut = NewMagnetController(
            Dispatchers.Unconfined,
            getTorrentModelUseCase,
            addTorrentToDataBaseUseCase
        )
        sut.onCreateView()
        sut.bind(viewMvc, pageNavigationHelper)
    }

    @Test
    fun invalid_magnet_navigate_back() = runBlocking {
        // Arrange
        pageNavigationBackSuccess()

        // Act
        sut.showTorrent(INVALID_MAGNET)

        // Assert
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        confirmVerified(pageNavigationHelper)
    }

    @Test
    fun valid_magnet_meta_data_does_not_exist_update_ui() = runBlocking {
        // Arrange
        metadataIsNotAvailable()
        coEvery { viewMvc.showMagnet(any()) }.returns(Unit)

        // Act
        sut.showTorrent(MAGNET)

        // Assert
        coVerify(exactly = 1) { viewMvc.showMagnet(MAGNET_OBJ) }
        confirmVerified(viewMvc)
    }

    @Test
    fun valid_magnet_meta_data_exists_update_ui_navigate_back() = runBlocking {
        // Arrange
        metaDataIsAvailable()
        pageNavigationBackSuccess()
        navigationToNewTorrentDialogSucceed()
        coEvery { viewMvc.showMagnet(any()) }.returns(Unit)

        // Act
        sut.showTorrent(MAGNET)

        // Assert

        coVerify(exactly = 1) { viewMvc.showMagnet(MAGNET_OBJ) }
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        coVerify(exactly = 1) { pageNavigationHelper.showNewTorrentDialog(INFO_HASH) }
        confirmVerified(pageNavigationHelper, viewMvc)
    }


    @Test
    fun on_start_register_to_view() {
        // Arrange
        setupCurrentMagnet()
        every { viewMvc.registerListener(any()) }.returns(Unit)

        // Act
        sut.onStart()

        // Assert
        verify(exactly = 1) { viewMvc.registerListener(sut) }
        confirmVerified(viewMvc)
    }

    @Test
    fun on_stop_register_to_view() {
        // Arrange
        setupCurrentMagnet()
        every { viewMvc.unRegisterListener(any()) }.returns(Unit)

        // Act
        sut.onStop()

        // Assert
        verify(exactly = 1) { viewMvc.unRegisterListener(sut) }
        confirmVerified(viewMvc)
    }

    @Test
    fun add_active_torrent_and_navigate_back() = runBlocking {
        // Arrange
        setupCurrentMagnet()
        pageNavigationBackSuccess()
        addTorrentToDataBaseSucceed()

        // Act
        sut.onDownloadClicked()

        // Assert
        coVerify(exactly = 1) { pageNavigationHelper.back() }
        confirmVerified(pageNavigationHelper)
    }

    @After
    fun tearDown() {
        sut.onViewDestroy()
    }

    // region helper methods

    private fun addTorrentToDataBaseSucceed() {
        coEvery { addTorrentToDataBaseUseCase(any()) }.returns(AddTorrentToDataBaseUseCase.Out(true))
    }


    private fun metaDataIsAvailable() {
        val model = TorrentModelUtils.getTorrentModel(INFO_HASH, NAME, MAGNET)
        coEvery { getTorrentModelUseCase(any()) }
            .returns(GetTorrentModelUseCase.Out(model))
    }

    private fun navigationToNewTorrentDialogSucceed() {
        coEvery { pageNavigationHelper.showNewTorrentDialog(any()) }.returns(Unit)
    }

    private fun setupCurrentMagnet() {
        metadataIsNotAvailable()
        coEvery { viewMvc.showMagnet(any()) }.returns(Unit)
        sut.showTorrent(MAGNET)
        clearAllMocks()
    }

    private fun metadataIsNotAvailable() {
        coEvery { getTorrentModelUseCase(any()) }
            .returns(GetTorrentModelUseCase.Out(null))
    }

    private fun pageNavigationBackSuccess() {
        coEvery { pageNavigationHelper.back() }.returns(Unit)
    }

    // endregion

    companion object {
        private const val INFO_HASH = "6ca8b71b3dfc217fc2420b7b07a97117740f6f03"
        private const val NAME = "torrent_name"
        private const val MAGNET = "magnet:?xt=urn:btih:$INFO_HASH&dn=$NAME"
        private val MAGNET_OBJ = Magnet.parse(MAGNET)!!
        private const val INVALID_MAGNET = ""
    }
}
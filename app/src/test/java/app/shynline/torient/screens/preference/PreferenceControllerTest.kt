package app.shynline.torient.screens.preference

import app.shynline.torient.common.userpreference.UserPreference
import app.shynline.torient.domain.torrentmanager.torrent.Torrent
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Before

class PreferenceControllerTest {

    // No need to test this class
    // Implementation is pretty simple

    private lateinit var torrent: Torrent
    private lateinit var userPreference: UserPreference
    private lateinit var viewMvc: PreferenceViewMvc

    private lateinit var sut: PreferenceController

    @Before
    fun setUp() {
        torrent = mockk()
        userPreference = mockk()
        viewMvc = mockk()
        sut = PreferenceController(Dispatchers.Unconfined, userPreference, torrent)
        sut.onCreateView()
        sut.bind(viewMvc)
    }

    @After
    fun tearDown() {
        sut.onViewDestroy()
    }


}
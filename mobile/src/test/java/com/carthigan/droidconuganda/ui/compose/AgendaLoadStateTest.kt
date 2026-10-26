package com.carthigan.droidconuganda.ui.compose

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.carthigan.droidconuganda.androidtest.util.LiveDataTestUtil
import com.carthigan.droidconuganda.model.Block
import com.carthigan.droidconuganda.shared.data.agenda.AgendaRepository
import com.carthigan.droidconuganda.shared.domain.agenda.LoadAgendaUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetTimeZoneUseCase
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import com.carthigan.droidconuganda.test.util.SyncTaskExecutorRule
import com.carthigan.droidconuganda.test.util.fakes.FakePreferenceStorage
import com.carthigan.droidconuganda.ui.agenda.AgendaViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.ZonedDateTime
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class AgendaLoadStateTest {
    @get:Rule val instantTaskExecutor = InstantTaskExecutorRule()
    @get:Rule val syncTaskExecutor = SyncTaskExecutorRule()
    @get:Rule val coroutineRule = MainCoroutineRule()

    @Test fun failedLoadStopsLoadingAndRetryRecovers() = coroutineRule.runTest {
        val repository = RecoverableAgendaRepository().apply { failing = true }
        val viewModel = createViewModel(repository)
        assertTrue(LiveDataTestUtil.getValue(viewModel.loadFailed))
        assertFalse(LiveDataTestUtil.getValue(viewModel.isLoading))

        repository.failing = false
        viewModel.refreshAgenda()
        assertEquals(repository.blocks, LiveDataTestUtil.getValue(viewModel.agenda))
        assertFalse(LiveDataTestUtil.getValue(viewModel.loadFailed))
    }

    @Test fun failedRefreshKeepsPreviouslyLoadedAgenda() = coroutineRule.runTest {
        val repository = RecoverableAgendaRepository()
        val viewModel = createViewModel(repository)
        assertEquals(repository.blocks, LiveDataTestUtil.getValue(viewModel.agenda))

        repository.failing = true
        viewModel.refreshAgenda()
        assertTrue(LiveDataTestUtil.getValue(viewModel.loadFailed))
        assertEquals(repository.blocks, LiveDataTestUtil.getValue(viewModel.agenda))
    }

    private fun createViewModel(repository: AgendaRepository) = AgendaViewModel(
        LoadAgendaUseCase(repository, coroutineRule.testDispatcher),
        GetTimeZoneUseCase(FakePreferenceStorage(), coroutineRule.testDispatcher)
    )
}

private class RecoverableAgendaRepository : AgendaRepository {
    var failing = false
    private val start = ZonedDateTime.parse("2026-10-28T10:00:00+03:00")
    val blocks = listOf(Block("Session block", "session", 0, startTime = start, endTime = start.plusHours(1)))

    override suspend fun getAgenda(forceRefresh: Boolean): List<Block> {
        if (failing) throw IOException("Schedule unavailable")
        return blocks
    }
}

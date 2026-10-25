/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.carthigan.droidconuganda.shared.data.agenda

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.carthigan.droidconuganda.shared.data.config.AppConfigDataSource
import com.carthigan.droidconuganda.shared.util.SyncExecutorRule
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.hamcrest.core.Is.`is`
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

/**
 * Unit test for [DefaultAgendaRepositoryTest].
 */
@ExperimentalCoroutinesApi
@FlowPreview
class DefaultAgendaRepositoryTest {

    // Executes tasks in the Architecture Components in the same thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Executes tasks in a synchronous [TaskScheduler]
    @get:Rule
    var syncExecutorRule = SyncExecutorRule()

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    // These strings are used for titles in addition to start/end time, but we want only
    // parsable texts for start/end time
    private val remoteConfigString = "2019-10-23T22:20-07:00"
    private val updatedString = "2019-10-23T23:20-07:00"

    private var stringToReturn = remoteConfigString

    private val mockDataSource = object : AppConfigDataSource {
        override fun getString(key: String): LiveData<String> =
            MutableLiveData(stringToReturn)

        override suspend fun syncStrings() {}

        override suspend fun isAutoScrollFlagEnabled(): Boolean = true
    }

    @Test
    fun testGetAgenda_noSyncWithRemote() = coroutineRule.runTest {
        val repository = DefaultAgendaRepository(mockDataSource)

        val result = repository.getAgenda(false)
        result.forEach {
            assertThat(it.startTime.toString(), `is`(remoteConfigString))
        }
    }

    @Test
    fun testGetAgenda_syncWithRemote() = coroutineRule.runTest {
        val repository = DefaultAgendaRepository(mockDataSource)
        stringToReturn = updatedString

        val result = repository.getAgenda(true)

        result.forEach {
            assertThat(it.startTime.toString(), `is`(updatedString))
        }
    }
}

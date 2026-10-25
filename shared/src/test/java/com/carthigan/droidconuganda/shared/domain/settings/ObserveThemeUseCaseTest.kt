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

package com.carthigan.droidconuganda.shared.domain.settings

import com.carthigan.droidconuganda.model.Theme
import com.carthigan.droidconuganda.shared.data.prefs.PreferenceStorage
import com.carthigan.droidconuganda.shared.result.Result.Success
import com.carthigan.droidconuganda.shared.result.data
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.single
import org.hamcrest.Matchers
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class ObserveThemeUseCaseTest {

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Test
    fun `observe current theme`() = coroutineRule.runTest {
        var currentTheme = Theme.SYSTEM // doesn't matter, just need to initialize it
        val mockFlow = flow {
            emit(currentTheme.storageKey)
        }
        val storage: PreferenceStorage = mock {}
        whenever(storage.observableSelectedTheme).thenReturn(mockFlow)

        val useCase = ObserveThemeModeUseCase(storage, coroutineRule.testDispatcher)
        val flow = useCase(Unit)

        for (theme in Theme.values()) {
            currentTheme = theme
            val result = flow.single()
            assertThat(result, Matchers.instanceOf(Success::class.java))
            assertThat(result.data, Matchers.equalTo(theme))
        }
    }
}

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
import com.carthigan.droidconuganda.shared.result.Result.Success
import com.carthigan.droidconuganda.shared.result.data
import com.carthigan.droidconuganda.test.data.MainCoroutineRule
import com.carthigan.droidconuganda.test.data.runTest
import com.carthigan.droidconuganda.test.util.FakePreferenceStorage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class GetThemeUseCaseTest {

    // Overrides Dispatchers.Main used in Coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    @Test
    fun `get current theme`() = coroutineRule.runTest {
        val storage = FakePreferenceStorage()
        val useCase = GetThemeUseCase(storage, coroutineRule.testDispatcher)

        for (theme in Theme.values()) {
            storage.selectedTheme = theme.storageKey

            val result = useCase(Unit)
            assertThat(result, instanceOf(Success::class.java))
            assertThat(result.data, equalTo(theme))
        }
    }
}

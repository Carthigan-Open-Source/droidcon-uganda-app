/*
 * Copyright 2018 Google LLC
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

package com.carthigan.droidconuganda.shared.model

import com.carthigan.droidconuganda.model.ConferenceData
import com.carthigan.droidconuganda.model.ConferenceDay
import com.carthigan.droidconuganda.shared.data.ConferenceDataRepository
import com.carthigan.droidconuganda.shared.data.ConferenceDataSource
import com.carthigan.droidconuganda.test.data.TestData
import com.carthigan.droidconuganda.test.util.FakeAppDatabase

object TestDataSource : ConferenceDataSource {
    override fun getRemoteConferenceData(): ConferenceData? {
        return TestData.conferenceData
    }

    override fun getOfflineConferenceData(): ConferenceData? {
        return TestData.conferenceData
    }
}

/** ConferenceDataRepository for tests */
object TestDataRepository : ConferenceDataRepository(
    TestDataSource,
    TestDataSource,
    FakeAppDatabase()
) {
    override fun getConferenceDays(): List<ConferenceDay> = TestData.TestConferenceDays
}

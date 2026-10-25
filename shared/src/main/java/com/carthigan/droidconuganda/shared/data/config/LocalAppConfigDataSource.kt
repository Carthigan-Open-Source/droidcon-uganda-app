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

package com.carthigan.droidconuganda.shared.data.config

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

/**
 * [AppConfigDataSource] without Remote Config: serves the static Droidcon
 * Uganda 2026 values that previously shipped as remote-config defaults.
 * Times follow the published timetable (East Africa Time, UTC+3).
 */
class LocalAppConfigDataSource @Inject constructor() : AppConfigDataSource {

    private val strings: Map<String, String> = mapOf(
        REGISTRATION_DAY1_START_TIME to "2026-10-28T08:00+03:00",
        REGISTRATION_DAY1_END_TIME to "2026-10-28T17:40+03:00",
        REGISTRATION_DAY2_START_TIME to "2026-10-29T08:00+03:00",
        REGISTRATION_DAY2_END_TIME to "2026-10-29T17:05+03:00",
        BREAKFAST_DAY1_START_TIME to "2026-10-28T08:00+03:00",
        BREAKFAST_DAY1_END_TIME to "2026-10-28T10:00+03:00",
        BREAKFAST_DAY2_START_TIME to "2026-10-29T08:00+03:00",
        BREAKFAST_DAY2_END_TIME to "2026-10-29T10:00+03:00",
        KEYNOTE_START_TIME to "2026-10-28T10:00+03:00",
        KEYNOTE_END_TIME to "2026-10-28T10:05+03:00",
        LUNCH_DAY1_START_TIME to "2026-10-28T13:05+03:00",
        LUNCH_DAY1_END_TIME to "2026-10-28T14:15+03:00",
        LUNCH_DAY2_START_TIME to "2026-10-29T13:00+03:00",
        LUNCH_DAY2_END_TIME to "2026-10-29T14:05+03:00",
        SESSIONS_DAY1_1_START_TIME to "2026-10-28T10:05+03:00",
        SESSIONS_DAY1_1_END_TIME to "2026-10-28T13:05+03:00",
        SESSIONS_DAY1_2_START_TIME to "2026-10-28T14:15+03:00",
        SESSIONS_DAY1_2_END_TIME to "2026-10-28T15:20+03:00",
        SESSIONS_DAY1_3_START_TIME to "2026-10-28T15:20+03:00",
        SESSIONS_DAY1_3_END_TIME to "2026-10-28T17:40+03:00",
        SESSIONS_DAY2_1_START_TIME to "2026-10-29T10:00+03:00",
        SESSIONS_DAY2_1_END_TIME to "2026-10-29T13:00+03:00",
        SESSIONS_DAY2_2_START_TIME to "2026-10-29T14:05+03:00",
        SESSIONS_DAY2_2_END_TIME to "2026-10-29T15:05+03:00",
        SESSIONS_DAY2_3_START_TIME to "2026-10-29T15:05+03:00",
        SESSIONS_DAY2_3_END_TIME to "2026-10-29T17:05+03:00",
        TEA_BREAK_DAY1_START_TIME to "2026-10-28T16:20+03:00",
        TEA_BREAK_DAY1_END_TIME to "2026-10-28T16:45+03:00",
        TEA_BREAK_DAY2_START_TIME to "2026-10-29T15:00+03:00",
        TEA_BREAK_DAY2_END_TIME to "2026-10-29T15:05+03:00",
        PARTY_START_TIME to "2026-10-29T18:00+03:00",
        PARTY_END_TIME to "2026-10-29T20:00+03:00",
        LABEL_REGISTRATION to "Registration",
        LABEL_KEYNOTE to "Opening",
        LABEL_SESSIONS to "Sessions",
        LABEL_BREAKFAST to "Breakfast",
        LABEL_LUNCH to "Lunch",
        LABEL_TEA_BREAK to "Tea break",
        LABEL_PARTY to "After party"
    )

    override fun getString(key: String): LiveData<String> =
        MutableLiveData(strings[key] ?: "")

    override suspend fun syncStrings() {
        // No remote source; the local values are always current.
    }

    override suspend fun isAutoScrollFlagEnabled(): Boolean = true

    companion object {
        const val REGISTRATION_DAY1_START_TIME = "registration_day1_start_time"
        const val REGISTRATION_DAY1_END_TIME = "registration_day1_end_time"
        const val REGISTRATION_DAY2_START_TIME = "registration_day2_start_time"
        const val REGISTRATION_DAY2_END_TIME = "registration_day2_end_time"
        const val BREAKFAST_DAY1_START_TIME = "breakfast_day1_start_time"
        const val BREAKFAST_DAY1_END_TIME = "breakfast_day1_end_time"
        const val BREAKFAST_DAY2_START_TIME = "breakfast_day2_start_time"
        const val BREAKFAST_DAY2_END_TIME = "breakfast_day2_end_time"
        const val KEYNOTE_START_TIME = "keynote_start_time"
        const val KEYNOTE_END_TIME = "keynote_end_time"
        const val LUNCH_DAY1_START_TIME = "lunch_day1_start_time"
        const val LUNCH_DAY1_END_TIME = "lunch_day1_end_time"
        const val LUNCH_DAY2_START_TIME = "lunch_day2_start_time"
        const val LUNCH_DAY2_END_TIME = "lunch_day2_end_time"
        const val SESSIONS_DAY1_1_START_TIME = "sessions_day1_1_start_time"
        const val SESSIONS_DAY1_1_END_TIME = "sessions_day1_1_end_time"
        const val SESSIONS_DAY1_2_START_TIME = "sessions_day1_2_start_time"
        const val SESSIONS_DAY1_2_END_TIME = "sessions_day1_2_end_time"
        const val SESSIONS_DAY1_3_START_TIME = "sessions_day1_3_start_time"
        const val SESSIONS_DAY1_3_END_TIME = "sessions_day1_3_end_time"
        const val SESSIONS_DAY2_1_START_TIME = "sessions_day2_1_start_time"
        const val SESSIONS_DAY2_1_END_TIME = "sessions_day2_1_end_time"
        const val SESSIONS_DAY2_2_START_TIME = "sessions_day2_2_start_time"
        const val SESSIONS_DAY2_2_END_TIME = "sessions_day2_2_end_time"
        const val SESSIONS_DAY2_3_START_TIME = "sessions_day2_3_start_time"
        const val SESSIONS_DAY2_3_END_TIME = "sessions_day2_3_end_time"
        const val TEA_BREAK_DAY1_START_TIME = "tea_break_day1_start_time"
        const val TEA_BREAK_DAY1_END_TIME = "tea_break_day1_end_time"
        const val TEA_BREAK_DAY2_START_TIME = "tea_break_day2_start_time"
        const val TEA_BREAK_DAY2_END_TIME = "tea_break_day2_end_time"
        const val PARTY_START_TIME = "party_start_time"
        const val PARTY_END_TIME = "party_end_time"
        const val LABEL_REGISTRATION = "label_registration"
        const val LABEL_KEYNOTE = "label_keynote"
        const val LABEL_SESSIONS = "label_sessions"
        const val LABEL_BREAKFAST = "label_breakfast"
        const val LABEL_LUNCH = "label_lunch"
        const val LABEL_TEA_BREAK = "label_tea_break"
        const val LABEL_PARTY = "label_party"
        const val AUTOSCROLL_FEATURE_ENABLED = "autoscroll_feature_enabled"
    }
}

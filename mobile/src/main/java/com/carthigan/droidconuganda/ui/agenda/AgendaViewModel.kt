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

package com.carthigan.droidconuganda.ui.agenda

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carthigan.droidconuganda.model.Block
import com.carthigan.droidconuganda.shared.domain.agenda.LoadAgendaUseCase
import com.carthigan.droidconuganda.shared.domain.settings.GetTimeZoneUseCase
import com.carthigan.droidconuganda.shared.result.Result
import com.carthigan.droidconuganda.shared.result.data
import com.carthigan.droidconuganda.shared.util.TimeUtils
import com.carthigan.droidconuganda.shared.util.map
import kotlinx.coroutines.launch
import org.threeten.bp.ZoneId
import javax.inject.Inject

class AgendaViewModel @Inject constructor(
    private val loadAgendaUseCase: LoadAgendaUseCase,
    private val getTimeZoneUseCase: GetTimeZoneUseCase
) : ViewModel() {

    private val _agenda = MutableLiveData<List<Block>>()
    val agenda: LiveData<List<Block>> = _agenda
    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading
    private val _loadFailed = MutableLiveData(false)
    val loadFailed: LiveData<Boolean> = _loadFailed

    private val preferConferenceTimeZoneResult = MutableLiveData<Boolean>()
    val timeZoneId = preferConferenceTimeZoneResult.map { inConferenceTimeZone ->
        if (inConferenceTimeZone) {
            TimeUtils.CONFERENCE_TIMEZONE
        } else {
            ZoneId.systemDefault()
        }
    }

    init {
        viewModelScope.launch {
            refreshConferenceTimeZone()
            loadAgenda(false)
        }
    }

    fun refreshAgenda() {
        if (_isLoading.value == true) return
        _isLoading.value = true
        // Agenda is lightweight and it's not possible to observe the changes with Remote Config,
        // we refresh the agenda on fragment start
        viewModelScope.launch {
            refreshConferenceTimeZone()
            loadAgenda(true)
        }
    }

    private suspend fun loadAgenda(forceRefresh: Boolean) {
        _isLoading.value = true
        val result = loadAgendaUseCase(forceRefresh)
        _loadFailed.value = result is Result.Error
        result.data?.let { _agenda.value = it }
        _isLoading.value = false
    }

    private suspend fun refreshConferenceTimeZone() {
        preferConferenceTimeZoneResult.value = getTimeZoneUseCase(Unit).data ?: true
    }
}

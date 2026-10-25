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

package com.carthigan.droidconuganda.shared.domain.logistics

import com.carthigan.droidconuganda.model.ConferenceWifiInfo
import com.carthigan.droidconuganda.shared.data.logistics.LogisticsRepository
import com.carthigan.droidconuganda.shared.di.DefaultDispatcher
import com.carthigan.droidconuganda.shared.domain.UseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class LoadWifiInfoUseCase @Inject constructor(
    private val logisticsRepository: LogisticsRepository,
    @DefaultDispatcher defaultDispatcher: CoroutineDispatcher
) : UseCase<Unit, ConferenceWifiInfo>(defaultDispatcher) {

    override fun execute(parameters: Unit): ConferenceWifiInfo {
        return logisticsRepository.getWifiInfo()
    }
}

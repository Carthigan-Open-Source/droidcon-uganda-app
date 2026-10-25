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

package com.carthigan.droidconuganda.shared.di

import android.content.Context
import androidx.work.Configuration
import com.carthigan.droidconuganda.shared.data.BootstrapConferenceDataSource
import com.carthigan.droidconuganda.shared.data.ConferenceDataRepository
import com.carthigan.droidconuganda.shared.data.ConferenceDataSource
import com.carthigan.droidconuganda.shared.data.NetworkConferenceDataSource
import com.carthigan.droidconuganda.shared.data.config.AppConfigDataSource
import com.carthigan.droidconuganda.shared.data.config.LocalAppConfigDataSource
import com.carthigan.droidconuganda.shared.data.db.AppDatabase
import com.carthigan.droidconuganda.shared.data.logistics.LocalLogisticsDataSource
import com.carthigan.droidconuganda.shared.data.logistics.LogisticsDataSource
import com.carthigan.droidconuganda.shared.data.logistics.LogisticsRepository
import com.carthigan.droidconuganda.shared.data.session.DefaultSessionRepository
import com.carthigan.droidconuganda.shared.data.session.SessionRepository
import com.carthigan.droidconuganda.shared.data.userevent.DefaultSessionAndUserEventRepository
import com.carthigan.droidconuganda.shared.data.userevent.LocalUserEventDataSource
import com.carthigan.droidconuganda.shared.data.userevent.SessionAndUserEventRepository
import com.carthigan.droidconuganda.shared.data.userevent.UserEventDataSource
import com.carthigan.droidconuganda.shared.data.work.IoschedWorkerFactory
import com.carthigan.droidconuganda.shared.time.DefaultTimeProvider
import com.carthigan.droidconuganda.shared.time.TimeProvider
import com.carthigan.droidconuganda.shared.util.NetworkUtils
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

/**
 * Module where classes created in the shared module are created.
 */
@Module
class SharedModule {

// Define the data source implementations that should be used. All data sources are singletons.

    @Singleton
    @Provides
    @Named("remoteConfDatasource")
    fun provideConferenceDataSource(
        context: Context,
        networkUtils: NetworkUtils
    ): ConferenceDataSource {
        return NetworkConferenceDataSource(context, networkUtils)
    }

    @Singleton
    @Provides
    @Named("bootstrapConfDataSource")
    fun provideBootstrapRemoteSessionDataSource(): ConferenceDataSource {
        return BootstrapConferenceDataSource
    }

    @Singleton
    @Provides
    fun provideConferenceDataRepository(
        @Named("remoteConfDatasource") remoteDataSource: ConferenceDataSource,
        @Named("bootstrapConfDataSource") boostrapDataSource: ConferenceDataSource,
        appDatabase: AppDatabase
    ): ConferenceDataRepository {
        return ConferenceDataRepository(remoteDataSource, boostrapDataSource, appDatabase)
    }

    @Singleton
    @Provides
    fun provideSessionRepository(
        conferenceDataRepository: ConferenceDataRepository
    ): SessionRepository {
        return DefaultSessionRepository(conferenceDataRepository)
    }

    @Singleton
    @Provides
    fun provideUserEventDataSource(context: Context): UserEventDataSource {
        // Local, on-device storage: no account and no cloud sync.
        return LocalUserEventDataSource(context)
    }

    @Singleton
    @Provides
    fun provideSessionAndUserEventRepository(
        userEventDataSource: UserEventDataSource,
        sessionRepository: SessionRepository
    ): SessionAndUserEventRepository {
        return DefaultSessionAndUserEventRepository(
            userEventDataSource,
            sessionRepository
        )
    }

    @Singleton
    @Provides
    fun provideLogisticsDataSource(): LogisticsDataSource {
        return LocalLogisticsDataSource()
    }

    @Singleton
    @Provides
    fun provideLogisticsRepository(
        logisticsDataSource: LogisticsDataSource
    ): LogisticsRepository {
        return LogisticsRepository(logisticsDataSource)
    }

    @Singleton
    @Provides
    fun provideAppConfigDataSource(): AppConfigDataSource {
        return LocalAppConfigDataSource()
    }

    @Singleton
    @Provides
    fun provideTimeProvider(): TimeProvider {
        return DefaultTimeProvider
    }

    @Singleton
    @Provides
    fun provideWorkManagerConfiguration(
        ioschedWorkerFactory: IoschedWorkerFactory
    ): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .setWorkerFactory(ioschedWorkerFactory)
            .build()
    }
}

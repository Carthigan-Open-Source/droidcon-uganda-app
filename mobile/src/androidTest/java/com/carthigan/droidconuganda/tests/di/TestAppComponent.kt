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

package com.carthigan.droidconuganda.tests.di

import com.carthigan.droidconuganda.MainApplication
import com.carthigan.droidconuganda.di.ActivityBindingModule
import com.carthigan.droidconuganda.di.AppModule
import com.carthigan.droidconuganda.di.SignInModule
import com.carthigan.droidconuganda.shared.di.BroadcastReceiverBindingModule
import com.carthigan.droidconuganda.shared.di.SharedModule
import com.carthigan.droidconuganda.shared.di.ViewModelModule
import com.carthigan.droidconuganda.ui.ThemedActivityDelegateModule
import com.carthigan.droidconuganda.ui.signin.SignInViewModelDelegateModule
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

/**
 * Main component of the app for instrumentation tests,
 * created and persisted in the MainTestApplication class.
 *
 * Whenever a new module is created, it should be added to the list of modules.
 * [AndroidSupportInjectionModule] is the module from Dagger.Android that helps with the
 * generation and location of subcomponents.
 */
@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        // Test Module that overrides CoroutinesModule
        TestCoroutinesModule::class,
        ActivityBindingModule::class,
        BroadcastReceiverBindingModule::class,
        ViewModelModule::class,
        SharedModule::class,
        SignInModule::class,
        SignInViewModelDelegateModule::class,
        ThemedActivityDelegateModule::class
    ]
)
interface TestAppComponent : AndroidInjector<MainApplication> {
    @Component.Builder
    abstract class Builder : AndroidInjector.Builder<MainApplication>()
}

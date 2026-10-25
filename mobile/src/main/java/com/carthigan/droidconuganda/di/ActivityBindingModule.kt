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
package com.carthigan.droidconuganda.di

import com.carthigan.droidconuganda.shared.di.ActivityScoped
import com.carthigan.droidconuganda.ui.LaunchModule
import com.carthigan.droidconuganda.ui.LauncherActivity
import com.carthigan.droidconuganda.ui.MainActivity
import com.carthigan.droidconuganda.ui.MainActivityModule
import com.carthigan.droidconuganda.ui.agenda.AgendaModule
import com.carthigan.droidconuganda.ui.info.InfoModule
import com.carthigan.droidconuganda.ui.onboarding.OnboardingActivity
import com.carthigan.droidconuganda.ui.onboarding.OnboardingModule
import com.carthigan.droidconuganda.ui.prefs.PreferenceModule
import com.carthigan.droidconuganda.ui.schedule.ScheduleModule
import com.carthigan.droidconuganda.ui.search.SearchModule
import com.carthigan.droidconuganda.ui.sessioncommon.EventActionsViewModelDelegateModule
import com.carthigan.droidconuganda.ui.sessiondetail.SessionDetailModule
import com.carthigan.droidconuganda.ui.signin.NotificationsPreferenceModule
import com.carthigan.droidconuganda.ui.speaker.SpeakerModule
import dagger.Module
import dagger.android.ContributesAndroidInjector

/**
 * We want Dagger.Android to create a Subcomponent which has a parent Component of whichever module
 * ActivityBindingModule is on, in our case that will be [AppComponent]. You never
 * need to tell [AppComponent] that it is going to have all these subcomponents
 * nor do you need to tell these subcomponents that [AppComponent] exists.
 * We are also telling Dagger.Android that this generated SubComponent needs to include the
 * specified modules and be aware of a scope annotation [@ActivityScoped].
 * When Dagger.Android annotation processor runs it will create 2 subcomponents for us.
 */
@Module
abstract class ActivityBindingModule {

    @ActivityScoped
    @ContributesAndroidInjector(modules = [LaunchModule::class])
    internal abstract fun launcherActivity(): LauncherActivity

    @ActivityScoped
    @ContributesAndroidInjector(modules = [OnboardingModule::class])
    internal abstract fun onboardingActivity(): OnboardingActivity

    @ActivityScoped
    @ContributesAndroidInjector(
        modules = [
            // activity
            MainActivityModule::class,
            // fragments
            ScheduleModule::class,
            SessionDetailModule::class,
            AgendaModule::class,
            InfoModule::class,
            SearchModule::class,
            SpeakerModule::class,
            // other
            EventActionsViewModelDelegateModule::class,
            NotificationsPreferenceModule::class,
            PreferenceModule::class
        ]
    )
    internal abstract fun mainActivity(): MainActivity
}

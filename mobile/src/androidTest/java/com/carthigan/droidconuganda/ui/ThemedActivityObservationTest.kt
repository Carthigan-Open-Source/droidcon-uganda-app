package com.carthigan.droidconuganda.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.MutableLiveData
import androidx.test.platform.app.InstrumentationRegistry
import com.carthigan.droidconuganda.model.Theme
import com.carthigan.droidconuganda.util.observeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemedActivityObservationTest {
    @Test fun sharedThemeKeepsSeparateSubscriptionsForOverlappingOwners() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val theme = MutableLiveData<Theme>()
            val first = ThemeLifecycleOwner()
            val second = ThemeLifecycleOwner()
            try {
                first.observeTheme(theme)
                second.observeTheme(theme)
                assertTrue(theme.hasObservers())

                first.registry.currentState = Lifecycle.State.DESTROYED
                assertTrue(theme.hasObservers())

                second.registry.currentState = Lifecycle.State.DESTROYED
                assertFalse(theme.hasObservers())
            } finally {
                first.registry.currentState = Lifecycle.State.DESTROYED
                second.registry.currentState = Lifecycle.State.DESTROYED
            }
        }
    }

    @Test fun stoppedOwnerAppliesLatestThemeOnlyWhenStartedAgain() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            val previousMode = AppCompatDelegate.getDefaultNightMode()
            val theme = MutableLiveData<Theme>()
            val owner = ThemeLifecycleOwner()
            try {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                owner.observeTheme(theme)
                theme.value = Theme.LIGHT
                assertEquals(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                    AppCompatDelegate.getDefaultNightMode())

                owner.registry.currentState = Lifecycle.State.STARTED
                assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())

                owner.registry.currentState = Lifecycle.State.CREATED
                theme.value = Theme.DARK
                assertEquals(AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.getDefaultNightMode())

                owner.registry.currentState = Lifecycle.State.STARTED
                assertEquals(AppCompatDelegate.MODE_NIGHT_YES, AppCompatDelegate.getDefaultNightMode())
            } finally {
                owner.registry.currentState = Lifecycle.State.DESTROYED
                AppCompatDelegate.setDefaultNightMode(previousMode)
            }
        }
    }
}

private class ThemeLifecycleOwner : LifecycleOwner {
    val registry = LifecycleRegistry(this).apply { currentState = Lifecycle.State.CREATED }
    override val lifecycle: Lifecycle get() = registry
}

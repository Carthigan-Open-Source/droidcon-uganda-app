# Startup crash repair

## Boundary

No builds, Gradle syncs, Kotlin compilation, APK installation, or Android test execution. The device's existing crash log was read without launching the app or clearing logs. Regression sources are written before the fixes, but RED/GREEN execution remains deferred to the user's build.

## Evidence and diagnosis

- 10:36:29: `LiveData.observe` rejects an observer already attached to another lifecycle, at `MainActivity.kt:60`. Onboarding and MainActivity share the singleton `ThemedActivityDelegate.theme` and both register `Observer(::updateForTheme)`. Those callable-reference adapters compare equal. Use a distinct object observer for each subscription, keeping automatic lifecycle removal and theme application.
- 10:36:35: Navigation 2.10.2 fails in `rememberNavHostEventHandler` because `LocalNavigationEventDispatcherOwner.current` is null. Its published dependencies already resolve Activity Compose/Activity 1.13.0. AppCompat 1.1.0-rc01 overrides `setContentView` without calling ComponentActivity, so `initializeViewTreeOwners()` never runs. Compose's compatibility `setOwners()` installs only lifecycle, ViewModel, and saved-state owners. Explicitly initialize the built-in activity owners before attaching Compose in both host activities. Do not allocate a replacement dispatcher or change dependency versions to mask this host setup error.

## Checklist

- [x] Read both crash chains and trace their owner/observer setup against the actual library sources.
- [x] Add regressions for shared-theme lifecycle subscriptions, onboarding-to-main handoff, and recreated-host system back navigation (sources only; not executed).
- [x] Give each lifecycle its own observer and initialize activity view-tree owners before Compose attachment.
- [x] Inspect the patch and run source-only checks.
- [ ] User rebuild/relaunch and Android regression execution (deferred; not authorized for the agent).

## Navigation Event scope

- Compile SDK is already 37; Navigation 2 remains in use, not Navigation 3.
- There is one activity NavHost; no custom interception or parent-child dispatchers are needed.
- Keep the activity's built-in dispatcher and the library's automatic dialog/sheet ownership and teardown.
- No legacy back handler migration or predictive-back manifest behavior change is needed to repair these crashes.

## Source review

A focused read-only review confirmed the observer equality issue against the pre-repair class artifacts from the user's build, and confirmed that the explicit owner setup uses ComponentActivity's existing dispatcher. Its missing onboarding-handoff coverage finding is addressed by `ComposeOnboardingStartupTest`, which directly launches OnboardingActivity, completes it, checks the schedule, and finishes the launched MainActivity during cleanup. Existing preference setup suppresses unrelated attendance/notification prompts; it does not bypass an explicitly launched OnboardingActivity.

The theme-mode regression restores AppCompat's process-global mode in `finally`; keep these instrumented tests serial. The recreated-host test also asserts navigation state restoration, so a failure after startup may concern restoration rather than owner provisioning. No Android tests were executed.

## Source-only verification

- `git diff --check` is clean.
- Both activity call sites use `observeTheme` and initialize view-tree owners before their `setContent` calls.
- Cached Activity 1.13.0 sources confirm the invoked method is public and supplies the existing activity Navigation dispatcher on the decor view.
- All string resource references in the three new regression files resolve; the four test methods are uncompiled and unexecuted.
- Compilation, crash resolution on the rebuilt APK, onboarding handoff, and system Back remain unverified until the user's next build/relaunch. Do not infer success from the installed pre-repair APK or its old crash buffer.

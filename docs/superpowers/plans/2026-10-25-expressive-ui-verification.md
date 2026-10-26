# Expressive UI verification

## Execution boundary

The user explicitly asked for **no builds** and subsequently asked to skip the browser preview. No Gradle task, sync, Kotlin compilation, Android test, emulator session, or preview renderer has been run. The Android test files are unexecuted; they are not evidence of runtime correctness.

## Source checks

- XML/resource inspection: 158 XML files parse. The new UI's 136 mobile and 1 shared string/plural references resolve in their owning modules, and all 11 drawable references resolve. In particular, `app_name` explicitly uses `SharedR` rather than relying on transitive R classes.
- `git diff --check` is clean. A read-only lexical check found balanced delimiters, comments, and literals in 33 new/modified Kotlin files; this is not a Kotlin compiler or type checker.
- Custom text colors: 15 checked light/dark text/container pairs exceed WCAG AA's 4.5:1 threshold. The lowest checked muted-text pairing is 5.84:1. Wallpaper colors use Android's semantic dynamic-color pairs.
- Dependency metadata: AGP 9.4.1 declares Kotlin 2.2.10, matching the Compose compiler plugin. The added Compose, Activity, and Lifecycle artifact metadata is reachable.
- Navigation 2.10.2's AAR requires compile API 37. The compile setting is updated to 37, the platform is already installed, and target API 36/minimum API 24 are unchanged. Compose BOM 2026.06.01 aligns UI/foundation/runtime at 1.11.4 above Navigation's minimums; UI 1.11.4 declares Kotlin stdlib 2.1.20, consumable by the project's Kotlin 2.2.10 toolchain.
- Expressive API inspection: `MaterialExpressiveTheme`, `MotionScheme.expressive()`, and indeterminate `LoadingIndicator()` are available in the pinned Material 3 sources. Expressive APIs are explicitly opted into.
- Interoperability: existing Glide speaker photos and the bundled license HTML use Android Views inside Compose; no new photo/logo assets were fabricated.

These checks are source-level evidence only, not a build or an accessibility certification.

## Added regression coverage (not run)

Eight regression source files contain 17 test methods. The parameterized auto-scroll method covers both pre-conference and during-conference modes; no cases were executed.

- Day selection and bookmark action isolation.
- Error retry behavior and the empty saved-schedule escape action.
- Existing session deep links and rejection of unsupported schemes/hosts/paths.
- Selected-zone time formatting and missing/whitespace-heavy speaker names.
- Agenda load failure/retry and preservation of cached agenda data after refresh failure.
- Narrow-phone refresh reachability at 200% font scale; full-host Compose navigation, filtering, FAQ disclosure, speaker navigation, licenses, and current-session auto-scroll. These replace the XML-host Espresso tests whose selectors are no longer applicable.

## Device validation for the user's build

- [ ] Complete onboarding and verify it is not repeated on relaunch.
- [ ] Navigate Schedule, Agenda, and Info; verify tab state, day selection, list positions, and back navigation survive rotation/process restoration.
- [ ] Filter topics/types, save and unsave sessions, relaunch, and verify both filters and bookmarks persist.
- [ ] Search by title and speaker; type quickly and clear the query while results are loading.
- [ ] Open a session, speaker, related session, share chooser, and available video/social links.
- [ ] Open `adssched://sessions/1287249` on a cold and warm launch; rotate after navigating elsewhere and verify the original deep link is not replayed.
- [ ] Check initial loading, empty filters, no bookmarks, offline load/refresh errors, and retry.
- [ ] Change Light, Dark, System, and supported Battery Saver themes; test wallpaper colors on Android 12+.
- [ ] Change conference/device time-zone preferences and verify all schedule, detail, and agenda times.
- [ ] Allow, deny, and revoke notification access; verify reminders and the notification setting match the actual permission state.
- [ ] Read FAQ entries and bundled licenses; open venue directions and website/legal links.
- [ ] Check a narrow phone, landscape, and a wide window; use 200% fonts, a hardware keyboard, TalkBack, gesture navigation, and three-button navigation.

## Delivery gate

- Source/resource integrity: **PASS (source only)**; resource/lexical/diff checks above are the evidence, not a build.
- Visual direction/purpose: **PASS (source only)**; the design spec records the reference-derived palette, typography, shapes, hierarchy, and ENERGY 2 / RHYTHM 2 / MOTION 2.
- Content honesty: **PASS (source only)**; actual conference models, existing resource copy/speaker photo URLs, and explicit initials fallbacks only. No new testimonials, statistics, logos, or fictional identities.
- Custom text contrast: **PASS (calculation only)**; all 15 checked light/dark pairs exceed 4.5:1. Device focus, dynamic wallpaper output, and accessibility interaction remain unverified.
- Runtime click-through, screenshots, device accessibility, compilation, and test execution: **UNVERIFIED**, deliberately deferred to the user rather than reported as passing.

## Best practices and security alignment update

- Area: session deep-link routing and reminder handoff, supporting the new single-activity Compose host.
- Impact: preserves the current task/back stack on a warm reminder launch, limits resolution to this app, and prevents mutable notification click payloads.
- Files: `mobile/src/main/AndroidManifest.xml`, `mobile/src/main/java/com/carthigan/droidconuganda/ui/MainActivity.kt`, `mobile/src/main/java/com/carthigan/droidconuganda/ui/compose/ConferenceNavigation.kt`, and `shared/src/main/java/com/carthigan/droidconuganda/shared/notifications/AlarmBroadcastReceiver.kt`.
- Implementation: both cold and warm launches parse only the existing `adssched://sessions/{id}` form; incoming Intents are not forwarded. Outgoing website/video URLs allow HTTP(S) only. Reminder clicks resolve a package-scoped component and use an immutable per-session PendingIntent instead of clearing the Compose task with TaskStackBuilder.

```diff
- intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
- TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)
-     .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
+ intent.setPackage(context.packageName)
+ intent.component = intent.resolveActivity(context.packageManager)
+ intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
+ PendingIntent.getActivity(context, session.id.hashCode(), intent,
+     PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
```

The manifest/deep-link source checks and new test sources cover the declared contract. Actual reminder delivery and cold/warm launches remain on the device-validation checklist above.

## User build follow-up, 2026-10-26

The user's `:mobile:assembleDebug` log reached Kotlin compilation but failed when inlining Compose ViewModel helpers compiled for JVM 11 into the mobile module's JVM 1.8 bytecode. `mobile/build.gradle` now sets both Java source and target compatibility to 11; AGP 9's built-in Kotlin inherits that target. The other modules retain their existing targets because the reported failure is in the Compose-consuming mobile module.

The earlier Jetifier, manifest, annotation, and Java 8 deprecation warnings are not the cause of these errors. No build, sync, or test was run by the agent to verify this adjustment; the next user build is still required.

## Startup crash follow-up, 2026-10-26

The connected device's existing crash buffer identified two startup failures: `Cannot add the same observer with different lifecycles` in MainActivity, followed by `No NavigationEventDispatcher was provided via LocalNavigationEventDispatcherOwner` in NavHost. No app launch, log clearing, build, or reinstall was performed to retrieve these logs.

- Theme subscriptions now use a distinct object observer per lifecycle instead of equal top-level callable-reference adapters on the singleton theme LiveData. Both onboarding and MainActivity use the shared `observeTheme` helper.
- Both Compose host activities initialize ComponentActivity's built-in view-tree owners before attaching Compose. Navigation already brings in Activity/Activity Compose 1.13.0; AppCompat 1.1's `setContentView` bypassed that activity setup. No dependency change or substitute dispatcher is needed.
- Four additional regression methods cover overlapping theme owners, stopped-owner theme delivery, onboarding-to-main handoff, and system Back after activity recreation. These are uncompiled, unexecuted test sources, not passing test results.

Diagnosis, source-review findings, and pending verification are tracked in `2026-10-26-startup-crashes.md`. The user must rebuild and relaunch to verify the repaired APK, including the onboarding transition and Back from a detail/search screen. Existing app data need not be cleared.

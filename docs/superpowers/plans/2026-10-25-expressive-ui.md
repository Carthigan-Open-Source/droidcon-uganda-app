# Expressive UI Implementation Plan

> **For agentic workers:** Use executing-plans inline. The user requested implementation directly, without builds or additional approval gates.

**Goal:** Rewrite the app UI in Compose with the supplied reference's editorial calendar style and Material 3 Expressive.

**Architecture:** Reuse the existing Dagger factory and feature ViewModels. MainActivity hosts a Compose navigation graph and adaptive bottom navigation/rail; existing data and preference repositories remain unchanged. Legacy XML/Fragments stay available for compatibility, but are no longer the main UI.

**Tech Stack:** Kotlin 2.2.10 Compose compiler plugin, Compose BOM 2026.06.01, Material 3 1.5.0-alpha01 (public Expressive APIs), existing Navigation 2 runtime, compile API 37/target API 36, LiveData, Dagger, and Glide.

**Spec:** `docs/superpowers/specs/2026-10-25-expressive-ui-design.md`

## Global Constraints

- No builds, Gradle sync, compilation, Android tests, or browser previews.
- Preserve backend, real data, existing local bookmarks, theme/preferences, reminders, and session deep links.
- Work in the existing checkout without commits or worktree creation.
- Write regression tests first, but leave them unexecuted because they require Android compilation.

## Review Focus

1. Long titles, small windows, large fonts, and RTL must not hide session actions.
2. Empty, loading, failed, and cached data must remain distinguishable and recoverable.
3. Selecting sessions/speakers and handling notification deep links must not lose navigation state.
4. Bookmarks, persisted filters, theme, and time-zone changes must remain backed by existing use cases.
5. Runtime notification denial must not leave a misleading enabled setting.

## Tasks

- [x] 1. Add regression tests, Compose dependencies/compiler, shared theme, text helpers, and accessible reusable components.
- [x] 2. Implement the main Compose host, adaptive navigation, schedule/filter/day views, and conference agenda.
- [x] 3. Implement session and speaker detail screens and search, preserving share/video/social actions and deep links.
- [x] 4. Implement Info/FAQ/about/licenses/settings, attendance/notification prompts, and onboarding.
- [x] 5. Inspect resources and APIs, run non-build checks, request a source-only review, and document the unverified runtime boundary.

## Execution ledger

- User's direct-implementation request replaces the preview and design-approval workflow.
- Ruling: stay in the current checkout and do not commit; the user wants these edits available for their own build.
- Ruling: Android test execution and normal RED/GREEN verification are deferred because even test execution compiles the app. Test files are still written before implementation.
- Pre-flight: shared components feed all screens; route wrappers use the existing feature ViewModel APIs, not a parallel repository implementation.
- Tasks 1–4: source implementation is present. Regression tests are added/replaced but have not been compiled or executed.
- Source review: a fresh, read-only reviewer inspected the new UI, model/ViewModel wiring, resources, and existing reminder/deep-link paths. No source-level missing binding/reference was reported. Its important layout/navigation findings were addressed without running a build.
- Review correction: `WindowInsets.safeDrawing` in both Compose 1.9.4 and the final pinned 1.11.4 sources is `systemBars.union(ime).union(displayCutout)`. Keep this single inset owner; adding `imePadding()` would double-pad search.
- Fix pass: wrapping schedule actions, persistent speaker back header, RTL-mirrored arrow, singleTop MainActivity, and package-scoped immutable reminder PendingIntents that do not clear the existing Compose task.
- Fix pass: stale agenda feedback/in-flight guard, generic localized refresh errors, generation-guarded search publication/loading state, FAQ disclosure semantics, restored current-session auto-scroll, and bounded centered onboarding.
- Fix pass: notification permission rationale is inside the app theme and does not stack over the preference prompt; instant-app notification requests retain the platform install handoff.
- Ruling: use compile API 37 rather than downgrade the existing Navigation family or change target behavior. Its AAR explicitly declares minCompileSdk=37; API 37 is installed locally. Cost: building requires the API 37 platform.
- Ruling: align Compose to BOM 2026.06.01 instead of allowing Navigation to pull a partially newer runtime/UI over the initial 2025 BOM. Cost: the new UI uses Compose 1.11.4; the compiler plugin remains matched to AGP's Kotlin 2.2.10.
- Ruling: wallpaper-color preference is deliberately Compose-only, persisted in `ui_appearance`, while existing theme/time-zone/notification preferences remain on their use cases. Cost: retained legacy XML screens do not follow wallpaper colors.
- Ruling: retain legacy Fragment/XML sources and their DI bindings, but replace the five MainActivity XML/Espresso test classes with Compose integration/auto-scroll tests. Cost: legacy-only layout behavior is no longer the active-host UI test target.
- Ruling on reviewer exclusions: exact compilation, runtime rendering/performance, bidirectional time formatting, TalkBack/focus, device interaction, and test execution cannot be established without the forbidden build/run steps; leave them explicitly unverified. Legacy deletion and renewed account-system support are not part of this rewrite.
- Deferred minor: no general account sign-in event bridge is added because this fork has only local, always-signed-in user data. Reintroducing accounts would require corresponding UI/flow work.
- Deferred minor: full ktlint/Spotless verification is not performed because the project runs it through Gradle; imports and whitespace were inspected, not reported as formatter-verified.
- Final refinement: strict, module-qualified resource inspection found that `app_name` belongs to `shared`, not `mobile`. The three Compose uses now explicitly reference `SharedR`, respecting AGP's non-transitive R classes.
- Final non-build checks: `git diff --check` clean; 158 XML files parse; 136 mobile and 1 shared string/plural references resolve in their owning modules; 11 drawable references resolve; lexical delimiter/comment/literal inspection passes across 33 new/modified Kotlin files.
- Regression sources: 8 files containing 17 test methods, including replacement Compose host/navigation/auto-scroll coverage. None have been compiled or executed. No commits, builds, syncs, or previews were performed.

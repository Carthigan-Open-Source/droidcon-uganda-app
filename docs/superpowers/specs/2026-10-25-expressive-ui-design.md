# Droidcon Uganda expressive UI rewrite

## Intent

Replace the attendee-facing UI with Compose, heavily inspired by the supplied calendar reference: oversized dates, warm neutral backgrounds, muted topic-colored cards, strong typography, and generous, varied corner radii. Use Material 3 Expressive components and motion, not iOS chrome. Keep the existing conference data, ViewModels, local bookmarks, preferences, and reminder use cases.

## Constraints and decisions

- The user requested direct implementation, without a browser preview or further design approval.
- Do not run builds, Gradle syncs, compilation, instrumentation, or Android tests. Add regression tests for the user to run; report them as unexecuted.
- Work in the existing checkout. Do not create a worktree, commit, or remove legacy resources that other code still references.
- Keep Schedule, Agenda, and Info as the primary destinations. Keep session and speaker deep links and real session/speaker content.
- Rewrite onboarding, schedule/filter controls, agenda, search, session details, speaker details, conference information, FAQ, about/licenses, settings, and attendance/notification prompts.
- Use the existing Dagger ViewModel factory. Compose Navigation uses the project's Navigation 2 version; replacing the navigation architecture with Navigation 3 is not necessary for this UI rewrite.
- Compile against API 37, as required by the existing Navigation 2.10.2 artifacts. Keep target API 36 and minimum API 24. Align Compose with BOM 2026.06.01 (1.11.4); explicitly opt into Material 3 1.5.0-alpha01's public Expressive APIs.
- Use a branded light/dark palette by default; offer persisted Android 12+ wallpaper colors. Preserve existing theme, time-zone, attendance, analytics, and notification preferences.
- Use proper system/keyboard insets, accessible labels and touch targets, wrapping content, bounded reading widths, and a navigation rail on wider windows.

## Visual decisions

Reading this as a two-day conference companion for attendees, in the reference's warm editorial calendar style: ENERGY 2 / RHYTHM 2 / MOTION 2.

- Color: amber, sage, and lavender distinguish topic families, with dark on-container text instead of the old low-contrast topic colors.
- Layout: oversized day/month headers anchor the schedule; full-width cards prioritize the actual, sometimes long, talk titles.
- Typography: Android's sans-serif keeps native readability; bold dates and medium-weight titles reproduce the reference's hierarchy.
- Spacing: compact metadata, generous section separation, and content-driven card heights keep the schedule useful at large font sizes.
- Shapes: rounded cards, circular icon controls, and segmented day selectors use different radii to distinguish their jobs.
- Motion: Material Expressive motion and short navigation/content transitions communicate state changes, without decorative loops.
- Images/icons: reuse existing resources and speaker photos; initials are the honest loading/missing-photo fallback. No invented branding or people.
- Compatibility: preserve current-session auto-scroll until the user interacts, and replace the old XML-host Espresso tests with Compose interaction tests rather than leaving those obsolete selectors active.

## Verification boundary

Source/resource inspection and `git diff --check` are permitted. Compilation, runtime correctness, screenshots, and actual TalkBack/keyboard/device behavior remain for the user's build and device validation.

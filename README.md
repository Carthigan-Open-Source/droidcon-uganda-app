# Droidcon Uganda

<img src="shared/src/main/res/drawable-nodpi/droidcon_uganda_icon.png" alt="Droidcon Uganda icon" width="88" height="88" />

An Android conference companion for Droidcon Uganda 2026. Browse talks, find speakers, and save the sessions you want to attend.

The conference takes place on October 28-29, 2026 at the National ICT Innovation Hub in Nakawa, Kampala. See the [conference website](https://uganda.droidcon.com/) for event announcements.

## Features

- Browse both conference days and filter sessions by topic or type.
- See which session is currently running with a ribbon wrapped around its card.
- Save sessions to a personal schedule and enable session reminders.
- Search sessions and speakers, read talk descriptions, and explore related sessions.
- View the conference agenda, venue information, FAQs, and preferences.
- Switch between light, dark, and system themes, with optional wallpaper colors on Android 12 and later.
- Use bundled or cached conference data when offline. Refresh the schedule when connected.

Bookmarks and preferences stay on the device. This fork does not require an account, Firebase configuration, or cloud bookmark sync. Conference updates and uncached speaker photos still need an internet connection. Reminders depend on the device's notification settings and permissions.

## Getting started

### Requirements

- Android Studio compatible with Android Gradle Plugin 9.4.1.
- Android SDK Platform 37.
- The configured JetBrains JDK 25 runtime. Gradle's daemon configuration is in [`gradle/gradle-daemon-jvm.properties`](gradle/gradle-daemon-jvm.properties).
- An Android 7.0 or later device or emulator (API 24+).

The Gradle wrapper pins Gradle 9.6.0. The app compiles against API 37 and targets API 36. The `mobile` module compiles its Java/Kotlin bytecode to JVM 11; `shared`, `model`, and `test-shared` remain on JVM 8. This is separate from the JDK used to run Gradle.

### Build and run

Clone the repository:

```sh
git clone https://github.com/Carthigan-Open-Source/droidcon-uganda-app.git
cd droidcon-uganda-app
```

Open the project in Android Studio, configure the Android SDK location, and select the `mobile` module with the `debug` build variant. The SDK location can also be supplied through `local.properties` or `ANDROID_HOME`.

To assemble a debug APK from the command line:

```sh
./gradlew :mobile:assembleDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`. Use your own release signing configuration before publishing; the bundled debug keystore is for development.

### Tests

Run the mobile unit tests:

```sh
./gradlew :mobile:testStagingUnitTest
```

With a device or emulator connected, run the mobile instrumentation tests:

```sh
./gradlew :mobile:connectedStagingAndroidTest
```

Instrumentation tests use the `staging` variant, which provides test conference data instead of downloading the live schedule. The test sources cover Compose components, navigation, bookmarks, filtering, startup, and loading/error states.

## Project structure

| Module | Purpose |
| --- | --- |
| `mobile` | Android app, Compose screens, navigation, theming, and launcher configuration |
| `shared` | Conference data, repositories, use cases, local preferences, reminders, and shared resources |
| `model` | Conference, session, speaker, and user-event models |
| `test-shared` | Shared JVM test utilities |
| `androidTest-shared` | Android-aware test utilities |

The attendee UI uses Jetpack Compose and Material 3 Expressive. Existing ViewModels and Dagger injection connect it to the data layer. Some legacy Fragment/XML screens remain for compatibility.

## Conference data

The app downloads its schedule from the [Droidcon Uganda data repository](https://github.com/Carthigan-Open-Source/droidcon-uganda-data). A bundled snapshot at [`shared/src/main/resources/conference_data_2026.json`](shared/src/main/resources/conference_data_2026.json) provides an offline starting point.

For a future event, update the conference dates and time zone in [`gradle.properties`](gradle.properties), the data URLs in [`shared/build.gradle`](shared/build.gradle), and the bundled snapshot. The current conference time zone is `Africa/Kampala` (UTC+3).

## Branding

The launcher uses the blue DU mark with a green underline, with adaptive, round, and themed variants. The schedule header draws a Uganda outline in Compose using public-domain boundary data. Artwork provenance and rights information are in [`shared/ASSET_SOURCES.md`](shared/ASSET_SOURCES.md).

## License and acknowledgments

This project builds on Google's [IOSched](https://github.com/google/iosched) app and its Android Dev Summit fork. The source code is licensed under the [Apache License 2.0](LICENSE). Original copyright notices remain in the source. Conference artwork and trademarks retain their separate rights.

    Copyright 2018 Google Inc. All rights reserved.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

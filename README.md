# FOKOF2 Android — source package 1.2.0

This is the Android/Kotlin source package locally hardened from the Google AI Studio export.

## Open and build

Preferred mobile-friendly path: open an Android project in Google AI Studio Build mode, import this ZIP if the file explorer offers **Upload Zip file**, and use `AI_STUDIO_BUILD_PROMPT_AR.md` to request a Debug build and tests only. Google documents a cloud Android emulator and native Kotlin/Jetpack Compose support.

Alternative: open this folder in Android Studio with JDK 21 and Android SDK Platform 36.1 installed, sync Gradle, run `./gradlew testDebugUnitTest assembleDebug` (Windows: `gradlew.bat testDebugUnitTest assembleDebug`), and confirm the generated APK under `app/build/outputs/apk/debug/`. The first command-line run downloads Gradle 9.3.1 and verifies its checksum; internet access is required for the initial download and dependency resolution.

Build bootstrap files are included: `gradlew` (Linux/macOS), `gradlew.bat` and `gradlew.ps1` (Windows). They download the pinned Gradle 9.3.1 binary distribution once and verify its official SHA-256 before running it. These are small project bootstrap scripts, not the upstream `gradle-wrapper.jar` implementation. The project still cannot claim a successful Android build until the build command completes in an environment with the Android SDK and network access.

A development-only `debug.keystore` is included because the existing Gradle configuration explicitly references it. It uses the public Android debug-key credentials and is **not safe for release signing**. Never use it for a Play Store or production release; create a private release keystore separately and keep it out of source control.

## Important operating limits

- Authentication and data storage are local to this device, using Room. This is not a centralized multi-user service.
- Existing Room data is migrated from schema 11 to 12 and then 13. The database filename and Android application ID are retained for upgrade compatibility.
- Passwords written by this build use PBKDF2-HMAC with a unique random salt. Legacy values are accepted only to permit a successful login to upgrade them; known seeded owner credentials are moved to `OWNER_SETUP_REQUIRED` during database initialization.
- The first account on a truly empty local database becomes the local owner. Existing owner recovery is restricted to the pre-seeded legacy owner records.
- Orange Cash is enabled on a fresh install. Other payment methods remain disabled and blank until configured by the owner. Payment requests and manual approvals are stored locally and do not verify transfers or synchronize across devices.
- Purchased bundles become local credit balances after owner approval. A credit can be spent on one published, paid template. This entitlement is local to this installation.
- Gemini network calls are disabled. Local text helpers are deterministic fallback templates, not a connected generative AI service. No private AI key is required or included.
- Website publishing, server-side bookings, public QR creation, password reset emails, and server-side backups are not active in this package.

## Reports and checks

See `ANDROID_SECURITY_REVIEW_AR.md` for implemented changes, test results, and release blockers. Run `python3 tools/check_security_contracts.py` for the included static contract checks. See `NEXT_STEP_AR.md` and `AI_STUDIO_BUILD_PROMPT_AR.md` for the exact next step.

## Release safety

Do not share keystore files or signing passwords. Do not ship a release APK until Android Studio builds it successfully and the sign-in, owner recovery, purchase approval, bundle redemption, Room migration, and website-owner tests have been run on a test device.

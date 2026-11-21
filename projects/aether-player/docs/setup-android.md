# Android toolchain — setup

The workspace currently has **no JDK, no Gradle and no Android SDK installed**. The scaffold
in `projects/aether-player` cannot be built until these exist. This is a real blocker, not a
formality: `./gradlew :app:assembleDebug` has never run in this environment.

## Required

| Tool | Version | Why |
| --- | --- | --- |
| JDK | 17 (Temurin or Microsoft OpenJDK) | AGP 8.x requires JDK 17 |
| Android SDK | Platform 35, Build-Tools 35.x | `compileSdk 35` |
| Android SDK Platform-Tools | latest | adb for device tests |
| Gradle | 8.9 | only to generate the wrapper once |

## Install on Windows

1. **JDK 17**
   ```powershell
   winget install EclipseAdoptium.Temurin.17.JDK
   ```
   Verify: `java -version` must print `17.x`.

2. **Android command-line tools** — download `commandlinetools-win-*.zip` from
   <https://developer.android.com/studio#command-line-tools-only>, extract to
   `%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest`.

3. **Environment**
   ```powershell
   setx ANDROID_HOME "$env:LOCALAPPDATA\Android\Sdk"
   setx PATH "$env:PATH;$env:LOCALAPPDATA\Android\Sdk\platform-tools;$env:LOCALAPPDATA\Android\Sdk\cmdline-tools\latest\bin"
   ```
   Open a new terminal afterwards.

4. **SDK packages**
   ```powershell
   sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
   sdkmanager --licenses
   ```

5. **Gradle wrapper** (commit it once, then never regenerate)
   ```powershell
   winget install Gradle.Gradle
   cd projects/aether-player
   gradle wrapper --gradle-version 8.9
   ```

6. **First build**
   ```powershell
   cd projects/aether-player
   .\gradlew.bat :app:assembleDebug
   ```

## Not needed

Android Studio is optional. Everything above works headless. Studio is only worth installing
if you want the layout inspector and the emulator UI.

## Emulator note

`connectedDebugAndroidTest` needs a running device or emulator. If none is available, that
suite reports `not run` — it is never reported as passing.

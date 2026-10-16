# User Instruction Memory

This file records user instructions, preferences, and teachings for reference in future interactions.

## Entries

[Project Knowledge Summary]
- Date: 2026-10-16
- Context: Discovered by Agent while building an Android APK from `main.java`
- Category: Build Methods
- Instructions:
  - This project builds an Android APK without Gradle. Run `bash /workspace/build.sh`.
  - Toolchain is not preinstalled. Install JDK 17 (`apt-get install -y openjdk-17-jdk-headless`).
  - Android build-tools extracted at `/opt/android-sdk/android-14` (aapt2, d8, zipalign, apksigner).
  - Android platform `android.jar` at `/opt/android-sdk/android-34-ext12/android.jar`.
  - Build pipeline: aapt2 compile/link -> javac (-source/-target 8, bootclasspath android.jar) -> d8 -> zip classes.dex into base.apk -> zipalign -> apksigner.
  - App source: `app/AndroidManifest.xml`, `app/res/`, `app/src/com/example/qrencrypt/MainActivity.java`, QR dependency `app/libs/zxing-core.jar`.

[Project Knowledge Summary]
- Date: 2026-10-16
- Context: Discovered by Agent while attempting to install the built APK
- Category: Environment Configuration
- Instructions:
  - No Android device, emulator, or KVM is available in this sandbox; `adb devices` is always empty.
  - `platform-tools` (adb) is at `/opt/android-sdk/platform-tools/adb`; a device must be supplied externally to install.

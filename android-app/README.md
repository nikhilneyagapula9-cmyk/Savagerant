# SavageScreen Android app

The Android app hosts the same responsive web client as the Spring Boot server so account, rant, reaction, and moderation behavior stays consistent between platforms.

1. Start the backend on port 8081.
2. Open `android-app` in Android Studio and run the `app` configuration on an emulator. The default URL uses `10.0.2.2`, the Android emulator's host-machine address.
3. For a physical phone on a trusted local development network, set the debug app property `apiBaseUrl` to the computer's reachable address, for example `-PapiBaseUrl=http://192.168.1.20:8081`. Cleartext is enabled only in the Android debug variant. Production/release builds must use HTTPS.

The HTTP setting is for local development only. Use HTTPS and configure the production host before distributing a release build.

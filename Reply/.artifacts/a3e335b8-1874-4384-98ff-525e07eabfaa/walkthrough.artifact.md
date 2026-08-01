# Walkthrough - Fix Push Notifications (FCM)

I have implemented the necessary fixes to enable push notifications via Firebase Cloud Messaging (FCM).

## Changes Made

### 1. Manifest & Permissions
- **Service Registration**: Registered `NewsMessagingService` in `AndroidManifest.xml` (in both `app` and `presentation` modules) so that the system can deliver FCM messages to the app.
- **Notification Permission**: Added `android.permission.POST_NOTIFICATIONS` to the manifests. This is required for Android 13+ (API 33+) to show notifications.

### 2. MainActivity Enhancements
- **Runtime Permission**: Added logic to request the `POST_NOTIFICATIONS` permission from the user when the app starts on Android 13+.
- **FCM Initialization**:
    - Subscribed the device to the `"all"` topic for broadcast notifications.
    - Logged the FCM registration token to Logcat (tag: `MainActivity`) to assist in sending targeted test messages during development.

### 3. Messaging Service Robustness
- **Payload Fallback**: Updated `NewsMessagingService` to handle standard notification payloads sent from the Firebase Console. Previously, it only handled custom data payloads with a `news_data` key. It now generates a fallback `News` object to ensure a notification is displayed even if the custom data is missing.

## Verification Results

### Build Status
- The project builds successfully: `presentation:assembleDebug` completed without errors.

### Manual Testing Recommendations
1. **Grant Permission**: Launch the app on an Android 13+ device and tap "Allow" on the notification permission prompt.
2. **Check Logs**: Filter Logcat for `MainActivity` to find the device's FCM token.
3. **Send Test Message**: Use the Firebase Console to send a notification to the `"all"` topic or the specific device token and verify it appears on the device.

render_diffs(file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/AndroidManifest.xml)
render_diffs(file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/app/src/main/AndroidManifest.xml)
render_diffs(file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/notifications/NewsMessagingService.kt)
render_diffs(file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/main/MainActivity.kt)

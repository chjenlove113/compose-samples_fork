# Analysis & Implementation Plan - Fix Push Notifications (FCM)

## Root Causes Identified

1. **`NewsMessagingService` is NOT registered in `AndroidManifest.xml`**:
   Firebase Cloud Messaging requires declaring `FirebaseMessagingService` with an intent-filter for `com.google.firebase.MESSAGING_EVENT`. Without this in the manifest, FCM cannot invoke `NewsMessagingService` when messages arrive.

2. **`POST_NOTIFICATIONS` permission is missing from `AndroidManifest.xml`**:
   On Android 13+ (API level 33+), notifications require the `android.permission.POST_NOTIFICATIONS` permission in `AndroidManifest.xml`. `NotificationHelper.kt` explicitly checks for this permission and aborts if missing.

3. **Missing Runtime Permission Request**:
   On Android 13+, the app needs to request `POST_NOTIFICATIONS` permission from the user at runtime.

4. **Missing FCM Registration Token / Topic Subscription**:
   To receive messages from Firebase Console or backend, the app should either subscribe to a topic (e.g., `"all"` or `"news"`) or obtain/log the FCM registration token on startup.

5. **Payload Parsing in `NewsMessagingService`**:
   Currently, `NewsMessagingService` only handles custom `news_data` data payloads. If notifications are sent directly from the Firebase Console (using `remoteMessage.notification`), they were not displaying a local notification when the app is in the foreground.

---

## Proposed Changes

### Manifest & Permissions

#### [MODIFY] [AndroidManifest.xml (app)](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/app/src/main/AndroidManifest.xml) & [AndroidManifest.xml (presentation)](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/AndroidManifest.xml)
- Add `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`.
- Declare `NewsMessagingService`:
  ```xml
  <service
      android:name="com.app.tintuccongnghe.notifications.NewsMessagingService"
      android:exported="false">
      <intent-filter>
          <action android:name="com.google.firebase.MESSAGING_EVENT" />
      </intent-filter>
  </service>
  ```

---

### MainActivity & Permissions Setup

#### [MODIFY] [MainActivity.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/main/MainActivity.kt)
- Request `POST_NOTIFICATIONS` runtime permission on Android 13+ (API level 33+).
- Subscribe to FCM topic `"all"` and log FCM token for testing.

---

### Notification Handling

#### [MODIFY] [NewsMessagingService.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/notifications/NewsMessagingService.kt)
- Add handling for `remoteMessage.notification` fallback (creating a dummy `News` object or showing notification if `news_data` key is absent).

---

## Verification Plan

### Automated Build Verification
- Execute `gradle_build("presentation:assembleDebug")` to ensure everything compiles without errors.

### Manual Testing Instructions
1. Run app on device/emulator (Android 13+).
2. Verify notification permission prompt appears and accept it.
3. Check Logcat for `NewsMessagingService: Refreshed token: ...` or FCM topic subscription logs.
4. Send a test message from Firebase Console to verify notification delivery.

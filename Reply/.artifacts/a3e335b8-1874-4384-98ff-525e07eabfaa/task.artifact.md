# Task Checklist - Fix Push Notifications (FCM)

- [x] Manifest & Permissions
    - [x] Add `POST_NOTIFICATIONS` permission to `presentation/src/main/AndroidManifest.xml` and `app/src/main/AndroidManifest.xml`
    - [x] Declare `NewsMessagingService` in `presentation/src/main/AndroidManifest.xml` and `app/src/main/AndroidManifest.xml`
- [x] Notification Handling Fixes
    - [x] Update `NewsMessagingService.kt` to handle standard notification payload fallback
- [x] MainActivity Updates
    - [x] Add `POST_NOTIFICATIONS` runtime permission request in `MainActivity.kt`
    - [x] Subscribe to FCM topic `"all"` and log FCM token in `MainActivity.kt`
- [x] Verification
    - [x] Verify project builds cleanly
    - [x] Create walkthrough documentation

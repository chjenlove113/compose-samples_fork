# Fix OutOfMemoryError in OkHttp Dispatcher

The application is experiencing a `java.lang.OutOfMemoryError` in the `OkHttp Dispatcher` thread, specifically when allocating a large `String` (approx. 1.45 MB) on a heap limited to 16MB. This is typically caused by reading large network response bodies into memory, often due to aggressive logging or improper Retrofit converter configuration.

## Proposed Changes

### 1. Data Module

#### [MODIFY] [NetworkModule.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/data/src/main/java/com/app/tintuccongnghe/data/di/NetworkModule.kt)
- Reorder Retrofit converter factories to place `ScalarsConverterFactory` after `GsonConverterFactory`. This prevents `ScalarsConverterFactory` from accidentally attempting to convert large JSON/HTML responses into Strings.
- Ensure `HttpLoggingInterceptor` is set to a safer level (e.g., `BASIC` or `NONE`) if it's currently contributing to the OOM.

### 2. Presentation Module

#### [MODIFY] [AppUserSiteViewModel.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/account/AppUserSiteViewModel.kt)
- Inject the Hilt-provided `OkHttpClient` instead of creating a new instance locally.
- Remove `val client = OkHttpClient()` from `syncSite`.

#### [MODIFY] [ShowHomeRssViewModel.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/showHomeRSS/ShowHomeRssViewModel.kt)
- Inject the Hilt-provided `OkHttpClient` instead of creating a new instance locally.
- Remove `val client = OkHttpClient()` from `syncRss`.

#### [MODIFY] [RssRefreshWorker.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/work/RssRefreshWorker.kt)
- Inject the Hilt-provided `OkHttpClient` via `@AssistedInject`.
- Remove `val client = OkHttpClient()` from `doWork`.

## Verification Plan

### Automated Tests
- Build the project to ensure all dependency injections are working correctly.
- Run the app and trigger RSS synchronization to verify that memory usage is stable and no OOM occurs.

### Manual Verification
- Monitor Logcat for any `OutOfMemoryError` during network operations.
- Verify that RSS feeds are still correctly fetched and parsed.

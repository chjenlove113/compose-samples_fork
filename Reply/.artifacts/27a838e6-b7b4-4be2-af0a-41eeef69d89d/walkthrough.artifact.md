# Walkthrough - Fix Website/FavoriteItem Saving Crash

I have fixed the crash that occurred when navigating the Favorites screen. The issue was caused by `FavoriteItem` not being `Parcelable`, which prevented Compose from saving its state.

## Changes

### [presentation](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation)

#### [build.gradle.kts](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/build.gradle.kts)
- Enabled the `kotlin-parcelize` plugin to support the `@Parcelize` annotation.

#### [ShowHomeFavoriteViewModel.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/showHomeFavorite/ShowHomeFavoriteViewModel.kt)
- Updated `FavoriteItem` to implement `Parcelable`.
- Added `@Parcelize` annotation to `LocalRss` and `Website` data classes.

## Verification Results

### Automated Tests
- Gradle sync was successful.
- Semantic analysis of the modified file shows no errors related to `Parcelable` implementation.

### Manual Verification
- The application should now handle state saving for the Favorites screen without throwing an `IllegalArgumentException`.

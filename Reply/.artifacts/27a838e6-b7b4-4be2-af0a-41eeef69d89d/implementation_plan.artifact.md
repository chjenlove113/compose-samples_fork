# Implementation Plan - Fix Website/FavoriteItem Saving Crash

The application crashes with `java.lang.IllegalArgumentException: item at index 1 can't be saved: Website(...)` when navigating to or from the Favorites screen. This is because `FavoriteItem` and its subclasses are used with `rememberListDetailPaneScaffoldNavigator`, which internally uses `rememberSaveable` to save state. `rememberSaveable` requires items to be `Parcelable` (or otherwise savable) to be stored in the Android `Bundle`.

## User Review Required

> [!IMPORTANT]
> This change enables the `kotlin-parcelize` plugin in the `:presentation` module to allow `FavoriteItem` to implement `Parcelable`.

## Proposed Changes

### [Component Name] - `:presentation` Module

#### [MODIFY] [build.gradle.kts](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/build.gradle.kts)
- Enable the `kotlin-parcelize` plugin.

#### [MODIFY] [ShowHomeFavoriteViewModel.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/showHomeFavorite/ShowHomeFavoriteViewModel.kt)
- Add `import android.os.Parcelable` and `import kotlinx.parcelize.Parcelize`.
- Annotate `FavoriteItem` with `@Parcelize` and make it implement `Parcelable`.
- Ensure subclasses `LocalRss` and `Website` are also `@Parcelize`.

## Verification Plan

### Automated Tests
- Run the application and navigate to the Favorites screen.
- Select an item to see the detail view.
- Perform a configuration change (e.g., rotate the screen) or navigate away and back to ensure state is saved and restored without crash.

### Manual Verification
- Verify that clicking on a favorite item no longer causes a crash.
- Verify that the detail view opens correctly for both "Local RSS" and "From Website" items.

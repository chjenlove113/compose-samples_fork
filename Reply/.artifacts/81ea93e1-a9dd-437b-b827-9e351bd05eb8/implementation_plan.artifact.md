# Modernize UI Colors and Layout to match Reply Style

This plan aims to update the `ShowHomeScreen.kt` UI to match the modern, Material 3 "Reply" sample app style as requested in the attached images. This includes updating the card layouts, adding a search bar and FAB, and refining color usage.

## Proposed Changes

### [Component Name]

#### [MODIFY] [ShowHomeScreen.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/showHome/ShowHomeScreen.kt)
- **Add Search Bar**: Implement a `DockedSearchBar` at the top of the main list content, similar to the Reply app.
- **Add FAB**: Add a `FloatingActionButton` (with a pencil icon) to the home screen.
- **Redesign `NewsListItem`**:
    - Change layout to a Row with a profile/avatar image (from news image).
    - Add sender (source) name and time in a column.
    - Add a favorite (star) icon on the right.
    - Place the title and body snippet below the top row.
- **Redesign `NewsGridItem`**: Update to match the new card style.
- **Color Refinement**:
    - Use `MaterialTheme.colorScheme.surfaceContainer` for the screen background.
    - Use `MaterialTheme.colorScheme.surface` or `MaterialTheme.colorScheme.surfaceVariant` for card backgrounds to provide better contrast.
    - Ensure selection states use `primaryContainer`.

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Verify that the home screen background matches the warm cream color in the images.
- Verify that news items now follow the "Reply" layout (Avatar, Source, Date, Title, Snippet).
- Verify the presence and styling of the Search Bar and FAB.
- Check the adaptive layout (List/Detail) on larger screens to ensure it still works correctly with the new styling.

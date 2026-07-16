# Adaptive UI Implementation for AccountInfoScreen

This plan outlines the changes to make `AccountInfoScreen.kt` adaptive for different screen sizes (phones, tablets, and foldables).

## User Review Required

> [!NOTE]
> The adaptive design will focus on improving readability and usability on larger screens (Expanded width) by limiting the content width and potentially adjusting the layout of banners and settings items.

## Proposed Changes

### Component: Presentation

#### [MODIFY] [AccountInfoScreen.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/account/AccountInfoScreen.kt)

1.  **Import Adaptive Libraries:** Add imports for `androidx.compose.material3.adaptive` and `androidx.window.core.layout.WindowWidthSizeClass`.
2.  **Get Adaptive Info:** Use `currentWindowAdaptiveInfo()` to obtain the current `WindowSizeClass`.
3.  **Layout Adjustments:**
    *   **Root Container:** Update the main `Column` to be centered and have a maximum width on `EXPANDED` screens to avoid extremely long lines of text and wide buttons.
    *   **Banners:** Update `LoginBanner` and `UserInfoBanner` to adjust their height or internal layout for wider screens. For example, on wide screens, they could be wider or have more horizontal padding.
    *   **Settings Items:** Maintain a single column but with a constrained width for better focus on tablets.
4.  **Preview Support:** Add/Update previews for different screen sizes (Compact, Medium, Expanded) to verify the adaptive behavior.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.
- (Optional) Run UI tests if any exist for this screen.

### Manual Verification
- Deploy the app and test on:
    - Phone (Compact)
    - Tablet (Expanded)
    - Foldable (Medium/Expanded)
- Verify that on large screens, the content is not overly stretched and remains legible.

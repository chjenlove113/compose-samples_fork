# Implementation Plan - Add Feedback Message for Account Deletion

Update `AccountViewModel` and `AccountInfoScreen` to display a message to the user after attempting to delete their account.

## User Review Required

> [!NOTE]
> The feedback message will be displayed using a `Snackbar` at the bottom of the screen.

## Proposed Changes

### Presentation Layer

#### [MODIFY] [AccountViewModel.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/account/AccountViewModel.kt)
- Update `deleteAccount()` to be a `suspend` function or use a callback/event pattern to return the `DeleteAccountResponse`.
- Alternatively, expose a `SharedFlow` or `Channel` for "UI events" like showing a message.
- Let's expose a `SharedFlow<String>` for messages.

#### [MODIFY] [AccountInfoScreen.kt](file:///E:/Projects/AndroidStudio/Projects/compose-samples_fork/Reply/presentation/src/main/java/com/app/tintuccongnghe/account/AccountInfoScreen.kt)
- Add `SnackbarHostState` to the screen.
- Observe the message flow from `AccountViewModel` and show a snackbar.
- Update the `Delete` button click handler to trigger the deletion and wait for the result (if using a direct call) or just trigger it.

## Verification Plan

### Automated Tests
- Build the project to ensure no compilation errors.

### Manual Verification
- Trigger account deletion and verify that a message appears (e.g., "Account deleted successfully" or an error message).

# AGENTS.md - Rules for Kotlin Compose UI + Clean Architecture (Latest 2026)

## Công nghệ bắt buộc phải dùng
- **UI**: Luôn dùng **Jetpack Compose** (không dùng XML/View system trừ migrate legacy).
- **Design System**: **Material 3** (`androidx.compose.material3:*`) – KHÔNG dùng Material 2.
- **Ngôn ngữ**: Kotlin mới nhất (2.1+ / 2.2+).
- **Compose**: Phiên bản mới nhất theo Gradle (1.10.x+ hoặc cao hơn).
- **Navigation**: `androidx.navigation:navigation-compose` + Type-safe navigation (Navigation Compose 2.8+).
- **State management**:
    - `StateFlow` + `MutableStateFlow` trong ViewModel.
    - `collectAsStateWithLifecycle()` trong Composable.
    - `remember` / `derivedStateOf` cho state cục bộ.
- **Dependency Injection**: **Hilt** (`@HiltAndroidApp`, `@HiltViewModel`, `@Inject`, modules, qualifiers).
- **Local Database**: **Room** (`@Entity`, `@Dao`, `@Database`, Room 2.6.x+, với Kotlin coroutines/Flow support).
- **Kiến trúc tổng thể**: **Clean Architecture** (3 layers: Domain → Data → Presentation) kết hợp MVVM ở Presentation layer.
- **Async**: Kotlin Coroutines + Flow (StateFlow, SharedFlow).

## Clean Architecture Structure (bắt buộc tuân thủ)
- **:domain** (pure Kotlin module – không phụ thuộc Android/framework)
    - Entities (data class immutable)
    - Use Cases / Interactors (single responsibility, suspend functions hoặc Flow)
    - Repository interfaces
- **:data** (implementation của domain)
    - Repository impl
    - Data sources: Remote (Retrofit nếu có API), Local (Room)
    - Mappers (DTO ↔ Entity)
- **:presentation** hoặc **:app** (UI + ViewModel)
    - ViewModels (@HiltViewModel)
    - Composables (Screens, components)
    - UI State (data class sealed hoặc record)

Luồng dữ liệu: **UI → ViewModel → UseCase → Repository → Data Source** (unidirectional data flow).

## Best Practices Compose UI + Clean Arch (phải tuân thủ 100%)
- Mỗi screen/feature:
    - 1 Composable chính (ví dụ: `TodoListScreen`)
    - 1 ViewModel riêng (`TodoListViewModel`) – @HiltViewModel, inject UseCases
- State:
    - ViewModel expose `StateFlow<UiState>` (sealed interface/class với Loading, Success, Error)
    - Composable collect state bằng `collectAsStateWithLifecycle()`
- Hoist state lên ViewModel hoặc parent Composable.
- Tất cả data class immutable (`data class` + `val`).
- Preview: `@Preview` đầy đủ (light/dark, different devices, locales).
- Theme: `MaterialTheme` + dynamic color scheme.
- Adaptive/Responsive: `WindowSizeClass`, `provideWindowSizeClass`.
- Không lạm dụng `rememberCoroutineScope`; ưu tiên `LaunchedEffect` + `repeatOnLifecycle`.
- Xử lý lỗi: `try-catch` trong UseCase/Repository → emit Error state → show `Snackbar`/`AlertDialog`.
- Performance: `key()`, `rememberSaveable`, tránh recomposition thừa.

## ViewModel Rules (rất quan trọng)
- ViewModel **chỉ** thuộc Presentation layer.
- **Không** chứa business logic → đẩy xuống UseCase (Domain).
- Inject UseCases qua Hilt, không inject Repository trực tiếp vào ViewModel.
- Ví dụ tốt:
  ```kotlin
  @HiltViewModel
  class TodoViewModel @Inject constructor(
      private val getTodosUseCase: GetTodosUseCase,
      private val addTodoUseCase: AddTodoUseCase
  ) : ViewModel() { ... }
package com.app.tintuccongnghe.main

import com.app.tintuccongnghe.account.LanguageScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScope
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.app.tintuccongnghe.newsTag.NewsTagRoute
import com.app.tintuccongnghe.presentation.R
import com.app.tintuccongnghe.showHomeRSS.ShowHomeRSSFeedScreen
import com.app.tintuccongnghe.showHomeFavorite.ShowHomeFavoriteScreen
import android.net.Uri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import com.app.tintuccongnghe.domain.models.News
import com.app.tintuccongnghe.data.local.entities.RssItemEntity
import com.app.tintuccongnghe.account.AccountInfoScreen
import com.app.tintuccongnghe.account.AppUserSiteScreen
import com.app.tintuccongnghe.account.AppUserCategoryScreen
import com.app.tintuccongnghe.account.NotificationsScreen
import com.app.tintuccongnghe.account.LoginScreen
import com.app.tintuccongnghe.account.RegisterScreen
import com.app.tintuccongnghe.showHome.ShowHomeRoute2
import com.app.tintuccongnghe.showHomeForYou.ShowHomeForYouRoute
import com.app.tintuccongnghe.showHomeChild.ShowHomeChildRoute
import androidx.compose.material.icons.filled.Person
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.app.tintuccongnghe.theme.ContrastAwareReplyTheme
import androidx.activity.viewModels
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Tag
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.xr.compose.material3.EnableXrComponentOverrides
import androidx.xr.compose.material3.ExperimentalMaterial3XrApi
import androidx.xr.compose.material3.SpaceToggleButton
import com.google.firebase.messaging.FirebaseMessaging

private sealed interface TopLevelRoute1 {
    val icon: ImageVector
}
private data object Home : TopLevelRoute1 { override val icon = Icons.Default.Home }
private data object ChatList : TopLevelRoute1 { override val icon = Icons.Default.Face }

private data class ChatDetail(val id: News)
private data object Camera : TopLevelRoute1 { override val icon = Icons.Default.PlayArrow }

private val TOP_LEVEL_ROUTES : List<TopLevelRoute1> = listOf(Home, ChatList, Camera)

enum class AppDestinations(
    val label: String,
    val icon: ImageVector,
    val contentDescription: String
) {
    HOME("R.string.home", Icons.Default.Home, "home"),
    FAVORITES("R.string.favorites", Icons.Default.Favorite, "R.string.favorites"),
    SHOPPING("R.string.shopping", Icons.Default.ShoppingCart, "R.string.shopping"),
    PROFILE("R.string.profile", Icons.Default.AccountBox, "R.string.profile"),
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private var lastExitBackPressAt: Long? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification permission granted")
        } else {
            Log.d("MainActivity", "Notification permission denied")
        }
    }

    @SuppressLint("RestrictedApi")
    @OptIn(
        ExperimentalLayoutApi::class,
        ExperimentalMaterial3Api::class,
        ExperimentalMaterial3XrApi::class,
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        askNotificationPermission()
        initFcm()
        val isXrDevice = packageManager.hasSystemFeature(XR_SPATIAL_FEATURE)
        val startDestination = if (isXrDevice) "screen_rss" else "screen1"

        //enableEdgeToEdge()
        setContent {
            EnableXrComponentOverrides {
            val nightMode by mainViewModel.nightMode.collectAsStateWithLifecycle()
            val fontScale by mainViewModel.fontScale.collectAsStateWithLifecycle()

            ContrastAwareReplyTheme(
                darkTheme = nightMode,
                dynamicColor = true,
                fontScale = fontScale
            ) {
                val navController = rememberNavController()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
            //val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }
            var topLevelBackStack by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

            val navHost = rememberNavController()
            val navBackStackEntry by navHost.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            BackHandler(
                enabled = currentDestination?.route == startDestination &&
                    navHost.previousBackStackEntry == null
            ) {
                handleExitBackPress()
            }

            val rssItemJump by mainViewModel.rssItemJump.collectAsStateWithLifecycle()
            LaunchedEffect(rssItemJump) {
                if (rssItemJump != null) {
                    navigateWithBackStackHandling("screen_rss", navHost)
                }
            }

            val targetRoute by mainViewModel.targetRoute.collectAsStateWithLifecycle()
            LaunchedEffect(targetRoute) {
                val route = targetRoute
                if (route != null) {
                    navigateWithBackStackHandling(route, navHost)
                    mainViewModel.clearTargetRoute()
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd,
            ) {
            NavigationSuiteScaffold(
                navigationSuiteItems = navigationSuiteItems(currentDestination, navHost)

//                navigationSuiteItems = {
//                    AppDestinations.entries.forEach { topLevelRoute ->
//
//                        val isSelected = topLevelRoute == topLevelBackStack
//                        item(
//                            selected = isSelected,
//                            onClick = {
//                                topLevelBackStack = topLevelRoute
//                            },
//                            icon = {
//                                Icon(
//                                    imageVector = topLevelRoute.icon,
//                                    contentDescription = null
//                                )
//                            }, label = {Text("ZZ")}
//                        )
//                    }
//                }
            ){

                NavHost(
                    navController = navHost,
                    startDestination = startDestination
                ) {
                    composable(
                        route = "screen1"
                    ) {
                        ShowHomeRoute2()
                    }

                    composable(
                        route = "screen_foryou"
                    ) {
                        ShowHomeForYouRoute(
                            onNavigateToLogin = { navHost.navigate("login") }
                        )
                    }

                    composable(
                        route = "screen2"
                    ) {
                        NewsTagRoute()
                    }

                    composable(
                        route = "screen3"
                    ) {
                        ShowHomeChildRoute()
                    }

                    composable(
                        route = "screen_favorites"
                    ) {
                        ShowHomeFavoriteScreen(onBack = { navHost.popBackStack() })
                    }

                    composable(
                        route = "screen_rss",
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "reply://rss_feed" }
                        )
                    ) {
                        ShowHomeRSSFeedScreen(
                            onNavigateToLogin = { navHost.navigate("login") },
                            onNavigateToUserSites = { navHost.navigate("user_sites") }
                        )
                    }

                    composable(
                        route = "account"
                    ) {
                        AccountInfoScreen(
                            onNavigateToLogin = { navHost.navigate("login") },
                            onNavigateToUserSites = { navHost.navigate("user_sites") },
                            onNavigateToUserCategories = { navHost.navigate("user_categories") },
                            onNavigateToFavorites = { navHost.navigate("screen_favorites") },
                            onNavigateToNotifications = { navHost.navigate("notifications") },
                            onNavigateToLanguage = { navHost.navigate("language") }
                        )
                    }

                    composable(
                        route = "language"
                    ) {
                        LanguageScreen(
                            onBack = { navHost.popBackStack() }
                        )
                    }

                    composable(
                        route = "notifications"
                    ) {
                        NotificationsScreen(
                            onBack = { navHost.popBackStack() },
                            onNotificationClick = { newsJson ->
                                // newsJson in Entity is already a JSON string of NewsNotificationPayload
                                val encoded = Uri.encode(newsJson)
                                navHost.navigate("news_detail/$encoded")
                            }
                        )
                    }

                    composable(
                        route = "user_sites"
                    ) {
                        AppUserSiteScreen(
                            onBack = { navHost.popBackStack() },
                            onNavigateToLogin = { navHost.navigate("login") },
                            onNavigateToRss = { id, group ->
                                mainViewModel.setRssJump(id, group)
                                navigateWithBackStackHandling("screen_rss", navHost)
                            }
                        )
                    }

                    composable(
                        route = "user_categories"
                    ) {
                        AppUserCategoryScreen(
                            onBack = { navHost.popBackStack() },
                            onNavigateToLogin = { navHost.navigate("login") },
                            onNavigateToCategoryItems = { id, group ->
                                // Handle navigation if needed
                            }
                        )
                    }

                    composable(
                        route = "login",
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "reply://github-auth?code={code}" },
                            navDeepLink { uriPattern = "reply://login" }
                        )
                    ) { backStackEntry ->
                        val githubCode = backStackEntry.arguments?.getString("code")
                        LoginScreen(
                            onNavigateToRegister = { navHost.navigate("register") },
                            onLoginSuccess = { navHost.popBackStack() },
                            githubCode = githubCode
                        )
                    }

                    composable(
                        route = "register"
                    ) {
                        RegisterScreen(
                            onNavigateToLogin = { navHost.navigate("login") },
                            onRegisterSuccess = { navHost.popBackStack() }
                        )
                    }

                    composable(
                        route = "news_detail/{newsJson}",
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "reply://news_detail/{newsJson}" }
                        )
                    ) { backStackEntry ->
                        val newsJson = backStackEntry.arguments?.getString("newsJson")
                        val payload = newsJson?.let { 
                            try {
                                Json.decodeFromString<NewsNotificationPayload>(Uri.decode(it))
                            } catch (e: Exception) {
                                // Fallback for old simple News JSON if needed, or just handle error
                                try {
                                    val news = Json.decodeFromString<News>(Uri.decode(it))
                                    NewsNotificationPayload(news)
                                } catch (e2: Exception) {
                                    null
                                }
                            }
                        }
                        
                        LaunchedEffect(payload) {
                            if (payload != null) {
                                mainViewModel.selectNews(payload.news, payload.tabKey)
                                navHost.navigate("screen1") {
                                    popUpTo("screen1") { inclusive = true }
                                }
                            }
                        }
                    }
                }

//                // Destination content.
//                when (topLevelBackStack) {
//                    AppDestinations.HOME -> ShowHomeRoute()
//                    AppDestinations.FAVORITES -> NewsTagRoute()
//                    else -> {}
//                }
            }

//ver 2
//            val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }
//            Scaffold(
//                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
//                    .fillMaxSize(),
//                bottomBar = {
//                    NavigationBar {
//                        TOP_LEVEL_ROUTES.forEach { topLevelRoute ->
//
//                            val isSelected = topLevelRoute == topLevelBackStack.topLevelKey
//                            NavigationBarItem(
//                                selected = isSelected,
//                                onClick = {
//                                    topLevelBackStack.addTopLevel(topLevelRoute)
//                                },
//                                icon = {
//                                    Icon(
//                                        imageVector = topLevelRoute.icon,
//                                        contentDescription = null
//                                    )
//                                }
//                            )
//                        }
//                    }
//                },
//                topBar = {
//                    MediumTopAppBar(
//                        title = { Text(text = "Scroll Behavior Test") },
//                        navigationIcon = {
//                            IconButton(onClick = { /*TODO*/ }) {
//                                Icon(imageVector = Icons.Default.Menu, contentDescription = "")
//                            }
//                        },
//                        scrollBehavior = scrollBehavior
//                    )
//                }
//            ) { innerPadding ->
//
//                NavDisplay(
//                    backStack = topLevelBackStack.backStack,
//                    onBack = { topLevelBackStack.removeLast() },
//                    entryProvider = entryProvider {
//                        entry<Home>{
//                            ShowHomeRoute()
//                        }
//                        entry<ChatList>{
//                            NewsTagRoute()
//                        }
//                        entry<ChatDetail>{ key ->
//                            NewsDetailScreen (key.id.Title,{topLevelBackStack.addTopLevel()})
//                        }
//                    },
//                )

            //ver1
//                val graph =
//                    navController.createGraph(startDestination = Screen.Home.rout) {
//                        composable(route = Screen.Cart.rout) {
//                            NewsTagRoute()
//                        }
//                        composable(route = Screen.Setting.rout) {
//                            NewsTagRoute()
//                        }
//                        composable(route = Screen.Home.rout) {
//                            ShowHomeRoute()
//                        }
//                        composable(route = Screen.Profile.rout) {
//                            ShowHomeRoute()
//                        }
//                    }
//                NavHost(
//                    navController = navController,
//                    graph = graph,
//                    modifier = Modifier.padding(innerPadding)
//                )

//            }


            }
            if (isXrDevice) {
                SpaceToggleButton(
                    modifier = Modifier.padding(16.dp)
                )
            }
            }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.data?.host == "login" || intent?.getStringExtra("navigate_route") == "login") {
            mainViewModel.setTargetRoute("login")
        }
        
        // Handle deep link link parameter
        intent?.data?.getQueryParameter("link")?.let { link ->
            mainViewModel.setRssItemJumpByLink(link)
        }

        val rssItem = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra("rss_item", RssItemEntity::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<RssItemEntity>("rss_item")
        }
        rssItem?.let { 
            mainViewModel.setRssItemJump(it)
            mainViewModel.setRssJump(it.siteId, it.siteGroup)
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("MainActivity", "Notification permission already granted")
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun initFcm() {
        FirebaseMessaging.getInstance().subscribeToTopic("all")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("MainActivity", "Subscribed to 'all' topic")
                } else {
                    Log.e("MainActivity", "Failed to subscribe to 'all' topic", task.exception)
                }
            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            Log.d("MainActivity", "FCM registration token: $token")
        }
    }

    private fun handleExitBackPress() {
        val now = SystemClock.elapsedRealtime()
        val previousPress = lastExitBackPressAt

        if (previousPress != null && now - previousPress <= EXIT_CONFIRMATION_WINDOW_MILLIS) {
            finish()
        } else {
            lastExitBackPressAt = now
            Toast.makeText(this, R.string.press_back_again_to_exit, Toast.LENGTH_SHORT).show()
        }
    }

    private companion object {
        const val EXIT_CONFIRMATION_WINDOW_MILLIS = 2_000L
        const val XR_SPATIAL_FEATURE = "android.software.xr.api.spatial"
    }
}

@Composable
fun Item(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))

    )
}
class TopLevelBackStack<T: Any>(startKey: T) {

    // Maintain a stack for each top level route
    private var topLevelStacks : LinkedHashMap<T, SnapshotStateList<T>> = linkedMapOf(
        startKey to mutableStateListOf(startKey)
    )

    // Expose the current top level route for consumers
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Expose the back stack so it can be rendered by the NavDisplay
    val backStack = mutableStateListOf(startKey)

    private fun updateBackStack() =
        backStack.apply {
            clear()
            addAll(topLevelStacks.flatMap { it.value })
        }

    fun addTopLevel(key: T){

        // If the top level doesn't exist, add it
        if (topLevelStacks[key] == null){
            topLevelStacks.put(key, mutableStateListOf(key))
        } else {
            // Otherwise just move it to the end of the stacks
            topLevelStacks.apply {
                remove(key)?.let {
                    put(key, it)
                }
            }
        }
        topLevelKey = key
        updateBackStack()
    }

    fun add(key: T){
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast(){
        val removedKey = topLevelStacks[topLevelKey]?.removeLastOrNull()
        // If the removed key was a top level key, remove the associated top level stack
        topLevelStacks.remove(removedKey)
        topLevelKey = topLevelStacks.keys.last()
        updateBackStack()
    }
}

fun navigateWithBackStackHandling(route: String, navHost: NavHostController) {
    navHost.navigate(route) {
        popUpTo(navHost.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun navigationSuiteItems(
    currentDestination: NavDestination?,
    navHost: NavHostController
): NavigationSuiteScope.() -> Unit = {
    item(
        selected = currentDestination?.hierarchy?.any { it.route == "screen1" } == true,
        onClick = {
            navigateWithBackStackHandling("screen1", navHost)
        },
        label = { Text("#HOME") },
        icon = {
            Icon(
                imageVector = AppDestinations.HOME.icon, contentDescription = "HOME"
            )
        },
    )

    item(
        selected = currentDestination?.hierarchy?.any { it.route == "screen_foryou" } == true,
        onClick = {
            navigateWithBackStackHandling("screen_foryou", navHost)
        },
        label = { Text("#FOR YOU") },
        icon = {
            Icon(
                imageVector = Icons.Default.Face, contentDescription = "FOR YOU"
            )
        },
    )

    item(
        selected = currentDestination?.hierarchy?.any { it.route?.split("?")?.firstOrNull() == "screen_rss" } == true,
        onClick = {
            navigateWithBackStackHandling("screen_rss", navHost)
        },
        label = { Text("#RSS") },
        icon = {
            Icon(
                imageVector = Icons.Default.RssFeed, contentDescription = "RSS NEWS"
            )
        },
    )



    item(
        selected = currentDestination?.hierarchy?.any { it.route == "screen2" } == true,
        onClick = {
            navigateWithBackStackHandling("screen2", navHost)
        },
        label = { Text("#TAG") },
        icon = {
            Icon(
                imageVector = Icons.Default.Tag, contentDescription = "TAG"
            )
        },
    )



    item(
        selected = currentDestination?.hierarchy?.any { it.route == "account" } == true,
        onClick = {
            navigateWithBackStackHandling("account", navHost)
        },
        label = { Text("#ACCOUNT") },
        icon = {
            Icon(
                imageVector = Icons.Default.Person, contentDescription = "ACCOUNT"
            )
        },
    )
}

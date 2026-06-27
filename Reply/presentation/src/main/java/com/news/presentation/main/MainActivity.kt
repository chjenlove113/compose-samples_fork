package com.news.presentation.main

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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
import com.news.presentation.base.NewsShoHomeNavigation
import com.news.presentation.base.NewsTagNavigation
import com.news.presentation.base.TopLevelRoute
import com.news.presentation.newsTag.NewsTagRoute
import com.news.presentation.newsTag.NewsTagScreen
import com.news.presentation.showHome.ShowHomeScreen
import com.news.presentation.showHomeRSS.ShowHomeRSSFeedScreen
import android.net.Uri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.news.presentation.showHome.ShowHomeRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.createGraph
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.news.domain.models.News
import com.news.presentation.account.AccountInfoScreen
import com.news.presentation.account.AppUserSiteScreen
import com.news.presentation.account.LoginScreen
import com.news.presentation.account.RegisterScreen
import com.news.presentation.base.BottomNavigationBar
import com.news.presentation.base.Screen
import com.news.presentation.base.Destination
import com.news.presentation.base.bottomNavItems
import com.news.presentation.components.NewsDetailScreen
import com.news.presentation.showHome.ShowHomeRoute2
import com.news.presentation.showHomeForYou.ShowHomeForYouRoute
import com.news.presentation.showHomeChild.ShowHomeChildRoute
import androidx.compose.material.icons.filled.Person
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.news.presentation.theme.ContrastAwareReplyTheme
import androidx.activity.viewModels

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

    @SuppressLint("RestrictedApi")
    @OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            val nightMode by mainViewModel.nightMode.collectAsStateWithLifecycle()
            val fontScale by mainViewModel.fontScale.collectAsStateWithLifecycle()

            ContrastAwareReplyTheme(
                darkTheme = nightMode,
                fontScale = fontScale
            ) {
                val navController = rememberNavController()
            val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
            //val topLevelBackStack = remember { TopLevelBackStack<Any>(Home) }
            var topLevelBackStack by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

            val navHost = rememberNavController()
            val navBackStackEntry by navHost.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

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
                    startDestination = "screen1"
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
                        route = "screen_rss"
                    ) {
                        ShowHomeRSSFeedScreen()
                    }

                    composable(
                        route = "account"
                    ) {
                        AccountInfoScreen(
                            onNavigateToLogin = { navHost.navigate("login") },
                            onNavigateToUserSites = { navHost.navigate("user_sites") }
                        )
                    }

                    composable(
                        route = "user_sites"
                    ) {
                        AppUserSiteScreen(
                            onNavigateToLogin = { navHost.navigate("login") },
                            onNavigateToRss = { id, group ->
                                mainViewModel.setRssJump(id, group)
                                navigateWithBackStackHandling("screen_rss", navHost)
                            }
                        )
                    }

                    composable(
                        route = "login",
                        deepLinks = listOf(
                            navDeepLink { uriPattern = "reply://github-auth?code={code}" }
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
        }
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
                imageVector = AppDestinations.SHOPPING.icon, contentDescription = "RSS NEWS"
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
                imageVector = AppDestinations.FAVORITES.icon, contentDescription = "TAG"
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

package com.app.tintuccongnghe.base

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import kotlinx.serialization.Serializable

data class TopLevelRoute<T : Any>(val name: String, val route: T, val icon: ImageVector)

@Serializable data class NewsTagNavigation(val tagSlug: String? = null)

@Serializable object NewsShoHomeNavigation


@Composable
fun BottomNavigationBar(
    navController: NavController
){
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }

    val navigationItems = listOf(
        NavigationItem(
            title = "Home",
            icon = Icons.Default.Home,
            route = Screen.Home.rout
        ),
        NavigationItem(
            title = "Profile",
            icon = Icons.Default.Person,
            route = Screen.Profile.rout
        ),
        NavigationItem(
            title = "Cart",
            icon = Icons.Default.ShoppingCart,
            route = Screen.Cart.rout
        ),
        NavigationItem(
            title = "Setting",
            icon = Icons.Default.Settings,
            route = Screen.Setting.rout
        )
    )
    NavigationBar(
        containerColor = Color.White
    ) {
        navigationItems.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    selectedNavigationIndex.intValue = index
                    navController.navigate(item.route)
                },
                icon = {
                    Icon(imageVector = item.icon, contentDescription = item.title)
                },
                label = {
                    Text(
                        item.title,
                        color = if (index == selectedNavigationIndex.intValue)
                            Color.Black
                        else Color.Gray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.surface,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )

            )
        }
    }

}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

sealed class Screen(val rout: String) {
    object Home: Screen("home_screen")
    object Profile: Screen("profile_screen")
    object Cart: Screen("cart_screen")
    object Setting: Screen("setting_screen")
}

///nav 3
sealed class Destination(val route: String) {
    object Home : Destination("home")
    object ListRoot : Destination("list_root") // The list screen itself
    object Detail : Destination("detail/{itemId}") { // The detail screen, needs argument
        fun createRoute(itemId: String) = "detail/$itemId"
    }
    object Profile : Destination("profile")
}

val bottomNavItems = listOf(
    Destination.Home,
    Destination.ListRoot,
    Destination.Profile
)
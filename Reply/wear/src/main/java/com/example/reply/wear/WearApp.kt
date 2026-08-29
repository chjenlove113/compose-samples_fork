/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.reply.wear

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.TimeText
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.reply.wear.rss.RssScreen
import com.example.reply.wear.rss.RssScreenMaster

@Composable
fun WearApp() {
    MaterialTheme {
        val navController = rememberSwipeDismissableNavController()
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = "rss_master"
        ) {
            composable("rss_master") {
                RssScreenMaster(
                    onAllClick = {
                        navController.navigate("rss_details/all/all/all/Tất cả")
                    },
                    onSiteClick = { siteId, siteGroup, siteKind, siteName ->
                        navController.navigate("rss_details/$siteId/$siteGroup/$siteKind/$siteName")
                    }
                )
            }
            composable(
                route = "rss_details/{siteId}/{siteGroup}/{siteKind}/{siteName}",
                arguments = listOf(
                    navArgument("siteId") { type = NavType.StringType },
                    navArgument("siteGroup") { type = NavType.StringType },
                    navArgument("siteKind") { type = NavType.StringType },
                    navArgument("siteName") { type = NavType.StringType }
                )
            ) {
                RssScreen()
            }
        }
    }
}

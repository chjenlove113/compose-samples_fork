package com.news.presentation.showHomeChild

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.news.domain.models.AppSite
import com.news.domain.models.AppSiteCateByGroup
import com.news.domain.models.News
import com.news.domain.models.ShowHomeDataModel
import com.news.presentation.R
import com.news.presentation.base.ShowError
import com.news.presentation.base.ShowLoading
import com.news.presentation.base.TabItem
import com.news.presentation.base.UiState
import com.news.presentation.components.NewsDetailScreen
import com.news.presentation.showHome.AutoAdvancePager
import com.news.presentation.showHome.EventItem
import com.news.presentation.showHome.ExploreContent
import com.news.presentation.showHome.ItemDetail
import com.news.presentation.showHome.ItemDetailSite
import kotlinx.coroutines.launch

@Composable
fun ShowHomeChildRoute(
    viewModel: ShowHomeChildViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ShowHomeChildScreen(uiState, viewModel, {})
}

@Composable
fun ShowHomeChildScreen(
    uiState: UiState<ShowHomeDataModel>,
    viewModel: ShowHomeChildViewModel?,
    onNewsClicked: (News) -> Unit,
) {

    when(uiState) {
        is UiState.Loading -> {
            ShowLoading()
        }
        is UiState.Error -> {
            ShowError(
                text = stringResource(R.string.something_went_wrong),
                retryEnabled = true
            ) { viewModel?.fetchShowHome() }
        }

        is UiState.Success -> {
            Column {
                Text("${viewModel?.navKey?.slug}")

                ExploreContentChild(
                    uiState.data,
                    {  },
                    {  })
            }


        }
    }

}

@Composable
fun ExploreContentChild(allEventCategories: ShowHomeDataModel, onEventClick: (News) -> Unit, onEventClickSiteName: (AppSite) -> Unit) {
    LazyColumn {
        item {
            AutoAdvancePager(allEventCategories.LstNewsHeader ?: emptyList())
        }
        allEventCategories.CategoryViewModel.AppSiteCateByGroup?.forEach { (catId, catName, zz, yy,catSlug,catKey) ->
            EventItem(catId, catName, catSlug,yy ?: emptyList(), onEventClick,onEventClickSiteName,{})
        }
    }

    //Dynamic TabLayoutScreen(allEventCategories.CategoryViewModel.AppSiteCateByGroup)
}

@Composable
fun DynamicTabLayoutScreen(AppSiteCateByGroup: ArrayList<AppSiteCateByGroup>?) {

    val initialArrayList = arrayListOf<TabItem>()
    AppSiteCateByGroup?.forEach {
        initialArrayList.add(TabItem(title = it.Name, screen = { NewsDetailScreen(name = it.Name, onItemClick = {}, goToEx = {}) }))
    }

    // Dynamic list of tabs
    val tabs = remember {
        initialArrayList
//        mutableStateListOf(
//            //TabItem(title = "Home", screen = { NewsDetailScreen(name = "Home", onItemClick = {}, goToEx = {}) }),
//            //TabItem(title = "Settings", screen = { NewsDetailScreen(name = "Settings", onItemClick = {}, goToEx = {})  }),
//            //TabItem(title = "Profile", screen = { NewsDetailScreen(name = "Profile", onItemClick = {}, goToEx = {}) })
//            // More tabs can be added dynamically to this list
//
//        )
    }

    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    Column {
        // Tab Row implementation
        TabRow(selectedTabIndex = pagerState.currentPage) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = tab.title) }
                )
            }
        }

        // Horizontal Pager implementation (The ViewPager equivalent)
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Display the content (screen composable) for the current page/tab
            tabs[page].screen()
        }
    }

    // Synchronize pager swipes with the TabRow indicator
    LaunchedEffect(pagerState.currentPage) {
        // No explicit synchronization is needed here because `selectedTabIndex`
        // in `TabRow` is already observing `pagerState.currentPage`
    }
}

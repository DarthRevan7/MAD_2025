package com.example.voyago.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripListViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.text.font.FontWeight


@Composable
fun FilterSelection(navController: NavController, vm: TripListViewModel = viewModel(factory = Factory)) {
    Scaffold(
        topBar = {
            TopBar()
        },
        bottomBar = {
            BottomBar(1)
        }
    ) { innerPadding ->

        val listState = rememberLazyListState()
        var query by remember { mutableStateOf("") }
        var selectedDestination by remember { mutableStateOf<String?>(null) }

        var allDestinations = vm.allDestinations()

        val filteredSuggestions = remember(query) {
            allDestinations.filter {
                it.contains(query, ignoreCase = true)
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    DestinationSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        onSearch = { result ->
                            selectedDestination = result
                            query = result
                        },
                        searchResults = filteredSuggestions,
                        onResultClick = { result ->
                            selectedDestination = result
                            query = result
                        }
                    )
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DestinationSearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    searchResults: List<String>,
    onResultClick: (String) -> Unit,
    placeholder: @Composable () -> Unit = { Text("Search Destination") },
    leadingIcon: @Composable (() -> Unit)? = { Icon(Icons.Default.Search, contentDescription = "Search") },
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingContent: (@Composable (String) -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null
) {
    var active by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier
            .fillMaxWidth()
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            query = query,
            onQueryChange = onQueryChange,
            onSearch = {
                onSearch(query)
                active = false
            },
            active = active,
            onActiveChange = { active = it },
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(searchResults) { resultText ->
                    ListItem(
                        headlineContent = { Text(resultText) },
                        supportingContent = supportingContent?.let { { it(resultText) } },
                        leadingContent = leadingContent,
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .clickable {
                                onResultClick(resultText)
                                active = false
                            }
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

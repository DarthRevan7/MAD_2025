package com.example.voyago.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.MutableState
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

        vm.setMaxMinPrice()

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

            item {
                Text(
                    text = "Price Range:",
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                RangeSlider(vm)
            }

            item {
                Spacer(modifier = Modifier.padding(16.dp))
            }

            item {
                DropdownMenu("Duration")
            }

            item {
                DropdownMenu("Group Size")
            }

            item {
                DropdownMenu("Trip Type")
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

@Composable
fun RangeSlider(vm: TripListViewModel = viewModel(factory = Factory)) {
    var sliderPosition by remember { mutableStateOf(vm.getMinPrice().toFloat()..vm.getMaxPrice().toFloat()) }

    Column(
        modifier = Modifier.padding(top = 10.dp, start = 25.dp, end = 25.dp)
    ) {
        RangeSlider(
            value = sliderPosition,
            //steps = ((vm.getMaxPrice()-vm.getMinPrice())/100).toInt(),
            onValueChange = { range -> sliderPosition = range },
            valueRange = vm.getMinPrice().toFloat()..vm.getMaxPrice().toFloat(),
            onValueChangeFinished = {
                // launch some business logic update with the state you hold
                // viewModel.updateSelectedSliderValue(sliderPosition)
            },
        )
        Text(text = sliderPosition.toString())
    }
}

@Composable
fun DropdownMenu(filter: String) {
    var expanded by remember { mutableStateOf(false) }
    var strText by remember { mutableStateOf(filter) }
    Box(
        modifier = Modifier
            .wrapContentSize()
            .padding(start = 16.dp, bottom = 8.dp)
    ) {

        Button(
            onClick = { expanded = !expanded },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xd9, 0xd9, 0xd9, 255)
            )
        ) {

            Text(text = strText, color = Color.Black)
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "check", tint = Color.Gray)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (filter == "Duration") {
                DropdownMenuItem(
                    text = { Text("No Filter") },
                    onClick = { strText = "Duration"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("1-3 days") },
                    onClick = { strText = "1-3 days"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("3-5 days") },
                    onClick = { strText = "3-5 days"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("5-7 days") },
                    onClick = { strText = "5-7 days"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("7-10 days") },
                    onClick = { strText = "7-10 days"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("10-15 days") },
                    onClick = { strText = "10-15 days"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("15-20 days") },
                    onClick = { strText = "15-20 days"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("> 20 days") },
                    onClick = { strText = "> 20 days"; expanded = !expanded }
                )
            } else if (filter == "Group Size") {
                DropdownMenuItem(
                    text = { Text("No Filter") },
                    onClick = { strText = "Group Size"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("2-3 people") },
                    onClick = { strText = "2-3 people"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("3-5 people") },
                    onClick = { strText = "3-5 people"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("5-7 people") },
                    onClick = { strText = "5-7 people"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("7-10 people") },
                    onClick = { strText = "7-10 people"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("10-15 people") },
                    onClick = { strText = "10-15 people"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("> 15 people") },
                    onClick = { strText = "> 15 people"; expanded = !expanded }
                )
            } else if (filter == "Trip Type") {
                DropdownMenuItem(
                    text = { Text("No Filter") },
                    onClick = { strText = "Trip Type"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("Adventure") },
                    onClick = { strText = "Adventure"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("Culture") },
                    onClick = { strText = "Culture"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("Party") },
                    onClick = { strText = "Party"; expanded = !expanded }
                )
                DropdownMenuItem(
                    text = { Text("Relax") },
                    onClick = { strText = "Relax"; expanded = !expanded }
                )
            }
        }
    }
}
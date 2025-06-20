package com.example.voyago.view

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.RangeSlider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.PopupProperties
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel


data class SelectableItem(
    val label: String,
    val min: Int,
    val max: Int,
    val typeTravel: TypeTravel? = null,
    var isSelected: Boolean = false
)

@Composable
fun FiltersSelection(navController: NavController,
                     vm: TripViewModel = viewModel(factory = Factory),
                     uvm: UserViewModel
) {

    val listState = rememberLazyListState()
    var query by remember { mutableStateOf("") }
    var selectedDestination by remember { mutableStateOf<String?>(null) }

    //List of all the destinations present in the database
    val allDestinations by vm.allDestinations().collectAsState(initial = emptyList())

    //Suggestions the appear when you search
    val filteredSuggestions = remember(query, allDestinations) {
        allDestinations.filter {
            it.contains(query, ignoreCase = true)
        }
    }


    val isCompletedSelected = vm.filterCompletedTrips
    val isCanJoinSelected = vm.filterUpcomingTrips

    LaunchedEffect(Unit) {
        vm.setMaxMinPrice()
    }


    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        //Destination Search Bar
        item {
            Box(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                DestinationSearchBar(
                    query = vm.filterDestination,
                    onQueryChange = { vm.updateFilterDestination(it) },
                    onSearch = { result ->
                        selectedDestination = result
                        query = result
                    },
                    searchResults = filteredSuggestions,
                    onResultClick = { result ->
                        vm.updateFilterDestination(result)
                    },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                )
            }
        }

        //Price Range Slider
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

        //Duration dropdown
        item {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 16.dp, bottom = 8.dp)
            ) {

                MultiSelectDropdownMenu("Duration", vm = vm, items = vm.durationItems
                ) { updatedItems ->
                    vm.updateFilterDuration(updatedItems)
                }

            }
        }

        //Group Size Dropdown
        item {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 16.dp, bottom = 8.dp)
            ) {

                MultiSelectDropdownMenu("Group Size", vm = vm, items = vm.groupSizeItems
                ) { updatedItems ->
                    vm.updateFilterGroupSize(updatedItems)
                }

            }
        }

        //Trip Type Dropdown
        item {
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = 16.dp)
            ) {

                MultiSelectDropdownMenu("Trip Type", vm = vm, items = vm.filtersTripType
                ) { updatedItems ->
                    vm.updateFiltersTripType(updatedItems)
                }

            }
        }

        item {
            Spacer(Modifier.padding(2.dp))
        }

        if (!isCompletedSelected) {
            //Search in trips you can join
            item {
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            vm.updateUpcomingTripsFilter(!vm.filterUpcomingTrips)
                        }
                    ) {
                        Icon(
                            imageVector = if (isCanJoinSelected) {
                                Icons.Default.CheckBox
                            } else {
                                Icons.Default.CheckBoxOutlineBlank
                            },
                            contentDescription = if (isCanJoinSelected) {
                                "Selected"
                            } else {
                                "NotSelected"
                            },
                            tint = if (isCanJoinSelected) {
                                Color(0x65, 0x55, 0x8f, 255)
                            } else {
                                Color.Black
                            }
                        )
                    }
                    Text("Show only trips you can join")
                }
            }
        }

        if (!isCanJoinSelected) {
            //Search in completed trips
            item {
                Row(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            vm.updateCompletedTripsFilter(!vm.filterCompletedTrips)
                        }
                    ) {
                        Icon(
                            imageVector = if (isCompletedSelected) {
                                Icons.Default.CheckBox
                            } else {
                                Icons.Default.CheckBoxOutlineBlank
                            },
                            contentDescription = if (isCompletedSelected) {
                                "Selected"
                            } else {
                                "NotSelected"
                            },
                            tint = if (isCompletedSelected) {
                                Color(0x65, 0x55, 0x8f, 255)
                            } else {
                                Color.Black
                            }
                        )
                    }
                    Text("Show only in completed trips")
                }
            }
        }

        item {
            Spacer(Modifier.padding(4.dp))
        }

        if (!isCompletedSelected) {
            //Seats filter
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 30.dp)
                ) {
                    Text(text = "Min available seats: ", modifier = Modifier.padding(end = 8.dp))
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .border(width = 1.dp, color = Color.Gray, shape = CircleShape)
                    ) {
                        IconButton(
                            onClick = { if (vm.filterBySeats > 0) vm.updateFilterBySeats(vm.filterBySeats - 1) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease"
                            )
                        }
                    }
                    Text(
                        text = vm.filterBySeats.toString(),
                        modifier = Modifier.padding(horizontal = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(25.dp)
                            .border(width = 1.dp, color = Color.Gray, shape = CircleShape)
                    ) {
                        IconButton(
                            onClick = { vm.updateFilterBySeats(vm.filterBySeats + 1) },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase"
                            )
                        }
                    }
                }
            }
        }

        //Search Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                Button(
                    onClick = {
                        vm.userAction = TripViewModel.UserAction.SEARCHING
                        vm.applyFilters(uvm.loggedUser.value.id)
                        navController.popBackStack() //reset backstack
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                ) {
                    Text(text = "Search")
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
                .fillMaxWidth()
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
fun RangeSlider(vm: TripViewModel) {
    // Get the price range bounds and selected range from the ViewModel
    val priceBounds = vm.priceBounds.value
    val selectedPriceRange = vm.selectedPriceRange.value

    // Get the min and max values for the slider
    val minPrice = priceBounds.start
    val maxPrice = priceBounds.endInclusive

    // Slider value range
    val valueRange = minPrice..maxPrice

    Column(
        modifier = Modifier.padding(top = 10.dp, start = 25.dp, end = 25.dp)
    ) {
        // Create a RangeSlider
        RangeSlider(
            value = selectedPriceRange,
            onValueChange = { newRange ->
                // Update the selected price range in the ViewModel
                vm.updateUserSelection(newRange)
            },
            valueRange = valueRange, // The full range of values (from min to max price)
            onValueChangeFinished = {
                vm.updateFilterPriceRange(
                    minPrice = vm.selectedPriceRange.value.start.toDouble(),
                    maxPrice = vm.selectedPriceRange.value.endInclusive.toDouble()
                )
            }
        )
        // Display the selected range for the user
        Text(text = "%.0f € - %.0f €".format(selectedPriceRange.start, selectedPriceRange.endInclusive))
    }
}

@Composable
fun MultiSelectDropdownMenu(filter:String, vm: TripViewModel,
    items: List<SelectableItem>,
    onSelectionChange: (List<SelectableItem>) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val itemStates = remember {
        items.map { mutableStateOf(it.isSelected) }
    }

    Column(modifier = Modifier.padding(4.dp)) {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xd9, 0xd9, 0xd9, 255))
        ){
            Text(filter, color = Color.Black)
            Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "check", tint = Color.Gray)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false
                if (filter == "Duration") {
                    vm.updateFilterDuration(items)
                    //println("Dropdown update: ${vm.filterDuration.first} - ${vm.filterDuration.second}")
                } else if (filter == "Group Size") {
                    vm.updateFilterGroupSize(items)
                } else if (filter == "Trip Type") {
                    vm.updateFiltersTripType(items)
                }
           },
            properties = PopupProperties(focusable = true)
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item.label) },
                    onClick = {
                        itemStates[index].value = !itemStates[index].value
                        onSelectionChange(
                            items.mapIndexed { idx, it ->
                                it.copy(isSelected = itemStates[idx].value)
                            }
                        )
                    },
                    leadingIcon = {
                        Checkbox(
                            checked = itemStates[index].value,
                            onCheckedChange = { isChecked ->
                                itemStates[index].value = isChecked
                                onSelectionChange(
                                    items.mapIndexed { idx, it ->
                                        it.copy(isSelected = itemStates[idx].value)
                                    }
                                )
                            }
                        )
                    }
                )
            }
        }
    }
}
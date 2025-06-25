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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.Factory
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel

// SelectableItem for Type Travel data structure
data class SelectableItem(
    val label: String,
    val min: Int,
    val max: Int,
    val typeTravel: TypeTravel? = null,
    var isSelected: Boolean = false
)

// Screen that lets users select filters
@Composable
fun FiltersSelection(
    navController: NavController, vm: TripViewModel = viewModel(factory = Factory),
    uvm: UserViewModel
) {

    // Remember scrolling state of the list
    val listState = rememberLazyListState()

    // Local state to manage user-typed search query and selection
    var query by rememberSaveable { mutableStateOf("") }
    var selectedDestination by remember { mutableStateOf<String?>(null) }

    //List of all the destinations present in the database
    val allDestinations by vm.allDestinations().collectAsState(initial = emptyList())

    // Filter destination suggestions based on current query
    val filteredSuggestions = rememberSaveable(query, allDestinations) {
        allDestinations.filter {
            it.contains(query, ignoreCase = true)
        }
    }

    // Track toggle states from the ViewModel
    val isCompletedSelected = vm.filterCompletedTrips
    val isCanJoinSelected = vm.filterUpcomingTrips

    // Initialize min/max prices when screen is composed
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

                MultiSelectDropdownMenu(
                    "Duration", vm = vm, items = vm.durationItems
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

                MultiSelectDropdownMenu(
                    "Group Size", vm = vm, items = vm.groupSizeItems
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

                MultiSelectDropdownMenu(
                    "Trip Type", vm = vm, items = vm.filtersTripType
                ) { updatedItems ->
                    vm.updateFiltersTripType(updatedItems)
                }

            }
        }

        item {
            Spacer(Modifier.padding(2.dp))
        }

        // Only Show joinable Trips if not viewing completed trips
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

        // Only Show Completed Trips if not showing joinable trips
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

        // Minimum Available Seats Filter
        if (!isCompletedSelected) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(start = 30.dp)
                ) {
                    Text(text = "Min available seats: ", modifier = Modifier.padding(end = 8.dp))

                    // Decrease button
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

                    // Increase button
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
                        vm.applyFilters(uvm.loggedUser.value.id)    // Apply selected filters
                        navController.popBackStack() // Navigate back
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

// A composable search bar component specifically for searching destinations
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
    leadingIcon: @Composable (() -> Unit)? = {
        Icon(
            Icons.Default.Search,
            contentDescription = "Search"
        )
    },
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingContent: (@Composable (String) -> Unit)? = null,
    leadingContent: (@Composable () -> Unit)? = null
) {

    // Keeps track of whether the search bar is currently active
    var active by rememberSaveable { mutableStateOf(false) }

    // The outer container of the search bar, taking full width and marked as a traversal group for accessibility
    Box(
        modifier
            .fillMaxWidth()
            .semantics {
                isTraversalGroup = true
            }  // Groups the content for accessibility navigation
    ) {
        // Search bar Component
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .semantics { traversalIndex = 0f },     // Accessibility order
            query = query,                              // Current text in the search field
            onQueryChange = onQueryChange,              // Called when user types
            onSearch = {
                onSearch(query)                         // Trigger search callback
                active = false                          // Close suggestions when search is executed
            },
            active = active,                            // Whether the bar is showing suggestions
            onActiveChange = {
                active =
                    it                             // Callback for expanding/collapsing the search
            },
            placeholder = placeholder,                  // Placeholder shown when input is empty
            leadingIcon = leadingIcon,                  // Icon before the text field
            trailingIcon = trailingIcon,                // Icon at the end of the search field
        ) {

            // LazyColumn is used to show a scrollable list of search suggestions
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp)  // Max height to avoid covering the screen
            ) {
                // For each result in the search results list, show a ListItem
                items(searchResults) { resultText ->
                    ListItem(
                        headlineContent = { Text(resultText) }, // Main text shown
                        supportingContent = supportingContent?.let { { it(resultText) } }, // Subtext
                        leadingContent = leadingContent,    // Icon or image before suggestion
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),   // No background color
                        modifier = Modifier
                            .clickable {
                                onResultClick(resultText)   // Trigger selection callback
                                active = false              // Close search suggestions
                            }
                            .fillMaxWidth()
                            .padding(
                                horizontal = 16.dp,
                                vertical = 4.dp
                            )   // Space around each list item
                    )
                }
            }
        }
    }
}

// Composable that shows the RangeSlider component
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
        Text(
            text = "%.0f € - %.0f €".format(
                selectedPriceRange.start,
                selectedPriceRange.endInclusive
            )
        )
    }
}

// A multi-select dropdown menu component used to filter trips
@Composable
fun MultiSelectDropdownMenu(
    filter: String, vm: TripViewModel,
    items: List<SelectableItem>,
    onSelectionChange: (List<SelectableItem>) -> Unit,
) {

    // Tracks whether the dropdown menu is currently expanded
    var expanded by remember { mutableStateOf(false) }

    // Holds the selection state of each item in the list
    val itemStates = remember {
        // Create a list of mutable states corresponding to each item's isSelected value
        items.map { mutableStateOf(it.isSelected) }
    }

    // Vertical layout with padding
    Column(modifier = Modifier.padding(4.dp)) {
        // Button to open the dropdown menu
        Button(
            onClick = { expanded = true },  // When clicked, expand the dropdown
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xd9, 0xd9, 0xd9, 255))
        ) {
            Text(filter, color = Color.Black)
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,  // Downward arrow icon
                contentDescription = "check",
                tint = Color.Gray
            )
        }

        // Dropdown menu containing multiple checkboxes
        DropdownMenu(
            expanded = expanded,    // Show or hide the menu
            onDismissRequest = {
                expanded = false    // Collapse the menu when user taps outside

                // Update ViewModel filter states when menu is closed,
                // based on the type of filter that was used
                if (filter == "Duration") {
                    vm.updateFilterDuration(items)
                } else if (filter == "Group Size") {
                    vm.updateFilterGroupSize(items)
                } else if (filter == "Trip Type") {
                    vm.updateFiltersTripType(items)
                }
            },
            properties = PopupProperties(focusable = true)  // Allows the menu to receive focus
        ) {
            // For each item in the dropdown, show a row with a label and checkbox
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = { Text(item.label) },    // Show the item label
                    onClick = {
                        // Toggle the selection state when row is clicked
                        itemStates[index].value = !itemStates[index].value

                        // Invoke callback with updated list
                        onSelectionChange(
                            items.mapIndexed { idx, it ->
                                it.copy(isSelected = itemStates[idx].value)
                            }
                        )
                    },
                    leadingIcon = {
                        // Checkbox shown before each item
                        Checkbox(
                            checked = itemStates[index].value,  // Current checked state
                            onCheckedChange = { isChecked ->
                                itemStates[index].value = isChecked     // Update checkbox state

                                // Invoke callback with new item selection states
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
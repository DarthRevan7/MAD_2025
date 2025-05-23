package com.example.voyago.view

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.voyago.model.Trip
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StarHalf
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.setValue
import com.example.voyago.activities.ProfilePhoto
import com.example.voyago.model.Review
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.window.Popup
import androidx.core.net.toUri
import com.example.voyago.model.Trip.Participant
import com.example.voyago.viewmodel.TripViewModel
import com.example.voyago.viewmodel.UserViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun TripDetails(navController: NavController, vm: TripViewModel, owner: Boolean, uvm: UserViewModel) {
    //Trip that we are showing
    val trip by vm.selectedTrip
    println("selected trip = ${vm.selectedTrip}")

    if (trip == null) {
        Text("Loading trip details...")
        return
    }

    val nonNullTrip = trip!!

    //The user joined the trip but didn't created
    val joined = nonNullTrip.participants.containsKey(1) && nonNullTrip.creatorId != 1

    //Delete confirmation trip
    var showPopup by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    //Manage join request
    val askedTrips: Map<Int, Int> by vm.askedTrips.collectAsState()
    vm.syncAskedTrips()
    val requestedSpots = trip?.id?.let { askedTrips[it] } ?: 0
    val hasAsked = requestedSpots > 0
    var showDialog by remember { mutableStateOf(false) }
    var selectedSpots by remember { mutableIntStateOf(1) }

    //To manage phase of the dialog
    var dialogPhase by remember { mutableIntStateOf(0) }

    // --- INIZIO BLOCCO MODIFICATO ---

    // Data of other participants - Ora usiamo un'unica lista di AdditionalParticipantInput
    val additionalParticipantsData = remember { mutableStateListOf<AdditionalParticipantInput>() }

    // I singoli stati per username, name, surname, email e i loro 'Touched' e 'HasErrors'
    // NON SONO PIÙ NECESSARI QUI a livello globale,
    // perché saranno gestiti all'interno di ogni oggetto AdditionalParticipantInput.
    // Li ho lasciati commentati per chiarezza, ma puoi rimuoverli completamente.

    // var username by rememberSaveable { mutableStateOf("") } // Rimuovi o commenta
    // var usernameTouched = remember {mutableStateOf(false)} // Rimuovi o commenta
    // val usernameHasErrors by remember { // Rimuovi o commenta
    //     derivedStateOf {
    //         usernameTouched.value && (username.isBlank() || !username.any { it.isLetter() })
    //     }
    // }

    // (Commenta o rimuovi anche name, surname, email e i relativi touched/hasErrors)
    // --- FINE BLOCCO MODIFICATO ---

    //Initialization of the list of other participants
    LaunchedEffect(dialogPhase, selectedSpots) {
        if (dialogPhase == 1) {
            // Assicurati che la lista sia della dimensione corretta
            // selectedSpots - 1 perché l'utente corrente è già conteggiato
            while (additionalParticipantsData.size < selectedSpots - 1) {
                additionalParticipantsData.add(AdditionalParticipantInput())
            }
            while (additionalParticipantsData.size > selectedSpots - 1) {
                additionalParticipantsData.remove(additionalParticipantsData.last())
            }
        }
    }

    val listState = rememberLazyListState()

    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val isAfterToday = nonNullTrip.startDate.after(today)
    var publishError by rememberSaveable {mutableStateOf(false)}

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
        ) {
            //Trip image
            item {
                Hero(nonNullTrip)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            //Trip information
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        text = formatTripDate(nonNullTrip.startDate) + " - " +
                                formatTripDate(nonNullTrip.endDate) + "\n " +
                                "${nonNullTrip.groupSize} people" +
                                if (nonNullTrip.availableSpots() > 0) {
                                    " (${nonNullTrip.availableSpots()} spots left)"
                                } else {
                                    ""
                                },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${trip?.estimatedPrice} €",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }
            }

            //The logged in user see a trip created by them in the "My Trip" section
            if (owner) {
                //The trip created by the logged in user is published
                if (nonNullTrip.published && nonNullTrip.status == Trip.TripStatus.NOT_STARTED && nonNullTrip.creatorId == 1) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Box {
                                //Applications Button
                                Button(
                                    onClick = {
                                        navController.navigate("trip_applications")
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x14, 0xa1, 0x55, 255)
                                    )
                                ) {
                                    Text("Applications")
                                }

                                if (nonNullTrip.appliedUsers.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .background(Color.Red, CircleShape)
                                            .align(Alignment.TopEnd)
                                    )
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            //Private Button (makes the trip private)
                            if (!nonNullTrip.participants.any { it.key != nonNullTrip.creatorId }) {
                                Button(
                                    onClick = {
                                        vm.changePublishedStatus(nonNullTrip.id)
                                        vm.updatePublishedTrip()
                                        navController.popBackStack()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x65, 0x55, 0x8f, 255)
                                    )
                                ) {
                                    Text("Private")
                                }
                            }
                            Spacer(Modifier.padding(5.dp))

                            //Delete button with popup for confirmation
                            DeleteButtonWithConfirmation(nonNullTrip, navController, vm)
                        }
                    }
                }

                if (joined) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (nonNullTrip.status == Trip.TripStatus.COMPLETED) {
                                Box {
                                    //Applications Button
                                    Button(
                                        onClick = { navController.navigate("my_reviews") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xd9, 0x24, 0xd6, 255)
                                        )
                                    ) {
                                        Text("My Reviews")
                                    }

                                    if (!vm.isReviewed(1, nonNullTrip.id)) {
                                        Box(
                                            modifier = Modifier
                                                .size(15.dp)
                                                .background(Color(0xFF448AFF), CircleShape)
                                                .align(Alignment.TopEnd)
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.weight(1f))

                            //"Create a Copy" Button (creates a copy of the trip in the logged in user private trips)
                            Button(
                                onClick = {
                                    vm.addImportedTrip(
                                        nonNullTrip.photo,
                                        nonNullTrip.title,
                                        nonNullTrip.destination,
                                        nonNullTrip.startDate,
                                        nonNullTrip.endDate,
                                        nonNullTrip.estimatedPrice,
                                        nonNullTrip.groupSize,
                                        nonNullTrip.activities,
                                        nonNullTrip.typeTravel,
                                        1,
                                        false
                                    )
                                    vm.updatePublishedTrip()

                                    showPopup = true
                                    coroutineScope.launch {
                                        delay(2000)
                                        showPopup = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x65, 0x55, 0x8f, 255)
                                )
                            ) {
                                Text("Create a Copy")
                            }
                        }
                    }
                }

                //The trip created by the logged in user is private
                if (!nonNullTrip.published) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            //Publish Button
                            Button(
                                onClick = {
                                    if (isAfterToday) {
                                        vm.changePublishedStatus(nonNullTrip.id)
                                        vm.updatePublishedTrip()
                                        navController.popBackStack()
                                    } else {
                                        publishError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x14, 0xa1, 0x55, 255)
                                )
                            ) {
                                Text("Publish")
                            }

                            Spacer(Modifier.padding(5.dp))

                            //Delete button with popup for confirmation
                            DeleteButtonWithConfirmation(nonNullTrip, navController, vm)
                        }

                        if (publishError) {
                            Text(
                                text = "The Start Date of the trip must be after today for it to be published.",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    TitleBox("My Itinerary")
                }
            }
            //The logged in user see a published trip in the "Explore" section
            else {
                item {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        //"Create a Copy" Button (creates a copy of the trip in the logged in user private trips)
                        Button(
                            onClick = {
                                vm.addImportedTrip(
                                    nonNullTrip.photo,
                                    nonNullTrip.title,
                                    nonNullTrip.destination,
                                    nonNullTrip.startDate,
                                    nonNullTrip.endDate,
                                    nonNullTrip.estimatedPrice,
                                    nonNullTrip.groupSize,
                                    nonNullTrip.activities,
                                    nonNullTrip.typeTravel,
                                    1,
                                    false
                                )
                                vm.updatePublishedTrip()

                                showPopup = true
                                coroutineScope.launch {
                                    delay(2000)
                                    showPopup = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x65, 0x55, 0x8f, 255)
                            )
                        ) {
                            Text("Create a Copy")
                        }

                        Spacer(Modifier.padding(5.dp))

                        //If the user can join the trip
                        if (nonNullTrip.canJoin() && nonNullTrip.creatorId != 1) {
                            //Ask to Join/Asked to Join Button
                            Button(
                                onClick = {
                                    if (hasAsked) {
                                        vm.cancelAskToJoin(nonNullTrip, 1)
                                    } else {
                                        selectedSpots = 1
                                        showDialog = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasAsked)
                                        Color(0x65, 0xa9, 0x8b, 255)
                                    else
                                        Color(0x14, 0xa1, 0x55, 255)
                                )
                            ) {
                                if (hasAsked) {
                                    Icon(Icons.Default.Check, "check")
                                    Text("Asked to Join ($requestedSpots spot${if (requestedSpots > 1) "s" else ""})")
                                } else {
                                    Text("Ask to Join")
                                }
                            }

                        } else if (nonNullTrip.participants.containsKey(1)
                            && nonNullTrip.status != Trip.TripStatus.COMPLETED
                            && nonNullTrip.creatorId != 1) {
                            Button(
                                onClick = {},
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0x2E, 0x7D, 0x32, 255)
                                )
                            ) {
                                Text("Already Joined")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {
                    TitleBox("Itinerary")
                }
            }

            //The Itinerary of the trip
            item {
                ItineraryText(
                    nonNullTrip,
                    modifier = Modifier
                        .padding(start = 24.dp, top = 16.dp, end = 20.dp)
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                TitleBox("Created by:")
            }

            item {
                ShowParticipants(uvm.getUserData(nonNullTrip.creatorId), 1, navController)
            }

            //Reviews section
            if (vm.getTripReviews(nonNullTrip.id).isNotEmpty()) {
                item {
                    TitleBox("Reviews")
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                //List of reviews of the trip
                items(vm.getTripReviews(nonNullTrip.id)) { review ->
                    ShowReview(review, vm, false, uvm, navController)
                }
            }

            if (nonNullTrip.participants.size > 1) {
                item {
                    TitleBox("Participants:")
                }

                val participantsMap = vm.getTripParticipants(nonNullTrip)

                items(participantsMap.entries.toList()) { entry ->
                    val user = entry.key
                    val spots = entry.value
                    if (nonNullTrip.creatorId != user.id) {
                        ShowParticipants(user, spots, navController)
                    }
                }
            }

        }

        //PopUp that appears when the user creates a copy of the trip
        if (showPopup) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = {
                    showPopup = false
                }
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = 80.dp)
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Default.CheckBox,
                            contentDescription = "check",
                            tint = Color.Green
                        )
                        Spacer(Modifier.padding(5.dp))
                        Text(
                            text = "Copy created in 'My Trips'",
                            color = Color.Black,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }
        }

        //PopUp that appears when the user ask to join a trip
        if (showDialog) {
            val maxSpots = nonNullTrip.availableSpots()

            AlertDialog(
                onDismissRequest = {
                    showDialog = false
                    dialogPhase = 0
                    //unregisteredParticipants.clear()
                    //registeredUsernames.clear()
                },
                title = {
                    if (dialogPhase == 0) {
                        Text("Select number of spots:")
                    } else {
                        Text("Details other participants:")
                    }
                },
                text = {
                    if (dialogPhase == 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (selectedSpots > 1) selectedSpots-- },
                                enabled = selectedSpots > 1
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Decrease")
                            }

                            Text(
                                "$selectedSpots",
                                modifier = Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.headlineMedium
                            )

                            IconButton(
                                onClick = { if (selectedSpots < maxSpots) selectedSpots++ },
                                enabled = selectedSpots < maxSpots
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Increase")
                            }
                        }
                    } else {
                    LazyColumn {
                        //For every participants
                        for (i in 0 until (selectedSpots - 1)) {
                            item {
                                // Accediamo all'elemento specifico della lista per questo partecipante
                                val participantData = additionalParticipantsData[i]

                                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                    Text(
                                        "Partecipante ${i + 1}", // Ho corretto il testo in italiano
                                        style = MaterialTheme.typography.titleMedium
                                    )

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = participantData.isRegistered,
                                            onCheckedChange = { isChecked ->
                                                // AGGIORNA LO STATO NEL MODELLO
                                                additionalParticipantsData[i] =
                                                    participantData.copy(isRegistered = isChecked)
                                                // Resetta i campi rilevanti quando la checkbox cambia
                                                if (isChecked) {
                                                    additionalParticipantsData[i] =
                                                        additionalParticipantsData[i].copy(
                                                            name = "",
                                                            lastName = "",
                                                            email = ""
                                                        )
                                                } else {
                                                    additionalParticipantsData[i] =
                                                        additionalParticipantsData[i].copy(username = "")
                                                }
                                            }
                                        )
                                        Text("Sono registrati a Voyago?") // Ho corretto il testo in italiano
                                    }

                                    // Se il partecipante è registrato
                                    if (participantData.isRegistered) {
                                        // Ora ValidatingInputTextField userà lo stato specifico di questo partecipante
                                        ValidatingInputTextField(
                                            participantData.username,
                                            { newValue ->
                                                additionalParticipantsData[i] =
                                                    participantData.copy(username = newValue)
                                            },
                                            // Dovrai gestire gli errori di validazione all'interno di AdditionalParticipantInput
                                            // Per ora, useremo un placeholder 'false' o un'altra logica di validazione per singolo campo
                                            false, // <<< Devi implementare la logica di validazione per 'username' dentro 'AdditionalParticipantInput' o qui.
                                            "Username"
                                        )
                                    } else {
                                        ValidatingInputTextField(
                                            participantData.name,
                                            { newValue ->
                                                additionalParticipantsData[i] =
                                                    participantData.copy(name = newValue)
                                            },
                                            false, // <<< Devi implementare la logica di validazione per 'name'
                                            "Nome"
                                        )
                                        ValidatingInputTextField(
                                            participantData.lastName, // Ho corretto 'surname' a 'lastName' per coerenza
                                            { newValue ->
                                                additionalParticipantsData[i] =
                                                    participantData.copy(lastName = newValue)
                                            },
                                            false, // <<< Devi implementare la logica di validazione per 'lastName'
                                            "Cognome"
                                        )
                                        ValidatingInputEmailField(
                                            participantData.email,
                                            { newValue ->
                                                additionalParticipantsData[i] =
                                                    participantData.copy(email = newValue)
                                            },
                                            false // <<< Devi implementare la logica di validazione per 'email'
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (dialogPhase == 0) {
                            if (selectedSpots > 1) {
                                dialogPhase = 1
                            } else {
                                vm.askToJoin(nonNullTrip, 1, selectedSpots, emptyList(), emptyList())
                                showDialog = false
                            }
                        } else {
                            // Validazione: controlla tutti i campi di tutti i partecipanti aggiuntivi
                            val allParticipantsValid = additionalParticipantsData.all { participant ->
                                if (participant.isRegistered) {
                                    // Valida solo l'username
                                    participant.username.isNotBlank() && participant.username.any { it.isLetter() }
                                } else {
                                    // Valida nome, cognome, email (almeno uno deve essere compilato per non essere vuoto)
                                    participant.name.isNotBlank() || participant.lastName.isNotBlank() || participant.email.isNotBlank()
                                    // Potresti voler aggiungere una validazione più robusta per l'email qui
                                }
                            }

                            if (allParticipantsValid) {
                                val finalUnregistered = additionalParticipantsData
                                    .filter { !it.isRegistered }
                                    .map { Participant(it.name, it.lastName, it.email) } // Assicurati che Participant sia il tuo modello per non registrati
                                    .filter { it.name.isNotEmpty() || it.surname.isNotEmpty() || it.email.isNotEmpty() } // Aggiunto .surname per coerenza

                                val finalRegisteredUsernames = additionalParticipantsData
                                    .filter { it.isRegistered }
                                    .map { it.username }
                                    .filter { it.isNotEmpty() }

                                vm.askToJoin(
                                    nonNullTrip,
                                    uvm.loggedUser.id,
                                    selectedSpots,
                                    finalUnregistered,
                                    uvm.getIdListFromUsernames(finalRegisteredUsernames)
                                )
                                showDialog = false
                                dialogPhase = 0
                                additionalParticipantsData.clear() // Resetta la lista unificata
                            } else {
                                // Mostra un messaggio di errore all'utente, se la validazione fallisce
                                // Ad esempio, potresti avere uno stato 'showValidationError'
                                // o un Toast per indicare che tutti i campi devono essere compilati.
                                Log.e("Validation", "Errore di validazione: alcuni campi sono vuoti o non validi.")
                            }
                        }

                    }) {
                        Text(if (dialogPhase == 0 && selectedSpots > 1) "Next" else "Confirm")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDialog = false
                        dialogPhase = 0
                        additionalParticipantsData.clear() // Resetta la lista unificata
                    }) {
                        Text("Annulla") // Ho corretto il testo in italiano
                    }
                }
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
@Composable
fun Hero(trip: Trip) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        val context = LocalContext.current
        val drawableId = remember(trip.photo) {
            context.resources.getIdentifier(trip.photo, "drawable", context.packageName)
        }

        //The photo is saved as a uri (the user selected it from the gallery)
        if(trip.photo.isUriString()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(trip.photo.toUri())
                    .crossfade(true)
                    .build(),
                contentDescription = "Selected Trip Photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        //The photo is saved as a drawable (the trip was already in the database and the user hadn't edit its photo)
        else {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(drawableId)
                    .crossfade(true)
                    .build(),
                contentDescription = trip.photo,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
        }

        //Title and Destination of the trip
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(vertical = 30.dp, horizontal = 10.dp)
                .background(
                    color = Color(0xAA444444),
                    shape = MaterialTheme.shapes.small
                )

        ) {
            Text(
                text = trip.destination +
                        "\n" +
                        trip.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            )
        }

        if (trip.status == Trip.TripStatus.COMPLETED) {
            //Banner that indicated that the trip has already happened
            CompletedBanner(Modifier.align(Alignment.TopEnd))
        } else if (!trip.canJoin()) {
            //Banner that shows that nobody can join the trip anymore
            BookedBanner(Modifier.align(Alignment.TopEnd))
        }
    }
}

//Function that create a good format for the dates
fun formatTripDate(calendar: Calendar): String {
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val suffix = getDayOfMonthSuffix(day)
    val dateFormat = SimpleDateFormat("MMMM d'$suffix', yyyy", Locale.ENGLISH)

    return dateFormat.format(calendar.time)
}

fun getDayOfMonthSuffix(day: Int): String {
    return if (day in 11..13) {
        "th"
    } else when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}

@Composable
fun TitleBox(title:String) {
    Box(
        modifier = Modifier
            .height(50.dp)
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp), clip = false)
            .background(
                color = Color(0xFFF4F4F4))
    ) {
        Text(
            text = title,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )
    }
}

@Composable
fun ItineraryText(trip: Trip, modifier: Modifier = Modifier) {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.US) // Same format used in your first view

    val itineraryString = trip.activities
        .toSortedMap(compareBy { it.timeInMillis }) // Sort days chronologically
        .entries
        .joinToString("\n\n") { (day, activities) ->
            val dayIndex = ((day.timeInMillis - trip.startDate.timeInMillis) / (1000 * 60 * 60 * 24)).toInt() + 1
            val dayHeader = "Day $dayIndex:\n"

            val sortedActivities = activities.sortedBy { activity ->
                try {
                    LocalTime.parse(activity.time, formatter)
                } catch (e: Exception) {
                    LocalTime.MIDNIGHT // fallback if parsing fails
                }
            }

            val activityDescriptions = sortedActivities.joinToString("\n") { activity ->
                val groupActivity = if (activity.isGroupActivity) " (group activity)" else ""
                "- ${activity.time} → ${activity.description}$groupActivity"
            }

            dayHeader + activityDescriptions
        }

    Text(
        text = itineraryString,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        modifier = modifier
    )
}

@Composable
fun DeleteButtonWithConfirmation(trip: Trip, navController: NavController, vm: TripViewModel) {
    val showDialog = remember { mutableStateOf(false) }

    //Delete Button
    Button(onClick = {
        showDialog.value = true
    },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xd8, 0x1f, 0x1f, 255)
        )
    ) {
        Text("Delete")
    }

    //PupUp that asks for confirmation of the cancellation of the trip
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            title = {
                Text(text = "Confirm Cancellation")
            },
            text = {
                Text("Are you sure you want to delete this trip?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newOwner = trip.participants.entries.firstOrNull { it.key != trip.creatorId }
                        println("newOwner: $newOwner")
                        if (!trip.published || newOwner == null) {
                            vm.deleteTrip(trip.id)
                            vm.updatePublishedTrip()
                        } else {
                            trip.participants = trip.participants.toMutableMap().apply {
                                remove(trip.creatorId)
                            }
                            trip.creatorId = newOwner.key
                        }
                        navController.popBackStack()
                        showDialog.value = false
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ShowReview(review: Review, vm: TripViewModel, myTrip: Boolean, uvm: UserViewModel, navController: NavController) {
    val reviewer = uvm.getUserData(review.reviewerId)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        //Profile photo of the reviewer
        if (!myTrip && review.isTripReview) {
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.Gray, shape = CircleShape)
            ) {
                ProfilePhoto(reviewer.firstname, reviewer.surname, true, null)
            }
            Text(
                "${reviewer.firstname} ${reviewer.surname}",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable {

                        navController.navigate("user_profile/${review.reviewerId}")
                    }
            )
        } else if (myTrip && !review.isTripReview) {
            val userReviewed = uvm.getUserData(review.reviewedUserId)
            Box(
                contentAlignment = Alignment.CenterStart,
                modifier = Modifier
                    .size(30.dp)
                    .background(Color.Gray, shape = CircleShape)
            ) {
                ProfilePhoto(userReviewed.firstname, userReviewed.surname, true, null)
            }
            Text(
                "${userReviewed.firstname} ${userReviewed.surname}",
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        //Stars rating
        Row(
            modifier = Modifier
                .weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically)
        {
            PrintStars(review.score)
        }
    }

    //Review title
    Row {
        Text(
            text = review.title,
            modifier = Modifier.padding(start = 50.dp, end = 16.dp),
            fontWeight = FontWeight.Bold)
    }

    //Review content
    Row {
        Text(
            text = review.comment,
            modifier = Modifier.padding(start = 50.dp, end = 16.dp)
        )
    }

    Spacer(Modifier.padding(16.dp))
}

@Composable
fun PrintStars(rating: Int) {
    val full = rating/2
    val half = rating - full*2
    val empty = 5 - (full+half)
    for(i in 1..full) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "filled star",
            tint = Color(0xff, 0xb4, 0x00, 255))
    }
    if (half > 0 ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.StarHalf,
            contentDescription = "half star",
            tint = Color(0xff, 0xb4, 0x00, 255))
    }
    if (empty > 0) {
        for (i in 1..empty) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = "empty star",
                tint = Color(0xff, 0xb4, 0x00, 255))
        }
    }
}

// Inserisci questa data class in fondo al file, o in un file separato per i modelli
data class AdditionalParticipantInput(
    var isRegistered: Boolean = false,
    var username: String = "",
    var name: String = "",
    var lastName: String = "",
    var email: String = ""
)
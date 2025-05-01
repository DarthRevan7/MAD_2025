package com.example.voyago.view

import android.app.DatePickerDialog
import android.net.Uri

import android.util.Log
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.toUri
import com.example.voyago.activities.BottomBar
import com.example.voyago.activities.TopBar
import com.example.voyago.model.Trip
import com.example.voyago.model.TypeTravel
import com.example.voyago.viewmodel.TripListViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EditTravelProposal(navController: NavController, vm: TripListViewModel) {
    val trip = vm.selectedTrip

    if(trip!=null) {

        var imageUri by rememberSaveable {
            mutableStateOf<Uri?>(
                if (trip.photo.isUriString()) {
                    trip.photo.toUri() // Converti la stringa URI in un oggetto Uri se è valida
                } else {
                    null // Se la stringa non è un URI valido (es. nome drawable o vuota), inizia con null
                }
            )
        }


        var tripName by rememberSaveable {mutableStateOf(trip.title)}
        var destination by rememberSaveable {mutableStateOf(trip.destination)}
        var tripNameError by rememberSaveable {mutableStateOf(false)}
        var destinationError by rememberSaveable {mutableStateOf(false)}
        var stringErrorMessage by rememberSaveable {mutableStateOf("")}

        var price by rememberSaveable { mutableStateOf(trip.estimatedPrice.toString()) }
        var priceError by rememberSaveable { mutableStateOf(false) }
        var priceErrorMessage by rememberSaveable { mutableStateOf("") }

        var groupSize by rememberSaveable { mutableStateOf(trip.groupSize.toString()) }
        var groupSizeError by rememberSaveable { mutableStateOf(false) }
        var groupSizeErrorMessage by rememberSaveable { mutableStateOf("") }

        val typeTravel = listOf("party", "adventure", "culture", "relax")
        val selected = rememberSaveable(
            saver = listSaver(
                save = { it.toList() },
                restore = { it.toMutableStateList() }
            )
        ) {
            trip.typeTravel.map { it.toString().lowercase() }.toMutableStateList()
        }

        var startDate by rememberSaveable { mutableStateOf(trip.startDate.toStringDate()) }
        var startCalendar by rememberSaveable { mutableStateOf<Calendar?>(trip.startDate) }

        var endDate by rememberSaveable { mutableStateOf(trip.endDate.toStringDate()) }
        var endCalendar by rememberSaveable { mutableStateOf<Calendar?>(trip.endDate) }

        var dateError by rememberSaveable { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopBar()
            },
            bottomBar = {
                BottomBar(1)
            }
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Color(0xFFF3EDF7))
            ) {
                val listState = rememberLazyListState()

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    item {
                        TripImageEdit(trip,
                            imageUri = imageUri, onUriSelected = { uri ->
                            imageUri = uri // Quando una nuova immagine viene selezionata, aggiorna LO STATO imageUri
                        })
                    }

                    item {
                        Spacer(modifier = Modifier.height(40.dp))
                    }

                    item {
                        TextField(
                            value = tripName,
                            onValueChange = { tripName = it },
                            label = { Text("Trip name") },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            supportingText = {
                                if (tripNameError) {
                                    Text(
                                        text = stringErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        TextField(
                            value = destination,
                            onValueChange = { destination = it },
                            label = { Text("Destination") },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            supportingText = {
                                if (destinationError) {
                                    Text(
                                        text = stringErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {

                        TextField(
                            value = price,
                            onValueChange = {
                                price = it
                                priceError = false
                            },
                            label = { Text("Price estimate") },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            isError = priceError,
                            supportingText = {
                                if (priceError) {
                                    Text(
                                        text = priceErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }

                    item {
                        TextField(
                            value = groupSize,
                            onValueChange = {
                                groupSize = it
                                groupSizeError = false
                            },
                            label = { Text("Group Size") },
                            modifier = Modifier.fillMaxWidth(0.8f),
                            isError = groupSizeError,
                            supportingText = {
                                if (groupSizeError) {
                                    Text(
                                        text = groupSizeErrorMessage,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()

                        ) {
                            Text(
                                text = "Trip type",
                                modifier = Modifier
                                    .align(Alignment.Center),
                                fontSize = 17.sp
                            )


                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 3.dp)
                        ) {


                            Text(
                                text = "Select one or more options",
                                modifier = Modifier
                                    .padding(vertical = 10.dp)
                                    .align(Alignment.Center),
                                fontStyle = FontStyle.Italic
                                //fontSize = 10.sp
                            )
                        }
                    }

                    item {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        ) {
                            typeTravel.forEach { type ->
                                FilterChip(
                                    selected = type in selected,
                                    onClick = {
                                        if (type in selected) {
                                            selected.remove(type)
                                        } else {
                                            selected.add(type)
                                        }
                                    },
                                    label = { Text(type.lowercase()) },
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    item {
                        val context = LocalContext.current
                        val calendar = Calendar.getInstance()
                        val year = calendar.get(Calendar.YEAR)
                        val month = calendar.get(Calendar.MONTH)
                        val day = calendar.get(Calendar.DAY_OF_MONTH)


                        val startDatePickerDialog = remember {
                            DatePickerDialog(
                                context,
                                { _: DatePicker, y: Int, m: Int, d: Int ->
                                    startDate = "$d/${m + 1}/$y"
                                    startCalendar = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                }, year, month, day
                            )
                        }

                        val endDatePickerDialog = remember {
                            DatePickerDialog(
                                context,
                                { _: DatePicker, y: Int, m: Int, d: Int ->
                                    endDate = "$d/${m + 1}/$y"
                                    endCalendar = Calendar.getInstance().apply {
                                        set(y, m, d, 0, 0, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }
                                }, year, month, day
                            )
                        }


                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 35.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.Start,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            ) {
                                OutlinedButton(onClick = { startDatePickerDialog.show() }) {
                                    Text("Start Date")
                                }

                                if (startDate.isNotEmpty()) {
                                    Text(
                                        "Start: $startDate",
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            ) {
                                OutlinedButton(onClick = { endDatePickerDialog.show() }) {
                                    Text("End Date")
                                }

                                if (endDate.isNotEmpty()) {
                                    Text("End: $endDate", modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                        }

                        if (dateError.isNotEmpty()) {
                            Text(
                                text = dateError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(50.dp))
                    }

                    item {

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                        ) {

                            Button(
                                onClick = {
                                    navController.navigate("main_page")
                                },
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(60.dp)
                                    .padding(top = 16.dp)
                            ) {
                                Text("Cancel")
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            Button(
                                onClick = {
                                    if (!validateStringField(tripName)) {
                                        tripNameError = true
                                        stringErrorMessage = "This field cannot be empty"
                                    } else if (!validateStringField(destination)) {
                                        destinationError = true
                                        stringErrorMessage = "This field cannot be empty"
                                    } else if (!validatePrice(price)) {
                                        priceError = true
                                        priceErrorMessage = "Price must be greater than zero"
                                    } else if (!validateGroupSize(groupSize)) {
                                        groupSizeError = true
                                        groupSizeErrorMessage = "Group size must be greater than 1"
                                    } else if (!validateDateOrder(startCalendar, endCalendar)) {
                                        priceError = false
                                        priceErrorMessage = ""
                                        dateError = "End date must be after start date"
                                    } else {
                                        priceError = false
                                        priceErrorMessage = ""
                                        dateError = ""

                                        val creatorId = 1

                                        if (vm.currentTrip == null) {
                                            val activities =
                                                mutableMapOf<Calendar, MutableList<Trip.Activity>>()

                                            val newTrip = Trip(
                                                photo = "",
                                                title = tripName,
                                                destination = destination,
                                                startDate = startCalendar!!,
                                                endDate = endCalendar!!,
                                                estimatedPrice = price.toDouble(),
                                                groupSize = groupSize.toInt(),
                                                activities = activities,
                                                typeTravel = selected.map { TypeTravel.valueOf(it.uppercase()) },
                                                creatorId = creatorId,
                                                published = false,
                                                id = 99,
                                                participants = emptyList(),
                                                status = Trip.TripStatus.NOT_STARTED,
                                                appliedUsers = emptyList(),
                                                reviews = emptyList()
                                            )

                                            vm.addNewTrip(newTrip)

                                        } else {

                                            val currentTrip = vm.currentTrip

                                            if (currentTrip != null) {
                                                val updatedTrip = Trip( // Rinominato per chiarezza
                                                    // >>>>>>>>>>>> MODIFICA QUI: Salva l'URI dallo STATO imageUri <<<<<<<<<<<<<<<
                                                    // Prendi l'URI attualmente selezionato o inizializzato dallo stato.
                                                    // Se è null, salva una stringa vuota.
                                                    photo = imageUri?.toString() ?: "",
                                                    title = tripName,
                                                    destination = destination,
                                                    startDate = startCalendar!!,
                                                    endDate = endCalendar!!,
                                                    estimatedPrice = price.toDouble(),
                                                    groupSize = groupSize.toInt(),
                                                    activities = currentTrip.activities, // Mantieni le attività esistenti
                                                    typeTravel = selected.map { TypeTravel.valueOf(it.uppercase()) },
                                                    creatorId = currentTrip.creatorId, // Mantieni l'ID del creatore originale
                                                    published = currentTrip.published, // Mantieni lo stato di pubblicazione originale
                                                    id = currentTrip.id, // Mantieni l'ID del trip originale
                                                    participants = currentTrip.participants, // Mantieni i partecipanti originali
                                                    status = currentTrip.status, // Mantieni lo stato originale
                                                    appliedUsers = currentTrip.appliedUsers, // Mantieni gli applied users originali
                                                    reviews = currentTrip.reviews // Mantieni le review originali
                                                )

                                                vm.editNewTrip(updatedTrip)
                                            }


                                        }


                                        navController.navigate("activities_list")

                                    }
                                },
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(60.dp)
                                    .padding(top = 16.dp)
                            ) {
                                Text("Next")
                            }
                        }
                    }

                }
            }

        }
    }
}

fun Calendar.toStringDate(): String {
    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return format.format(this.time)
}

/*

AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            LocalContext.current.resources.getIdentifier(
                                trip.photo,
                                "drawable",
                                LocalContext.current.packageName
                            )
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = trip.destination,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )


* */


@Composable
fun TripImageEdit(trip:Trip, imageUri: Uri?, onUriSelected: (Uri?) -> Unit) { // Ha cambiato firma: prende solo l'URI e il callback
    val context = LocalContext.current

    // Questo launcher gestisce il risultato del selettore di media
    val pickMedia = rememberLauncherForActivityResult(
        contract = PickVisualMedia()
    ) { uri ->
        // Quando l'utente seleziona un'immagine, chiama il callback.
        // Questo aggiorna LO STATO imageUri NEL PARENT (EditTravelProposal).
        // NON MODIFICARE DIRETTAMENTE trip.photo QUI! La modifica avviene nel parent.
        onUriSelected(uri)
        if (uri != null) {
            Log.d("PhotoPicker", "Selected URI: $uri")
            // >>>>>>>>>>>> RIMOSSA QUESTA RIGA: NON MODIFICARE trip.photo QUI! <<<<<<<<<<<<<<<
            // trip.photo = uri.toString()
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center // Centra il contenuto
    ) {
        // >>>>>>>>>>>> MODIFICA QUI: Logica di visualizzazione basata SOLO sull'URI ricevuto <<<<<<<<<<<<<<<
        // Mostra l'AsyncImage se c'è un URI valido nello stato, altrimenti mostra il placeholder.
        // Non usiamo più trip.photo.isUriString() qui per decidere cosa mostrare.
        // La decisione avviene a monte, nell'inizializzazione dello stato imageUri nel parent.
        if (imageUri.toString().isUriString()) {
            // Se c'è un URI nello stato (iniziale dall'oggetto trip o selezionato ora), mostralo.
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri) // Usa l'URI dello stato
                    .crossfade(true) // Aggiunge animazione
                    .build(),
                contentDescription = "Selected Trip Photo", // Descrizione per accessibilità
                contentScale = ContentScale.Crop, // Scala per coprire
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Se l'URI è null (il trip originale non aveva un URI valido salvato, o è stato "rimosso"?),
            // mostra il placeholder generico con l'icona.
            // La logica per caricare un drawable specifico basato su trip.photo è stata rimossa
            // per coerenza e semplificazione nella gestione via stato URI/placeholder.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(
                            LocalContext.current.resources.getIdentifier(
                                trip.photo,
                                "drawable",
                                LocalContext.current.packageName
                            )
                        )
                        .crossfade(true)
                        .build(),
                    contentDescription = trip.destination,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
        }

        // L'IconButton per selezionare la foto rimane sempre visibile in basso a destra.
        IconButton(
            onClick = {
                // Avvia il selettore quando l'icona viene cliccata
                pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
            },
            modifier = Modifier
                .align(Alignment.BottomEnd) // Posiziona nel Box
                .padding(16.dp) // Margine
                // Sfondo per migliorare la visibilità dell'icona
                .background(
                    color = Color.Black.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.AddPhotoAlternate,
                contentDescription = "Select photo from gallery", // Descrizione per accessibilità
                tint = Color.White, // Colore icona
                modifier = Modifier.padding(4.dp) // Padding interno
            )
        }
    }
}
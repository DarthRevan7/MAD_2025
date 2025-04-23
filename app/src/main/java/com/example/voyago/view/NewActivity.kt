package com.example.voyago.view


import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.voyago.activities.*
import java.util.Calendar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivity(navController: NavController) {


    var activityTitle by rememberSaveable { mutableStateOf("") }
    var activityDescription by rememberSaveable { mutableStateOf("") }

    var activityDate by rememberSaveable { mutableStateOf("") }

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
                    .fillMaxSize(.8f)
                    .fillMaxHeight(0.9f)
                    .align(Alignment.Center)
                    .background(
                        color = Color.Blue,
                        shape = RoundedCornerShape(24.dp)
                    ),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                item{
                    TextField(
                        value = activityTitle,
                        onValueChange = { activityTitle = it },
                        label = { Text("Activity title") },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(vertical = 20.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(50.dp))
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
                                activityDate = "$d/${m + 1}/$y"
                            }, year, month, day
                        )
                    }


                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    ) {
                        OutlinedButton(onClick = { startDatePickerDialog.show() }) {
                            if (activityDate.isNotEmpty()){
                                Text("Date: $activityDate")
                            } else {
                                Text("Select date")
                            }

                        }

//                        if (activityDate.isNotEmpty()) {
//                            Text("Start: $activityDate", modifier = Modifier.padding(top = 8.dp))
//                        }
                    }
                }

                item{
                    TextField(
                        value = activityDescription,
                        onValueChange = { activityDescription = it },
                        label = { Text("Activity description") },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
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
                                navController.navigate("create_new_travel_proposal")
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

                                navController.navigate("main_page")


                            },
                            modifier = Modifier
                                .width(160.dp)
                                .height(60.dp)
                                .padding(top = 16.dp)
                        ) {
                            Text("Add")
                        }
                    }

                }

            }
        }
    }
}








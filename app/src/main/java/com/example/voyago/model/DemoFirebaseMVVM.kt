package com.example.demofirebase

import android.util.Log
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


/***** FIREBASE ROUTING *****/
object Collections{
    private const val C_USERS = "users"

    private val db: FirebaseFirestore
        get() = Firebase.firestore

    init {
        db.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true) //false to Disable LocalChaching
            .build()
    }



    val users = db.collection(C_USERS)

    //val dummyUser = User(first = "John", second = "Doe", born = 1992)
}


/*** MODEL ****/
data class User(

    val id: Int=0, // Added an ID field
    val first: String = "",
    val second: String = "",
    val born: Long = 0L
)

class TheUsersModel(){

    fun getUsers(): Flow<List<User>> = callbackFlow {//Observes update from the Server
        val listener = Collections.users.
        orderBy("born")
            .addSnapshotListener { s, er ->
            if(s!=null)
                trySend(s.toObjects(User::class.java))
            else {
                Log.e("Error", er.toString())
                trySend(emptyList())
            }
        }
        awaitClose {
            listener.remove()
        }
    }
}

/***** ViewModel ****/

class UserViewModel(private val model: TheUsersModel): ViewModel() {
    val users: Flow<List<User>> =  model.getUsers()
}

object Factory: ViewModelProvider.Factory{
    private val model = TheUsersModel()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(UserViewModel::class.java) -> UserViewModel(model) as T
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}


/**** View ****/

//Method to call in the MainActivity
@Composable
fun UsersScreen(vm: UserViewModel = viewModel(factory = Factory), modifier: Modifier){
    val users by vm.users.collectAsState(initial = listOf())


    LazyColumn(modifier = modifier.then(Modifier.fillMaxSize())){
        items(users){
            UserCard(it)
            Spacer(Modifier.height(12.dp))
        }
     }
}

@Composable
fun  UserCard(u: User){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(color = MaterialTheme.colorScheme.surface)
    ){
        Text(u.first, modifier = Modifier.weight(1f), style = MaterialTheme.typography.displayMedium)
        Text(u.second, modifier = Modifier.weight(1f), style = MaterialTheme.typography.displayMedium)
        Text(u.born.toString(), modifier = Modifier.weight(1f), style = MaterialTheme.typography.displayMedium)
    }
}


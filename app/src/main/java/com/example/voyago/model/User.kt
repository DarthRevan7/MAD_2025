package com.example.voyago.model

import android.net.Uri

data class User(
    var name:String,
    var surname:String,
    var username:String,
    var trips:List<Int>
)
{
    fun getTrip(id: Int) {
        
    }
}

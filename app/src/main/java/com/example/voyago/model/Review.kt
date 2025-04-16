package com.example.voyago.model

import android.net.Uri

data class Review(
    val id:Int,
    val reviewer:LazyUser,
    val title:String,
    val text:String,
    val rating:Int,  //Integer from 1 to 10 -> 1 star = 2 points
    var pictures:List<Uri>
)
{

}

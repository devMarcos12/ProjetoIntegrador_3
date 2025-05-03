package com.example.projetointegrador
import android.graphics.Bitmap

data class Event(
    val docId : String,
    val title: String,
    val description: String,
    val imageResId: Int,
    val imageBitmap: Bitmap?
)

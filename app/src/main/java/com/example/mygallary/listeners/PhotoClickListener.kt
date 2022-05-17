package com.example.mygallary.listeners

import android.view.View
import com.example.mygallary.MediaImageFile

interface PhotoClickListener {
    fun onClick(selection_mode_active:Boolean=false,view: View, pos:Int)
    fun onLongClick(selection_mode_active:Boolean=false,view: View,pos:Int)
    fun onNothingSelected()
}
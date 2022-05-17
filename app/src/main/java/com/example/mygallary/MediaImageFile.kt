package com.example.mygallary

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import java.util.*

data class MediaImageFile(
    val id:Long,
    val displayName:String,
    val dateAdded: Date,
    val contentUri:Uri,
    var isSelected:Boolean=false,
    var LongClicked:Boolean=false,
    var adapter_position:Int=0,
    var layout_position:Int=0

){

    companion object{

        val diff_call_back=object:DiffUtil.ItemCallback<MediaImageFile>(){
            override fun areItemsTheSame(
                oldItem: MediaImageFile,
                newItem: MediaImageFile
            ): Boolean {
                return oldItem.id==newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MediaImageFile,
                newItem: MediaImageFile
            ): Boolean {
                return  oldItem==newItem
            }

        }
        var selection_mode_activated=false

    }


}

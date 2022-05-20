package com.example.mygallary

class MyComparator() :Comparator<MediaImageFile>{
    var is_desc:Boolean=true

    fun setOrder(is_d:Boolean){
        this.is_desc=is_d
    }
    override fun compare(p0: MediaImageFile?, p1: MediaImageFile?): Int{
        if(this.is_desc){
            return p0!!.dateAdded.day-p1!!.dateAdded.day
        }else{

            return p1!!.dateAdded.day-p0!!.dateAdded.day

        }
    }
}
package com.example.mygallary

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.mygallary.databinding.ActivityViewPhotoBinding

class ViewPhoto : AppCompatActivity() {

    lateinit var viewPhotoBinding: ActivityViewPhotoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewPhotoBinding=DataBindingUtil.setContentView(this,R.layout.activity_view_photo)
        var link:String=intent.getStringExtra(Util.PHOTO_URI)!!
        var uri:Uri= Uri.parse(link)
        viewPhotoBinding.myphoto.setImageURI(uri)
    }

    override fun onDestroy() {
        super.onDestroy()
        supportFinishAfterTransition()
    }

}
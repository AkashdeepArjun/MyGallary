package com.example.mygallary

import android.annotation.SuppressLint
import android.app.Application
import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.IntentSender
import android.database.ContentObserver
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivityViewModel(application: Application):AndroidViewModel(application) {

    private  val _delete_status=MutableLiveData<Boolean>()
    val delete_status:LiveData<Boolean> = _delete_status

    private  val _selected_items=MutableLiveData<MutableList<MediaImageFile>>()
    val selected_items:LiveData<MutableList<MediaImageFile>> =_selected_items

    private  val _images=MutableLiveData<List<MediaImageFile>>()
    val images:LiveData<List<MediaImageFile>> = _images

    private  var content_observer:ContentObserver?=null

    private var pendingDeleteImage:MediaImageFile?=null

    private val _permissionNeededForDeletion=MutableLiveData<IntentSender>()

    val permissionNeededForDeletion:LiveData<IntentSender> = _permissionNeededForDeletion



    fun loadImages(){
    viewModelScope.launch {
        val image_list=queryImages()
        _images.postValue(image_list)
        if(content_observer==null){

            content_observer=getApplication<Application>().contentResolver.registerObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI){
                loadImages()
            }
        }


    }}


    private  suspend fun queryImages():List<MediaImageFile>{
        val images= mutableListOf<MediaImageFile>()

        withContext(Dispatchers.IO){
            val projection= arrayOf(MediaStore.Images.Media._ID,MediaStore.Images.Media.DISPLAY_NAME,MediaStore.Images.Media.DATE_ADDED)

            val selection="${MediaStore.Images.Media.DATE_ADDED} >=?"

            val selection_params=arrayOf(dateToTimeStamp(22,10,2019).toString())

            val sortOrder="${MediaStore.Images.Media.DATE_ADDED} DESC"

            getApplication<Application>().contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,projection,selection,selection_params,sortOrder)?.use {cursor->

                val id_column=cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateCreatedColumn=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val displayNameColumn=cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                Log.i("VIEWMODEL","Found ${cursor.count} images")

                while(cursor.moveToNext()){
                    val id=cursor.getLong(id_column)
                    val date_created= Date(TimeUnit.SECONDS.toMillis(cursor.getLong(dateCreatedColumn)))
                    val displayName=cursor.getString(displayNameColumn)
                    val content_uri=ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id)
                    val image=MediaImageFile(id,displayName,date_created,content_uri)
                    images+=image
//                    Log.v("VIEWMODEL","added image $image")
                }
            }

        }
        Log.v("VIEWMODEL","found ${images.size} images")
        return  images
    }


    @Suppress("SameParameterValue")
    @SuppressLint("SimpleDateFormat")
    private  fun dateToTimeStamp(day:Int,month:Int,year:Int):Long{

        return java.text.SimpleDateFormat("dd.MM.yyyy").let {
            formatter->TimeUnit.MICROSECONDS.toSeconds(formatter.parse("$day.$month.$year")?.time?:0)
        }
    }

    override fun onCleared() {

        content_observer?.let {
            getApplication<Application>().contentResolver.unregisterContentObserver(it)
        }
    //        getApplication<>()
    }

    private  fun ContentResolver.registerObserver(uri:Uri,observer:(selfChange:Boolean)->Unit):ContentObserver{

    val content_observer=object :ContentObserver(Handler()){
        override fun onChange(selfChange: Boolean) {
            super.onChange(selfChange)
            observer(selfChange)
        }
    }
        registerContentObserver(uri,true,content_observer)


        return  content_observer


    }

    private suspend fun performDeleteImage(image:MediaImageFile){
    withContext(Dispatchers.IO){
        try {

            getApplication<Application>().contentResolver.delete(image.contentUri,"${MediaStore.Images.Media._ID}=?",
                arrayOf(image.id.toString()))
        }catch (exception:SecurityException){
            if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.Q){
                val recoverableSecurityException=exception as? RecoverableSecurityException?:throw  exception

                pendingDeleteImage=image
                _permissionNeededForDeletion.postValue(recoverableSecurityException.userAction.actionIntent.intentSender)
            }else{
                throw exception
            }
        }
    }
    }

    fun deleteImage(image:MediaImageFile){
        viewModelScope.launch {
            performDeleteImage(image)
        }
    }
//      fun deletePhotos(photos:MutableList<MediaImageFile>)= viewModelScope.launch{
//          _delete_status.postValue(false)
//        for(image:MediaImageFile in photos){
//            viewModelScope.runCatching {
//                performDeleteImage(image)
//            }.onSuccess { _delete_status.postValue(true) }
//        }
//
//    }

    fun setSelectedData(list:MutableList<MediaImageFile>){
        this._selected_items.value=list
    }

    fun deleteSelectedPhotos()=viewModelScope.launch{
        _delete_status.postValue(false)

            runCatching {
                for(image:MediaImageFile in _selected_items.value!!){

                    performDeleteImage(image)
                }
            }.onSuccess {
                _delete_status.postValue(true)

            }

//            if(_selected_items.value!!.size==0){
//        }

    }

//
//    private  fun getIds(list:List<MediaImageFile>):Array<String>{
//        var lists_id=Array<String>(list.size,{""})
//        for(l:MediaImageFile in list){
//            lists_id.fill(l.id.toString())
//        }
//        return  lists_id
//    }

    fun deletePendingImage(){
        pendingDeleteImage?.let {image->
            pendingDeleteImage=null
            deleteImage(image)
        }
    }




}
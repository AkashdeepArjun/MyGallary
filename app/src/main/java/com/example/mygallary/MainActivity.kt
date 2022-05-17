package com.example.mygallary

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.appcompat.view.ActionMode
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import com.example.mygallary.adapters.GallaryAdapter
import com.example.mygallary.databinding.ActivityMainBinding
import com.example.mygallary.listeners.PhotoClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private  const val READ_EXTERNAL_STORAGR_REQ=0x1045
private const val WRITE_DELETE_EXTERNAL_STORAGE_REQ=0x1033

class MainActivity : AppCompatActivity() ,PhotoClickListener{

    companion object{

        var app_resumed=false
//        var activate_action_mode=false
//        var selected_item_views:MutableList<View> = mutableListOf<View>()
    }

    lateinit var vmf:ViewModelFactory
    lateinit var vm:MainActivityViewModel
    lateinit var binding: ActivityMainBinding
    lateinit var gallaryAdapter:GallaryAdapter
    lateinit var viewPhotoIntent:Intent
    lateinit var action_mode_callbacks:ActionMode.Callback


    var action_mode:ActionMode?=null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=DataBindingUtil.setContentView(this,R.layout.activity_main)
        vmf= ViewModelFactory(application)
        vm=ViewModelProviders.of(this,vmf).get(MainActivityViewModel::class.java)
        initGallaryAdapter()
        setUpRv()
        setUpViewModelObservers()
        setUpRuntimeWritePermissionsViewModel()
        initIntent()
        setUpListeners()
        initActionMode()
        checkPermissionsAtStartUp()
    }

    fun checkPermissionsAtStartUp(){
        if(!havePermissions()){
            binding.permissionUi.visibility=View.VISIBLE
            binding.welcomeUi.visibility=View.GONE

        }else{
            openImagesFromPhone()
        }
    }


    fun initGallaryAdapter(){
        gallaryAdapter= GallaryAdapter(this)
    }

    fun setUpRv(){

        binding.gallary.also {
            view->
            view.layoutManager=GridLayoutManager(this,3)
            view.adapter=gallaryAdapter
        }


    }

    fun setUpViewModelObservers(){
        binding.pbStatus.visibility=View.VISIBLE
        vm.images.observe(this, Observer<List<MediaImageFile>> {
            images->
            if(images.isNotEmpty()){
                binding.pbStatus.visibility=View.GONE
                gallaryAdapter.submitList(images)
            }


        })
    }


    fun setUpRuntimeWritePermissionsViewModel(){
        vm.permissionNeededForDeletion.observe(this,Observer<IntentSender>{intentSender->
            startIntentSenderForResult(intentSender, WRITE_DELETE_EXTERNAL_STORAGE_REQ,null,0,0,0,null)

        })
    }

    fun setUpListeners(){

        binding.openAlbum.setOnClickListener {
                openImagesFromPhone()
        }

        binding.grantPermissionButtn.setOnClickListener {
            requestPermission()
        }

    }

    fun showNoAccessUi(){
        binding.welcomeUi.visibility=View.GONE
        binding.permissionUi.visibility=View.VISIBLE
    }

    private fun openImagesFromPhone(){

        if(havePermissions()){
            binding.welcomeUi.visibility= View.GONE
            binding.permissionUi.visibility=View.GONE
            vm.loadImages()
        }else{
            requestPermission()
        }


    }


    private fun havePermissions():Boolean{
       return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)==PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            READ_EXTERNAL_STORAGR_REQ->{
                if(grantResults.isNotEmpty() && grantResults[0]==PERMISSION_GRANTED){
                    openImagesFromPhone()
                }else{
                    val shouldAskUserForRequest=ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if(shouldAskUserForRequest){
                        showNoAccessUi()
                    }else{

                       goToSettings()

                    }
                }
            }

        }
        return
    }


    fun goToSettings(){

        val intent:Intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${packageName}")).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        }
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==Activity.RESULT_OK && resultCode== WRITE_DELETE_EXTERNAL_STORAGE_REQ)
        {
            vm.deletePendingImage()
        }
    }



    private  fun requestPermission(){
        if(!havePermissions()){
            val permissions= arrayOf(READ_EXTERNAL_STORAGE,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(this,permissions, READ_EXTERNAL_STORAGR_REQ)
        }
    }

    private fun deletePhoto(image:MediaImageFile){

            vm.deleteImage(image)
    }
//    fun showDialog(title:String, message:String, positve_button_message:String, negative_button_message:String){
//
//        MaterialAlertDialogBuilder(this)
//            .setTitle(title)
//            .setMessage(message)
//            .setPositiveButton(positve_button_message){_:DialogInterface,_:Int->
//               deleteSelectedItems(selected_items)
//            }
//            .setNegativeButton("cancel"){dialog:DialogInterface,_:Int->
//              dialog.dismiss()
//            }
//            .show()
//
//    }


    fun initIntent(){
    viewPhotoIntent=Intent(this,ViewPhoto::class.java)
    }

    @SuppressLint("RestrictedApi")
    override fun onClick(selection_mode_active:Boolean, view:View, pos:Int) {
            var item:MediaImageFile=vm.images.value!!.get(pos)
        if(action_mode!=null && GallaryAdapter.multi_selection_mode_activated){

                if(gallaryAdapter.selected_items.size>0){
                    action_mode!!.title="selected ${gallaryAdapter.selected_items.size} items"

                }else{
                    action_mode!!.finish()
                }


        }else{


            Toast.makeText(baseContext,"item have adapter position ${item.adapter_position} and layout position ${item.layout_position}",Toast.LENGTH_LONG).show()
            var uri:Uri=item.contentUri
            val options=ActivityOptionsCompat.makeSceneTransitionAnimation(this,view,"photo")
            viewPhotoIntent.putExtra(Util.PHOTO_URI,uri.toString())
            startActivity(viewPhotoIntent,options.toBundle())
//            if(!item.LongClicked){
//                item.isSelected=!item.isSelected
//            }
/*

                if(item.isSelected){
                    if(!selected_items.contains(item)){
                        selected_items.add(item)

                    }
                } else{
                if(selected_items.contains(item) ){
                    selected_items.remove(item)


                    if(selected_items.size==0 ){
//                        MediaImageFile.selection_mode_activated=false
                        item.isSelected=false
                        item.LongClicked=false
                        view.isLongClickable=true
                        if(action_mode!=null){
                            action_mode!!.finish()
                        }

                    }

                }

*/





            }



}
//        deletePhoto(item)


    override fun onLongClick(hega_active: Boolean, view: View, pos: Int) {
        var item:MediaImageFile = vm.images.value!!.get(pos)

            if(hega_active){
               action_mode= startSupportActionMode(action_mode_callbacks)
//                action_bar_destroyed=false
                if(action_mode!=null){

                    action_mode!!.title="selected ${gallaryAdapter.selected_items.size} items"

                }

            }else{
                onNothingSelected()
            }



        }



    override fun onBackPressed() {
        onNothingSelected()
        super.onBackPressed()
    }

    fun initActionMode(){

       action_mode_callbacks =object:ActionMode.Callback{
            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                menuInflater.inflate(R.menu.action_mode_menu,menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                 when(item?.itemId){
                    R.id.btn_delete->{
//                        var shit_done =false
                        MaterialAlertDialogBuilder(this@MainActivity)
                            .setTitle("Delete file?")
                            .setMessage("you suely want to delete this shit")
                            .setPositiveButton("delete"){_:DialogInterface,_:Int->
                                binding.pbStatus.visibility=View.VISIBLE
                                    Toast.makeText(baseContext,"deleting ${gallaryAdapter.selected_items.size} items",Toast.LENGTH_LONG).show()
                                    vm.setSelectedData(gallaryAdapter.selected_items)
                                    vm.deleteSelectedPhotos()
                                    vm.delete_status.observe(this@MainActivity , Observer { status->
                                        if(status){
                                            binding.pbStatus.visibility=View.GONE
//                                            activate_action_mode=false
//                                            resetDefaults()
//                                            mode!!.finish()
//                                            vm.loadImages()

                                               onNothingSelected()


                                        }
                                    })
                                Toast.makeText(baseContext,"tadaaaa",Toast.LENGTH_LONG).show()
//                                vm.delete_status.removeObservers(this@MainActivity)

                                }



                            .setNegativeButton("cancel"){dialog:DialogInterface,_:Int->
                                dialog.dismiss()
                            }
                            .show()





                    }

                    else->{}
                }
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode?) {
//                vm.delete_status.removeObservers {
//                    this@MainActivity.lifecycle
//                }


//                    onNothingSelected()
//                action_bar_destroyed=true
                GallaryAdapter.multi_selection_mode_activated=false
                gallaryAdapter.resetItems()
                gallaryAdapter.resetViews()
                gallaryAdapter.emptySelectedItems()
                gallaryAdapter.emptySelectedViews()
//                gallaryAdapter.multi_selection_mode_activated=false




            }
        }
    }




//    fun resetDefaults(){
//
//
//        onNothingSelected()
//
//
//
//    }

    override fun onNothingSelected() {

        if(action_mode!=null){
            action_mode!!.finish()
        }

        action_mode=null
        var is_action_mode_visible = if(action_mode==null) true else false
        Toast.makeText(this,"action null truth is  ${is_action_mode_visible}",Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        Toast.makeText(baseContext,"app resumed",Toast.LENGTH_LONG).show()
    }

}
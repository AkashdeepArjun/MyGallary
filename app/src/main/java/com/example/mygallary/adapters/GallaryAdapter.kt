package com.example.mygallary.adapters

import android.icu.text.SimpleDateFormat
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.mygallary.MediaImageFile
import com.example.mygallary.MyComparator
import com.example.mygallary.R
import com.example.mygallary.Util
import com.example.mygallary.adapters.GallaryAdapter.*
import com.example.mygallary.listeners.PhotoClickListener
import java.util.*
import javax.inject.Inject

class GallaryAdapter (val glide:RequestManager,val listener:PhotoClickListener):androidx.recyclerview.widget.RecyclerView.Adapter<PhotoViewHolder>() {

    private  lateinit var myComparator: MyComparator

    val differ=AsyncListDiffer<MediaImageFile>(this,MediaImageFile.diff_call_back)

    var data:MutableList<MediaImageFile>
        set(value) = differ.submitList(value)
        get() = differ.currentList


    override fun getItemCount(): Int {
        return data.size
    }

    companion object{
        var multi_selection_mode_activated=false
        val pattern:String ="yyyy-MM-dd"
        val format_date=java.text.SimpleDateFormat(pattern)
    }



    var selected_items:MutableList<MediaImageFile> = mutableListOf<MediaImageFile>()
    var selected_item_views:MutableList<View> = mutableListOf<View>()

    inner class PhotoViewHolder(v:View,listener: PhotoClickListener):RecyclerView.ViewHolder(v){
        val root_view=v
        val image_view:ImageView=v.findViewById(R.id.image)
//        val date_title_view:TextView=v.findViewById(R.id.date_created_tv)

        init {
            myComparator= MyComparator()
            image_view.setOnClickListener {iv->
                val image=root_view.tag as? MediaImageFile?:return@setOnClickListener
                image.isSelected=!image.isSelected

                image.layout_position=layoutPosition
                image.adapter_position=adapterPosition

                if(multi_selection_mode_activated){
                    iv.isLongClickable=false
                    if(image.isSelected){
                        iv.alpha=0.5f
                        if(!selected_items.contains(image)){
                            selected_items.add(image)
                            selected_item_views.add(iv)
                        }
                    }else{

                        image.LongClicked=false
                        iv.isLongClickable=true
                        iv.alpha=1.0f
                        if(selected_items.contains(image)){
                            selected_items.remove(image)

                        }



                    }

//                    listener.onClick(multi_selection_mode_activated,iv,adapterPosition)

                }
                listener.onClick(multi_selection_mode_activated,iv,adapterPosition)



            }
            image_view.setOnLongClickListener {iv->
                val image=root_view.tag as MediaImageFile
                image.LongClicked=!image.LongClicked

                multi_selection_mode_activated = image.LongClicked
//                image.isSelected=!image.isSelected
                if(multi_selection_mode_activated){
                        image.isSelected=true
                        iv.alpha=0.5f
                        if(!selected_items.contains(image)){
                            selected_items.add(image)
                            selected_item_views.add(iv)
                        }




                }else{
                    image.isSelected=false
                    iv.alpha=1.0f
                    if(!image.LongClicked){
                        listener.onNothingSelected()
                    }

                }

                listener.onLongClick(multi_selection_mode_activated,iv,adapterPosition)
//
                true
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
            val layout_inflator=LayoutInflater.from(parent.context)
            val view=layout_inflator.inflate(R.layout.gallary_item_layout,parent,false)

        return PhotoViewHolder(view,listener)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {

        val item=data.get(position)
        holder.root_view.tag=item

//        Glide.with(holder.image_view)
//            .applyDefaultRequestOptions(RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
            glide
            .load(item.contentUri)
            .thumbnail(0.33f)
            .centerCrop()
            .into(holder.image_view)
//            holder.date_title_view.text= format_date.format(item.dateAdded)

    }


    fun emptySelectedItems(){
        if(selected_items.isNotEmpty()){
            selected_items.removeAll {
                true
            }
        }
    }


    fun setMultiSelectionStatus(shouldTurnOn:Boolean){
        multi_selection_mode_activated=shouldTurnOn
    }

    fun resetItems(){
        for(item:MediaImageFile in selected_items){
            item.isSelected=false
            item.LongClicked=false

        }
    }

    fun resetViews(){
        for (v:View in selected_item_views){
            v.alpha=1.0f
            v.isLongClickable=true

        }
    }





    fun emptySelectedViews(){
        if(!selected_item_views.isEmpty())
        {
                selected_item_views.removeAll {
                true}
        }
    }


    fun emptyCurrentList(){
       this.runCatching {  }
    }


    fun sortImages(desc:Boolean){

        myComparator.setOrder(desc)

            Collections.sort(data,myComparator)
            differ.submitList(data)




    }

//    fun sortImages(should_sort_by_desc:Boolean){
//        val list:List<MediaImageFile> = currentList
//        if(!should_sort_by_desc){
//            Collections.sort(list,MyComparator())
//            submitList(list)
//        }else{
//
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                Collections.sort(list,MyComparator().reversed())
//                submitList(list)
//            }
//        }
//
//        notifyDataSetChanged()
//    }

}
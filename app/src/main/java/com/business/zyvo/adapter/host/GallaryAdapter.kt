package com.business.zyvo.adapter.host

import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.R
import com.google.android.material.imageview.ShapeableImageView


class GallaryAdapter(private var items: MutableList<Uri>,var context : Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var showImage :Int =0

    var uploadImage :Int =1

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int,type:String)
    }

    fun setOnItemClickListener(listener: GallaryAdapter.onItemClickListener) {
        mListener = listener
    }


    override fun getItemViewType(position: Int): Int {
        return if(position == items.size-1) uploadImage else showImage
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            showImage -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_gallary_show_image, parent, false)
                RegularViewHolder(view,context)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_gallery_add_image, parent, false)
                HeaderViewHolder(view,context)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is HeaderViewHolder -> holder.bind(mListener)
            is RegularViewHolder -> holder.bind(item,mListener)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    // ViewHolder for header
    class HeaderViewHolder(itemView: View,context:Context) : RecyclerView.ViewHolder(itemView) {
        private val rlMain: RelativeLayout = itemView.findViewById(R.id.main)
        fun bind(mListener: onItemClickListener) {
            rlMain.setOnClickListener {
              mListener.onItemClick(adapterPosition-1, AppConstant.ADD_IMAGE)
            }
        }
    }

    class RegularViewHolder(itemView: View,var context: Context) : RecyclerView.ViewHolder(itemView) {
        private val deleteImage: ImageView = itemView.findViewById(R.id.delete_image)
        private val shpImg:ShapeableImageView = itemView.findViewById(R.id.shapeableImageView)
        fun bind(item: Uri, mListener: onItemClickListener) {
            deleteImage.setOnClickListener {
                 mListener.onItemClick(adapterPosition,AppConstant.DELETE)
            }

            Glide.with(context)
                .load(item)               // Your image URL or resource
                .error(R.drawable.ic_error)  // Placeholder if the image fails to load
                .into(shpImg)
        }
    }

    public fun updateAdapter(items: List<Uri>){
        this.items = items.toMutableList()
        val dummyUri = Uri.parse("https://example.com/dummy_image.jpg")
        this.items.add(dummyUri)
        Log.d("TESTING","ITEMS IN A LIST ARE "+items.size)
        notifyDataSetChanged()
    }
}
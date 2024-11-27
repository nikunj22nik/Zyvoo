package com.yesitlab.zyvo.adapter.host

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.yesitlab.zyvo.AppConstant
import com.yesitlab.zyvo.R

class GallaryAdapter(private var items: List<Uri>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


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
                RegularViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.adapter_gallery_add_image, parent, false)
                HeaderViewHolder(view)
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
    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val rlMain: RelativeLayout = itemView.findViewById(R.id.main)

        fun bind(mListener: onItemClickListener) {
            rlMain.setOnClickListener {
              mListener.onItemClick(adapterPosition, AppConstant.ADD_IMAGE)
            }
        }
    }

    // ViewHolder for regular items
    class RegularViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deleteImage: ImageView = itemView.findViewById(R.id.delete_image)

        private val shpImg:ShapeableImageView = itemView.findViewById(R.id.shapeableImageView)
        fun bind(item: Uri, mListener: onItemClickListener) {
            deleteImage.setOnClickListener {
                 mListener.onItemClick(adapterPosition,AppConstant.DELETE)
            }
            shpImg.setImageURI(item)
        }
    }

    public fun updateAdapter(items: List<Uri>){
        this.items = items
        notifyDataSetChanged()
    }


}
package com.business.zyvo.adapter.guest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.R
import com.business.zyvo.model.ActivityModel

class ActivitiesAdapter(var context: Context, var list: MutableList<ActivityModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onItemClickListener: ((list: String/*MutableList<ActivityModel>*/, Int,Boolean) -> Unit)? = null


    fun setOnItemClickListener(listener: (list: String/*MutableList<ActivityModel>*/, Int,Boolean) -> Unit) {
        onItemClickListener = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_activities_photo, parent, false)
                TextViewHolder(view)
            }

            else -> {
                val view =
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.adapter_activities_text_view, parent, false)
                ImageViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextViewHolder -> {
                holder.bind(list[position].name, list[position].image)
                var laypout = holder.itemView.findViewById<LinearLayout>(R.id.layout)

                if(list[position].checked){
                    laypout.setBackgroundResource(R.drawable.bg_four_side_selected_blue)
                }else{
                    laypout.setBackgroundResource(R.drawable.bg_four_side_grey_corner)
                }
                laypout.setOnClickListener {
                    if (!list[position].checked) {
                        laypout.setBackgroundResource(R.drawable.bg_four_side_selected_blue)
                       var pair = list[position]
                        pair.checked = true
                        list[position] = pair
                        //onItemClickListener?.invoke(list,position,true)
                        onItemClickListener?.invoke(pair.name,position,true)
                    }
                    else{
                      laypout.setBackgroundResource(R.drawable.bg_four_side_grey_corner)
                        var pair = list[position]
                        pair.checked = false
                        list[position] = pair
                        //onItemClickListener?.invoke(list,position,false)
                        onItemClickListener?.invoke(pair.name,position,false)
                    }
                }
            }

            is ImageViewHolder -> holder.bind(list[position])
        }
    }


    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.tv_name)
        private val layout: LinearLayout = itemView.findViewById(R.id.layout)
        private val img: ImageView = itemView.findViewById(R.id.img)
        fun bind(text: String, imgVal: Int) {
            textView.text = text
            img.setImageResource(imgVal)

        }


    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //        private val imageView: ImageView = itemView.findViewById(R.id.)
//
        fun bind(activityModel: ActivityModel) {

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (list[position].name.equals("Other Activities")) 1 else 0
    }


    fun updateAdapter(list: MutableList<ActivityModel>){
        this.list = list
        notifyDataSetChanged()
    }


}
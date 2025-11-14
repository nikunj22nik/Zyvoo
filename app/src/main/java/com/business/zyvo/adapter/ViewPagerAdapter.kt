package com.business.zyvo.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.OnLogClickListener
import com.business.zyvo.R
import com.business.zyvo.adapter.host.MyPlacesHostAdapter
import com.business.zyvo.databinding.LayoutImageBinding
import com.business.zyvo.model.HostMyPlacesModel
import com.business.zyvo.model.ViewpagerModel

class ViewPagerAdapter(
    private var list: MutableList<String>,
    var context: Context,
    var listner: OnLogClickListener?
) : RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick()
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    inner class ViewHolder(val binding: LayoutImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        Log.d("TESTING_ZYVOO", BuildConfig.MEDIA_URL + currentItem)
        Glide.with(context)
            .load(BuildConfig.MEDIA_URL + currentItem)
            .error(R.drawable.ic_circular_img_user)
            .into(holder.binding.image)
        holder.itemView.setOnClickListener {
            mListener.onItemClick()
        }

    }

    override fun getItemCount() = list?.size ?: 0

    fun updateItem(newList: MutableList<String>) {
        this.list = newList
        notifyDataSetChanged()
    }
}

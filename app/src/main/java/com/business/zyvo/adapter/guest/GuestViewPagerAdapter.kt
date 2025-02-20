package com.business.zyvo.adapter.guest

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.databinding.LayoutImageBinding
import com.business.zyvo.fragment.guest.home.model.OnViewPagerImageClickListener

class GuestViewPagerAdapter(private var list: MutableList<String>, var context: Context,
                            var listner: OnViewPagerImageClickListener?
) : RecyclerView.Adapter<GuestViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutImageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]

        Log.d("TESTING_ZYVOO",AppConstant.BASE_URL + currentItem)
        Glide.with(context).load(AppConstant.BASE_URL + currentItem).into(holder.binding.image)

        holder.itemView.setOnClickListener {
            listner?.itemClick(position)
        }

    }

    override fun getItemCount() = list?.size ?: 0

    fun updateItem(newList: MutableList<String>) {
        this.list = newList
        notifyDataSetChanged()
    }
}

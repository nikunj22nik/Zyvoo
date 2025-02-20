package com.business.zyvo.adapter.guest

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.activity.guest.propertydetails.model.AddOn
import com.business.zyvo.activity.guest.propertydetails.model.Review
import com.business.zyvo.databinding.AdapterReviewsBinding

class AdapterProReview(var context : Context, var list :MutableList<Review>) :
RecyclerView.Adapter<AdapterProReview.ViewHolder>() {

    class ViewHolder(var binding: AdapterReviewsBinding) : RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterReviewsBinding = AdapterReviewsBinding.inflate(inflater, parent, false);
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list?.get(position)
        currentItem?.profile_image?.let {
            Glide.with(context).load(AppConstant.BASE_URL + currentItem).into(holder.binding.circleImageView)
        }
        currentItem?.reviewer_name?.let {
            holder.binding.tvReviewName.text = it
        }
        currentItem?.review_message?.let {
            holder.binding.tvHostDetail.text = it
        }
        currentItem?.review_rating?.let {
            holder.binding.reviewratingbar.rating = it.toFloat()
        }
        currentItem?.review_date?.let {
            holder.binding.tvReviewDate.text = it
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(list: MutableList<Review>) {
        this.list = list
        notifyDataSetChanged()
    }

}

package com.business.zyvo.adapter.host

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.adapter.guest.AdapterReview
import com.business.zyvo.adapter.guest.AdapterReview.ViewHolder
import com.business.zyvo.databinding.AdapterReviewsBinding
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.Review
import com.business.zyvo.model.host.ReviewerProfileModel

class AdapterReviewHost (
    private val context: Context,
    private var list: MutableList<ReviewerProfileModel>
 ) : RecyclerView.Adapter<AdapterReviewHost.ViewHolder>() {

    class ViewHolder(val binding: AdapterReviewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterReviewsBinding.inflate(inflater, parent, false)
        return AdapterReviewHost.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return list.size
    }

    override fun onBindViewHolder( holder: ViewHolder, position: Int ) {
        holder.binding.txtName.text = list.get(position).reviewer_name
        holder.binding.ratingbar.rating = (list.get(position).review_rating?.toFloat()?:0.0).toFloat()
        holder.binding.txtReviews.text = list.get(position).review_message
        holder.binding.tvDate.text = list.get(position).review_date
        Glide.with(context).load(AppConstant.BASE_URL+list.get(position).profile_image).into(holder.binding.circleImageView)

        if(position == list.size -1){
            holder.binding.v1.visibility = View.GONE
        }
        else{
            holder.binding.v1.visibility = View.VISIBLE
        }
    }

    fun updateAdapter(newList: MutableList<ReviewerProfileModel>) {
        list.addAll(newList)
        notifyDataSetChanged()
    }

}
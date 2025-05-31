package com.business.zyvo.adapter.guest

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.R
import com.business.zyvo.databinding.AdapterReviewsBinding
import com.business.zyvo.fragment.guest.bookingfragment.bookingviewmodel.dataclass.Review

class AdapterReview(
    private val context: Context,
    private var list: MutableList<Review>
) : RecyclerView.Adapter<AdapterReview.ViewHolder>() {

    class ViewHolder(val binding: AdapterReviewsBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = AdapterReviewsBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size // Dynamically return the size of the list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val review = list[position] // Get correct item

        holder.binding.txtName.text = review.name
        holder.binding.txtReviews.text = review.comment.toString()
        holder.binding.ratingbar.rating = review.rating?.toFloat() ?: 0f
        holder.binding.tvDate.text = review.date

        Glide.with(context)
            .load(BuildConfig.MEDIA_URL + review.image)
            .error(R.drawable.ic_circular_img_user)
            .into(holder.binding.circleImageView)

        // Hide last item's view
        holder.binding.v1.visibility = if (position == list.size - 1) View.GONE else View.VISIBLE
    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: MutableList<Review>) {

        list.addAll(newList)
        notifyDataSetChanged()
    }
}
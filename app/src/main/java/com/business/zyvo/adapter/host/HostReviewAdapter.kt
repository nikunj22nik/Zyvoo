package com.business.zyvo.adapter.host

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.databinding.AdapterReviewsBinding
import com.business.zyvo.model.host.HostReviewModel

class HostReviewAdapter : PagingDataAdapter<HostReviewModel, HostReviewAdapter.HostReviewViewHolder>(HostReviewModelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HostReviewViewHolder {
        val binding = AdapterReviewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HostReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HostReviewViewHolder, position: Int) {
        val item = getItem(position)
        item?.let { holder.bind(it) }
    }

    class HostReviewViewHolder(private val binding: AdapterReviewsBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HostReviewModel) {
            // Bind the HostReviewModel data to the view
            Glide.with(binding.root.context).load(AppConstant.BASE_URL+item.profile_image).into(binding.circleImageView)
            binding.txtName.setText(item.reviewer_name)
            item.review_rating?.let {
                binding.ratingbar.rating = it.toFloat()
            }
            binding.txtReviews.setText(item.review_message)
            binding.tvDate.setText(item.review_date)
        }

    }

    class HostReviewModelDiffCallback : DiffUtil.ItemCallback<HostReviewModel>() {
        override fun areItemsTheSame(oldItem: HostReviewModel, newItem: HostReviewModel): Boolean {
            return oldItem.reviewer_name == newItem.reviewer_name // Compare based on unique ID
        }

        override fun areContentsTheSame(oldItem: HostReviewModel, newItem: HostReviewModel): Boolean {
            return oldItem == newItem // Compare contents
        }
    }
}
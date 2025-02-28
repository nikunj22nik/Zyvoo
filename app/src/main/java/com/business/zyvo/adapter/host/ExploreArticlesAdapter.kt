package com.business.zyvo.adapter.host

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.BuildConfig
import com.business.zyvo.R

import com.business.zyvo.databinding.AdapterExploreArticlesBinding
import com.business.zyvo.fragment.both.browseArticleHost.model.Article
import com.business.zyvo.fragment.both.browseArticleHost.model.BrowseArticleModel
import com.business.zyvo.fragment.guest.helpCenter.model.Guide

import com.business.zyvo.model.TransactionModel

class ExploreArticlesAdapter(var context: Context,private var list: MutableList<Article>) :
    RecyclerView.Adapter<ExploreArticlesAdapter.ViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: ExploreArticlesAdapter.onItemClickListener) {
        mListener = listener
    }


    class ViewHolder(var binding: AdapterExploreArticlesBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreArticlesAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding: AdapterExploreArticlesBinding = AdapterExploreArticlesBinding.inflate(inflater,parent,false);
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ExploreArticlesAdapter.ViewHolder, position: Int) {
        val currentItem = list[position]
        if (currentItem.title != null){
            holder.binding.textTitle.text = currentItem.title
        }

        if (currentItem.description != null && !currentItem.description.equals("")){
            holder.binding.textDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(currentItem.description, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(currentItem.description)
            }
        }
        if (currentItem.cover_image != null){
            Glide.with(context)
                .load(BuildConfig.MEDIA_URL + currentItem.cover_image)
                .centerCrop()
                .error(R.drawable.ic_img_not_found)
                .placeholder(R.drawable.ic_img_not_found)
                .into(holder.binding.shapeableImageView)
        }
        holder.binding.main.setOnClickListener {
            mListener.onItemClick(currentItem.id)
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<Article>) {
        this.list = list
        notifyDataSetChanged()
    }


}
package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.AppConstant
import com.business.zyvo.OnClickListener
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutArticlesBinding
import com.business.zyvo.fragment.guest.helpCenter.model.Article
import com.business.zyvo.model.AllArticlesModel

class AdapterAllArticles(var context: Context,
    private var list: MutableList<Article>,
    private  val listener : OnClickListener1
) : RecyclerView.Adapter<AdapterAllArticles.ItemViewHolder>() {

    inner class ItemViewHolder(var binding: LayoutArticlesBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentItem: Article) {
            if (currentItem.title != null){
                binding.textTitle.setText(currentItem.title)
            }
          if (currentItem.description != null){
              binding.textDescription.visibility = View.VISIBLE
              binding.textDescription.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                  Html.fromHtml(currentItem.description, Html.FROM_HTML_MODE_LEGACY)
              } else {
                  Html.fromHtml(currentItem.description)
              }

          }else{
              binding.textDescription.visibility = View.GONE
          }


            binding.root.setOnClickListener{
                if (currentItem.id != null){
                    listener.itemClick(currentItem.id, AppConstant.ARTICLE_SMALL_TEXT)
                }

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val binding =
            LayoutArticlesBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = list[position]

        holder.bind(currentItem)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<Article>) {
        this.list = list
        notifyDataSetChanged()
    }

}
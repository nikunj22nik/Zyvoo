package com.business.zyvo.adapter


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnClickListener1
import com.business.zyvo.databinding.LayoutAddLanguageBinding
import com.business.zyvo.databinding.LayoutAddTextBinding
import com.business.zyvo.model.AddLanguageModel
import com.business.zyvo.onItemClickData

class AddLanguageSpeakAdapter(var context: Context, var list : MutableList<AddLanguageModel>,var listner : OnClickListener1,var listner2 : onItemClickData)
    :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val uploadLanguageCode = 1 // Represents normal location entries
    private val uploadLanguageFixed = 0
    private  var textAddNew : TextView? = null

    inner class LanguageViewHolder(var binding: LayoutAddLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(languageItem: AddLanguageModel) {


            binding.textMyWorkName.text = languageItem.name
            binding.imageCross.setOnClickListener {
                listner.itemClick(adapterPosition, "language")
            }
        }

    }

    inner class LanguageViewHolderFixed(var binding: LayoutAddTextBinding): RecyclerView.ViewHolder(binding.root){

        init {
            binding.root.setOnClickListener {
                listner2.itemClick(adapterPosition,"language","") // Handle "Add New" button click
            }
        }
        fun bind() {
//            binding.root.setOnClickListener {
//                listner.itemClick(adapterPosition, "work") // Handle "Add New" button click
//            }

            if (list.size >= 3) {
                binding.textAddNew.visibility = View.GONE
            } else {
                binding.textAddNew.visibility = View.VISIBLE
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  if (viewType == uploadLanguageCode){
            val binding = LayoutAddLanguageBinding.inflate(LayoutInflater.from(context),parent,false)
            LanguageViewHolder(binding)
        } else{
            val  binding = LayoutAddTextBinding.inflate(LayoutInflater.from(context), parent, false)
            LanguageViewHolderFixed(binding)
        }
    }

    override fun getItemCount() = list.size
    override fun getItemViewType(position: Int): Int {
        return  if (list.size -1 != position){
            uploadLanguageCode
        }
        else{
            uploadLanguageFixed
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is LanguageViewHolder){
            val languageItem = list[position]
            holder.bind(languageItem)
        }
        else if (holder is LanguageViewHolderFixed){
            holder.bind()
            textAddNew = holder.binding.textAddNew
        }

    }

    fun updateLanguage(newList: MutableList<AddLanguageModel>) {

        this.list = newList

        if (list.isEmpty()) {
            list.add(AddLanguageModel("Add New")) // Placeholder for "Add New"
        }
        notifyDataSetChanged()
    }



}
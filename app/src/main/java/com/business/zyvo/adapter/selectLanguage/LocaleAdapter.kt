package com.business.zyvo.adapter.selectLanguage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnLocalListener
import com.business.zyvo.R
import com.business.zyvo.model.AddLanguageModel
import com.business.zyvo.utils.PrepareData
import java.util.Locale

class LocaleAdapter(
    private val locales: List<Locale>,
    var listner: OnLocalListener,
    var thirdList: MutableList<AddLanguageModel> = PrepareData.languageObjects
) :
    RecyclerView.Adapter<LocaleAdapter.LocaleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LocaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocaleViewHolder, position: Int) {

        val locale = thirdList[position]

        // Get language and region names from the Locale
        var languageName = locale.name
        holder.countryName.visibility = View.GONE


        holder.ll1.setOnClickListener {
            listner.onItemClick(languageName)

        }
        holder.languageTitle.text = languageName

    }

    override fun getItemCount(): Int {
        return thirdList.size
    }

    class LocaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val languageTitle: TextView = itemView.findViewById(R.id.languageTitle)
        val countryName: TextView = itemView.findViewById(R.id.countryName)

        val ll1: RelativeLayout = itemView.findViewById(R.id.ll1)
    }
}
package com.yesitlab.zyvo.adapter.selectLanguage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.OnClickListener
import com.yesitlab.zyvo.OnLocalListener
import com.yesitlab.zyvo.R
import java.util.Locale

class LocaleAdapter(private val locales: List<Locale>,var listner : OnLocalListener) :
    RecyclerView.Adapter<LocaleAdapter.LocaleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocaleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language, parent, false)
        return LocaleViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocaleViewHolder, position: Int) {
        val locale = locales[position]


        // Get language and region names from the Locale
        var languageName = locale.getDisplayLanguage(locale) ?: "Unknown Language"
        val regionName = locale.getDisplayCountry(locale) ?: "Unknown Region"

holder.ll1.setOnClickListener {
    listner.onItemClick(languageName)

}

        holder.languageTitle.text = languageName
        holder.countryName.text = regionName
    }

    override fun getItemCount(): Int {
        return locales.size
    }

    class LocaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val languageTitle: TextView = itemView.findViewById(R.id.languageTitle)
        val countryName: TextView = itemView.findViewById(R.id.countryName)
        val ll1: LinearLayout = itemView.findViewById(R.id.ll1)
    }
}
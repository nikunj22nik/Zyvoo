package com.business.zyvo.adapter.selectLanguage

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.OnLocalListener
import com.business.zyvo.R
import com.business.zyvo.model.AddLanguageModel
import com.business.zyvo.session.SessionManager
import com.business.zyvo.utils.PrepareData
import java.util.Locale

class LocaleAdapter(
    private val locales: List<Locale>, var listner: OnLocalListener, var thirdList: MutableList<AddLanguageModel> = PrepareData.languagesWithRegions,
    var languageList: MutableList<AddLanguageModel>
) : RecyclerView.Adapter<LocaleAdapter.LocaleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocaleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_language, parent, false)
        return LocaleViewHolder(view)
    }


    override fun onBindViewHolder(holder: LocaleViewHolder, position: Int) {

        val locale = thirdList[position]
        var languageName = locale.name

        holder.countryName.visibility = View.VISIBLE
Log.d("checkLanguagesName",thirdList.toString())

        if(SessionManager(holder.itemView.context).isLanguageStored(holder.itemView.context,languageName)){
            holder.ll1.setBackgroundResource(R.drawable.blue_button_bg)
        }
        else{
            holder.ll1.setBackgroundResource(R.drawable.button_grey_line_bg)
        }

        holder.countryName.setText(locale.country)
/*
        holder.ll1.setOnClickListener {
           if(SessionManager(holder.itemView.context).isLanguageStored(holder.itemView.context,languageName)){
                  SessionManager(holder.itemView.context).removeLanguage(holder.itemView.context,languageName)
                   holder.ll1.setBackgroundResource(R.drawable.button_grey_line_bg)
           }
           else {
               var list1 = SessionManager(holder.itemView.context).getLanguages((holder.itemView.context)).toMutableList()

               list1.add(locale)

               SessionManager(holder.itemView.context).saveLanguages(holder.itemView.context,list1)
               holder.ll1.setBackgroundResource(R.drawable.blue_button_bg)
           }
           listner.onItemClick(languageName)
        }

 */
        holder.ll1.setOnClickListener {
            if (SessionManager(holder.itemView.context).isLanguageStored(holder.itemView.context, languageName)) {
//                SessionManager(holder.itemView.context).removeLanguage(holder.itemView.context, languageName)
//               holder.ll1.setBackgroundResource(R.drawable.button_grey_line_bg)

                listner.onItemClick(languageName,"delete")
            } else {
                if (languageList.size >= 3) {
                    // Show a toast or prevent action
                    Toast.makeText(holder.itemView.context, "You can select up to 2 languages only", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val list1 = SessionManager(holder.itemView.context).getLanguages(holder.itemView.context).toMutableList()
                list1.add(locale)
                SessionManager(holder.itemView.context).saveLanguages(holder.itemView.context, list1)

                holder.ll1.setBackgroundResource(R.drawable.blue_button_bg)
                listner.onItemClick(languageName, "add")
            }
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
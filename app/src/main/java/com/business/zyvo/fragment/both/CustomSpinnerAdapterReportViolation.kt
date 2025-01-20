package com.business.zyvo.fragment.both

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.business.zyvo.R

class CustomSpinnerAdapterReportViolation(private val context: Context,
                                          private val dataList: List<String>
) : BaseAdapter(), SpinnerAdapter {

    override fun getCount(): Int {
        return dataList.size
    }

    override fun getItem(position: Int): Any {
        return dataList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.custome_spinner_item_report_violation_selected, null)
        }

        val textView = view?.findViewById<TextView>(R.id.tv_spinner_text)
        textView?.text = dataList[position]

        return view!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.custom_spinner_item_report_violation, null)
        }

        val textView = view?.findViewById<TextView>(R.id.tv_spinner_text)
        textView?.text = dataList[position]

        return view!!
    }


}
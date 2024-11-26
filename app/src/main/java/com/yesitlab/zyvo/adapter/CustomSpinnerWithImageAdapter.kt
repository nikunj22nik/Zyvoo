package com.yesitlab.zyvo.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.model.SpinnerModel

class CustomSpinnerWithImageAdapter(context: Context, list: ArrayList<SpinnerModel>) : ArrayAdapter<SpinnerModel>(context, 0, list) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return createView(position, convertView, parent)
    }

    private fun createView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.spinner_with_image_item, parent, false)
        }

        val items = getItem(position)
        val spinnerImage = view!!.findViewById<ImageView>(R.id.spinner_icon)
        val spinnerName = view.findViewById<TextView>(R.id.spinner_text)

        items?.let {
            spinnerImage.setImageResource(it.spinnerImage)
            spinnerName.text = it.spinnerText
        }

        return view
    }
}

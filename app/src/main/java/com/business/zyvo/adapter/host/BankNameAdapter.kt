package com.business.zyvo.adapter.host

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.business.zyvo.R

import com.business.zyvo.databinding.AdapterBankNameBinding
import com.business.zyvo.model.CountryLanguage

class BankNameAdapter(private val context: Context, private var list: MutableList<CountryLanguage>) :
    RecyclerView.Adapter<BankNameAdapter.ViewHolder>(){

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
    fun setOnItemClickListener(listener: BankNameAdapter.onItemClickListener) {
        mListener = listener
    }

    class ViewHolder(var binding: AdapterBankNameBinding) : RecyclerView.ViewHolder(binding.root){}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BankNameAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding:AdapterBankNameBinding = AdapterBankNameBinding.inflate(inflater,parent,false);
        return BankNameAdapter.ViewHolder(binding)
    }

    override fun getItemCount(): Int {
      return 2
    }

    override fun onBindViewHolder(holder: BankNameAdapter.ViewHolder, position: Int) {
       var currentItem = list[position]
        if(position ==0) {
            holder.binding.rlPreferred.visibility = View.VISIBLE
        }
        holder.binding.bnkName.text = currentItem.country
        holder.binding.textDes.text = currentItem.language

        holder.binding.threeDot.setOnClickListener {
            showPopupWindow(holder.binding.threeDot,position)
        }
    }

    fun addItems(newItems: List<CountryLanguage>) {
        val startPosition = list.size
        list.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)
    }

    private fun showPopupWindow(anchorView: View, position: Int) {
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_primary_delete, null)

        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        popupView.findViewById<TextView>(R.id.itemSetPrimary).setOnClickListener {

            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
            dialogDelete()
            popupWindow.dismiss()
        }
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            -(popupWeight + 20)
        } else {
            -(popupWeight + 20)
        }
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        val yOffset = if (popupHeight > spaceBelow!!) {
            -(popupHeight + 20)
        } else {
            20
        }
        popupWindow.elevation = 8.0f
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)
    }

    fun dialogDelete() {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_delete_property)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
            findViewById<ImageView>(R.id.imgCross).setOnClickListener {
                dismiss()
            }
            findViewById<RelativeLayout>(R.id.yes_btn).setOnClickListener {
                dismiss()
            }
            findViewById<RelativeLayout>(R.id.rl_cancel_btn).setOnClickListener {
                dismiss()
            }
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }
}
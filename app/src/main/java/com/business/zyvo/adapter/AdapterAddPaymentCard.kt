package com.business.zyvo.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
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
import com.business.zyvo.activity.guest.checkout.model.UserCards
import com.business.zyvo.databinding.LayoutPaymentCardlistBinding
import com.business.zyvo.onClickSelectCard
import com.stripe.android.model.CardBrand

class AdapterAddPaymentCard(var context : Context, var list: MutableList<UserCards> , var onItemClickData:onClickSelectCard)
    : RecyclerView.Adapter<AdapterAddPaymentCard.PaymentCardViewHolder>() {


    inner  class PaymentCardViewHolder(var binding: LayoutPaymentCardlistBinding)
        : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(currentItem: UserCards) {
            binding.textCardNumber.text = "......"+currentItem.last4
            try {
                Log.d("*******","name :- "+currentItem.brand)
                val icon: Int = CardBrand.valueOf(currentItem.brand).icon
                binding.textCardNumber.setCompoundDrawablesWithIntrinsicBounds(context.getDrawable(icon), null, null, null)
            }catch (e:java.lang.Exception){

            }

            if (currentItem.is_preferred){
                binding.textPreferred.visibility = View.VISIBLE
            }else{
                binding.textPreferred.visibility = View.GONE
            }

            binding.rlThreeDot.setOnClickListener {

                showPopupWindow(binding.rlThreeDot, position = adapterPosition)


            }


            /*if (currentItem.is_preferred) {
                binding.root.setOnClickListener(null) // remove click listener
            } else {
                binding.root.setOnClickListener {
                    setpreferred.set(position)
                }
            }*/



        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentCardViewHolder {
       var binding = LayoutPaymentCardlistBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return  PaymentCardViewHolder(binding)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: PaymentCardViewHolder, position: Int) {
        var currentItem = list[position]
        holder.bind(currentItem)

    }


    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(listData: MutableList<UserCards>)
    {
       list = listData
        notifyDataSetChanged()
    }

    private fun showPopupWindow(anchorView: View, position: Int) {

        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_primary_delete, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        if (list[position].is_preferred){
            popupView.findViewById<TextView>(R.id.itemSetPrimary).visibility = View.GONE
        }else{
            popupView.findViewById<TextView>(R.id.itemSetPrimary).visibility = View.VISIBLE
        }

        popupView.findViewById<TextView>(R.id.itemSetPrimary).setOnClickListener {
            onItemClickData.itemClickCard(pos = position , type = "primary")
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
//            dialogDelete()

            dialogDelete(position)

            popupWindow.dismiss()
        }

        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            -(popupWeight + 20)
        } else {
            -(popupWeight + 20)
        }
        val screenHeight = context.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))
        val yOffset = if (popupHeight > spaceBelow!!) { -(popupHeight + 20)
        } else {
            20
        }
        popupWindow.elevation = 8.0f
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)
    }

    @SuppressLint("MissingInflatedId")
    fun dialogDelete(position: Int) {
        val dialog = context?.let { Dialog(it, R.style.BottomSheetDialog) }
        dialog?.apply {
            setCancelable(false)
            setContentView(R.layout.dialog_delete_property)
            window?.attributes = WindowManager.LayoutParams().apply {
                copyFrom(window?.attributes)
                width = WindowManager.LayoutParams.MATCH_PARENT
                height = WindowManager.LayoutParams.MATCH_PARENT
            }
              findViewById<TextView>(R.id.textCard).text = "Are you sure you want to delete this card?"


            findViewById<ImageView>(R.id.imgCross).setOnClickListener {
                dismiss()
            }

            findViewById<RelativeLayout>(R.id.yes_btn).setOnClickListener {
                onItemClickData.itemClickCard(pos = position , type = "delete")
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

interface SetPreferred {
    fun set(position: Int)

}

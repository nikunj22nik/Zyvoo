package com.business.zyvo.adapter.host

import android.annotation.SuppressLint
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
import com.business.zyvo.databinding.LayoutPaymentDetailsBinding
import com.business.zyvo.model.PaymentCardModel
import com.business.zyvo.onItemClickData

class PaymentAdapter(
    var context: Context,
    var list: MutableList<PaymentCardModel>, private var listner : onItemClickData
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {


    inner class PaymentViewHolder(var binding: LayoutPaymentDetailsBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding =
            LayoutPaymentDetailsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val currentItem = list.get(position)
        with(holder.binding) {
//            binding.textCardHolderName.text = currentItem.cardHolderName
//            binding.textBankAccount.text = currentItem.bankName
//            binding.textStartNumber.text = currentItem.cardFirstNumber
//            binding.textEndNumber.text = currentItem.cardEndNumber

            if (currentItem.isBankAccount) {
                textCardHolderName.visibility = View.GONE  // Hide cardholder name for bank accounts
                textBankAccount.visibility = View.VISIBLE
                textBankAccount.text = currentItem.bankName
                if (currentItem.defaultForCurrency){
                    imagePrimaryIcon.visibility = View.VISIBLE
                }else{
                    imagePrimaryIcon.visibility = View.INVISIBLE
                }
            } else {
                textCardHolderName.visibility = View.VISIBLE
                textCardHolderName.text = currentItem.cardHolderName
                textBankAccount.visibility = View.GONE
                if (currentItem.defaultForCurrency){
                    imagePrimaryIcon.visibility = View.VISIBLE
                }else{
                    imagePrimaryIcon.visibility = View.INVISIBLE
                }
            }

            imageThreeDots.setOnClickListener {
                currentItem.id?.let { it1 -> showPopupWindow(holder.binding.imageThreeDots,position, it1) }

            }


        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(list: MutableList<PaymentCardModel>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    private fun showPopupWindow(anchorView: View,position: Int, id: String) {
        // Inflate the custom layout for the popup menu
        val popupView =
            LayoutInflater.from(context).inflate(R.layout.popup_primary_delete, null)

        // Create PopupWindow with the custom layout
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Set click listeners for each menu item in the popup layout
        popupView.findViewById<TextView>(R.id.itemSetPrimary).setOnClickListener {
            listner.itemClick(position ,"setPrimary",  id)
            popupWindow.dismiss()
        }
        popupView.findViewById<TextView>(R.id.itemDelete).setOnClickListener {
            dialogDelete(position ,id)


            popupWindow.dismiss()
        }



        // Get the location of the anchor view (three-dot icon)
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)

        // Get the height of the PopupView after inflating it
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val popupHeight = popupView.measuredHeight
        val popupWeight = popupView.measuredWidth
        val screenWidht = context?.resources?.displayMetrics?.widthPixels
        val anchorX = location[1]
        val spaceEnd = screenWidht?.minus((anchorX + anchorView.width))

        val xOffset = if (popupWeight > spaceEnd!!) {
            // If there is not enough space below, show it above
            -(popupWeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            // 20 // This adds a small gap between the popup and the anchor view
            -(popupWeight + 20)
        }
        // Calculate the Y offset to make the popup appear above the three-dot icon
        val screenHeight = context?.resources?.displayMetrics?.heightPixels
        val anchorY = location[1]

        // Calculate the available space above the anchorView
        val spaceAbove = anchorY
        val spaceBelow = screenHeight?.minus((anchorY + anchorView.height))

        // Determine the Y offset
        val yOffset = if (popupHeight > spaceBelow!!) {
            // If there is not enough space below, show it above
            -(popupHeight + 20) // Adjust this value to add a gap between the popup and the anchor view
        } else {
            // Otherwise, show it below
            20 // This adds a small gap between the popup and the anchor view
        }

        // Show the popup window anchored to the view (three-dot icon)
        popupWindow.elevation = 8.0f  // Optional: Add elevation for shadow effect
        popupWindow.showAsDropDown(anchorView, xOffset, yOffset, Gravity.END)  // Adjust the Y offset dynamically
    }

    fun dialogDelete(position: Int,id : String) {
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
                listner.itemClick(position,"delete",id)
                dismiss()
            }
            findViewById<RelativeLayout>(R.id.rl_cancel_btn).setOnClickListener {

                dismiss()
            }

            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            show()
        }
    }

    private fun removeItem(position: Int) {
        if (position >= 0 && position < list.size) {
            list.removeAt(position)            // Remove item from list
            notifyItemRemoved(position)        // Notify adapter about item removal
            notifyItemRangeChanged(position, list.size) // Optional: refresh range
        }
    }

}
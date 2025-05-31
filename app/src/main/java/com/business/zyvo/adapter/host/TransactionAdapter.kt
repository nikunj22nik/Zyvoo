package com.business.zyvo.adapter.host

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.BuildConfig
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutTransactionBinding
import com.business.zyvo.fragment.host.payments.model.GetBookingList
import com.business.zyvo.model.TransactionModel


class TransactionAdapter(
    var context: Context,
    private var transactionsList: MutableList<GetBookingList>
) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView =
            LayoutTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(itemView)
    }

    private fun setHeaderBg(view: View) {
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.headerPaymentColor))
    }

    private fun setContentBg(view: View) {
        //    view.setBackgroundColor(Color.parseColor("#FFFFFF"))
        try {

            view.setBackgroundColor(ContextCompat.getColor(context, R.color.contentPaymentColor))
        } catch (e: IllegalArgumentException) {
            Log.e("TransactionAdapter", "Invalid color string", e)
        }
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val rowPos = holder.bindingAdapterPosition
        if (rowPos == 0) {
            // Header Cells. Main Headings appear here
            holder.binding.profileImage.visibility = View.GONE
            holder.apply {
                setHeaderBg(binding.rlMain)


                binding.tvAmount.text = "Amount"
                binding.tvStatus.text = "Status"
                binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                binding.tvGuestName.text = "Guest Name"
                binding.tvDate.text = "Date"
            }

        } else {
            val modal = transactionsList[rowPos - 1]
            holder.binding.profileImage.visibility = View.VISIBLE

            holder.apply {
                setContentBg(binding.rlMain)

                if (modal.booking_amount != null) {
                    binding.tvAmount.text = "$" + modal.booking_amount
                }

                var status = ""
                if (modal.status != null) {
                    status = modal.status.uppercase()
                    if (modal.status == "finished"){
                        binding.tvStatus.text = "COMPLETED"
                    }else{
                        binding.tvStatus.text = status
                    }

                }

                if (modal.guest_name != null) {
                    binding.tvGuestName.text = modal.guest_name
                }

                if (modal.booking_date != null) {
                    binding.tvDate.text = modal.booking_date
                }


                if (modal.guest_profile_image != null){
                    Glide.with(context)
                        .load(BuildConfig.MEDIA_URL + modal.guest_profile_image)
                        .centerCrop()
                        .error(R.drawable.ic_img_not_found)
                        .placeholder(R.drawable.ic_img_not_found)
                        .into(binding.profileImage)
                }



            }
            if (rowPos != 0){
                when (modal.status) {
                    "pending" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                    "completed" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                    "canceled" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.grey_button_bg)
                    "finished" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                    "confirmed" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                    "Status" -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                    "status" -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                    else -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                }
            }else{
                when (modal.status) {
                    "Status" -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                    "status" -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                    else -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                }
            }


        }
    }

    override fun getItemCount(): Int {
        return transactionsList.size + 1 // one more to add header row
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newList: MutableList<GetBookingList>) {
        this.transactionsList.clear()
        this.transactionsList.addAll(newList)
        //   this.transactionsList = newList
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(var binding: LayoutTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}

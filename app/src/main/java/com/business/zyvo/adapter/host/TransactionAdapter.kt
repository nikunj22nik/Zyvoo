package com.business.zyvo.adapter.host

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.business.zyvo.AppConstant
import com.business.zyvo.BuildConfig
import com.business.zyvo.R
import com.business.zyvo.databinding.LayoutTransactionBinding
import com.business.zyvo.fragment.host.payments.model.GetBookingList


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


                binding.tvAmount.text = AppConstant.AMOUNT
                binding.tvStatus.text = AppConstant.STATUS
                binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                binding.tvGuestName.text = AppConstant.GUEST_NAME
                binding.tvDate.text = AppConstant.DATE_TEXT
            }

        } else
        {
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
                    if (modal.status == AppConstant.FINISHED){
                        binding.tvStatus.text = AppConstant.COMPLETED_TEXT
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
                   AppConstant.PENDING -> holder.binding.tvStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                    AppConstant.COMPLETED_SMALL_TEXT -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                    AppConstant.CANCEL -> holder.binding.tvStatus.setBackgroundResource(R.drawable.grey_button_bg)
                   AppConstant.FINISHED -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                   AppConstant.CONFIRMED  -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                   AppConstant.STATUS -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                   AppConstant.STATUS_SMALL_TEXT -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                    else -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                }
            }else{
                when (modal.status) {
                    AppConstant.STATUS -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                   AppConstant.STATUS_SMALL_TEXT -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                    else -> holder.binding.tvStatus.setBackgroundResource(android.R.color.transparent)
                }
            }


        }

        if (holder.binding.tvAmount.text == AppConstant.AMOUNT ){
            holder.binding.tvAmount.setTextColor(Color.parseColor("#000000"))
        }else{
            holder.binding.tvAmount.setTextColor(Color.parseColor("#252849"))
        }

        if (holder.binding.tvStatus.text == AppConstant.STATUS ){
            holder.binding.tvStatus.setTextColor(Color.parseColor("#000000"))
        }else{
            holder.binding.tvStatus.setTextColor(Color.parseColor("#252849"))
        }

        if (holder.binding.tvGuestName.text == AppConstant.GUEST_NAME ){
            holder.binding.tvGuestName.setTextColor(Color.parseColor("#000000"))
        }else{
            holder.binding.tvGuestName.setTextColor(Color.parseColor("#252849"))
        }

        if (holder.binding.tvDate.text == AppConstant.DATE_TEXT ){
            holder.binding.tvDate.setTextColor(Color.parseColor("#000000"))
        }else{
            holder.binding.tvDate.setTextColor(Color.parseColor("#252849"))
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

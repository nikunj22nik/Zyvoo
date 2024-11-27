package com.yesitlab.zyvo.adapter.host

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yesitlab.zyvo.R
import com.yesitlab.zyvo.databinding.LayoutTransactionBinding
import com.yesitlab.zyvo.model.TransactionModel

class TransactionAdapter(private var transactionsList: ArrayList<TransactionModel>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutTransactionBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return TransactionViewHolder(itemView)
    }

    private fun setHeaderBg(view: View) {
        view.setBackgroundColor(Color.parseColor("#EFF2F5"))
    }

    private fun setContentBg(view: View) {
        view.setBackgroundColor(Color.parseColor("#FFFFFF"))
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
                binding.tvGuestName.text = "Guest Name"
                binding.tvDate.text = "Date"
            }

        } else {
            val modal = transactionsList[rowPos - 1]
            holder.binding.profileImage.visibility = View.VISIBLE

            holder.apply {
                setContentBg(binding.rlMain)

                binding.tvAmount.text = modal.amount.toString()
                binding.tvStatus.text = modal.status
                binding.tvGuestName.text = modal.guestName.toString()
                binding.tvDate.text = modal.date.toString()


            }



            when (modal.status) {
                "Pending" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.yellow_button_bg)
                "Completed" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.button_bg)
                "Canceled" -> holder.binding.tvStatus.setBackgroundResource(R.drawable.grey_button_bg)
            }

        }
    }

    override fun getItemCount(): Int {
        return transactionsList.size + 1 // one more to add header row
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItem(newList : ArrayList<TransactionModel>){
        this.transactionsList = newList
notifyDataSetChanged()
    }

    inner class TransactionViewHolder(var binding : LayoutTransactionBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}

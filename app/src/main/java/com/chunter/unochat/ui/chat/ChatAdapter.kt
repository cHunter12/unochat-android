package com.chunter.unochat.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.chunter.unochat.R
import com.chunter.unochat.ui.chat.ChatAdapter.ChatViewHolder
import com.google.android.material.chip.Chip

class ChatAdapter(
    private val userId: String
) : ListAdapter<Message, ChatViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.list_item_chat,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChatViewHolder(itemView: View) : ViewHolder(itemView) {

        fun bind(message: Message) {
            val context = itemView.context
            val isUserMessage = userId == message.senderId
            val messageLayout = itemView.findViewById<LinearLayout>(R.id.messageLayout)
            messageLayout.gravity = if (isUserMessage) Gravity.END else Gravity.START

            val messageChip = itemView.findViewById<Chip>(R.id.message)
            messageChip.text = message.content
            messageChip.setTextColor(
                if (isUserMessage) context.getColor(R.color.white) else context.getColor(
                    R.color.colorText
                )
            )
            messageChip.chipBackgroundColor =
                ContextCompat.getColorStateList(
                    context,
                    if (isUserMessage) R.color.colorPrimary else R.color.porcelain
                )
        }
    }

    companion object {

        private val diffUtil = object : DiffUtil.ItemCallback<Message>() {

            override fun areItemsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem

            override fun areContentsTheSame(oldItem: Message, newItem: Message) =
                oldItem.id == newItem.id
        }
    }
}
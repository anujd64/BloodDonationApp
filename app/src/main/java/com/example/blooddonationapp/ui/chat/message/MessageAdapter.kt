package com.example.blooddonationapp.ui.chat.message

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.ItemMessageBinding
import com.example.blooddonationapp.databinding.ItemPostBinding
import com.example.blooddonationapp.model.ChatMessage
import com.example.blooddonationapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.database.Query

class MessageAdapter(
    private val currentUid: String,
    options: FirestoreRecyclerOptions<ChatMessage>
) : FirestoreRecyclerAdapter<ChatMessage, MessageViewHolder>(options){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }


    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, message: ChatMessage) {
        Log.d("MessageAdapter", "onBindViewHolder: ${message.toString()}")

        if (currentUid == message.senderId) {
            holder.binding.rightChatTextView.text = message.message
            holder.binding.rightTimeTextView.text = FirebaseUtil.timestampToString(message.timestamp!!)
            holder.binding.leftContainer.visibility = View.GONE
        } else {
            holder.binding.leftChatTextView.text = message.message
            holder.binding.leftTimeTextView.text = FirebaseUtil.timestampToString(message.timestamp!!)
            holder.binding.rightContainer.visibility = View.GONE
        }
    }

}

class MessageViewHolder(val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

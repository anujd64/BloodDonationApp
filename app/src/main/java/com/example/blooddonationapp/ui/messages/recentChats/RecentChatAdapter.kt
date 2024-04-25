package com.example.blooddonationapp.ui.messages.recentChats

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.blooddonationapp.databinding.ItemMessageBinding
import com.example.blooddonationapp.databinding.ItemUserBinding
import com.example.blooddonationapp.model.ChatMessage
import com.example.blooddonationapp.model.ChatRoom
import com.example.blooddonationapp.model.User
import com.example.blooddonationapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale


class RecentChatAdapter(
    private val currentUid: String,
    private val handleClick: (userId: String) -> Unit,
    private val showPopupMenu: (view: View, chatRoomId: String, position: Int) -> Unit,
    private val chatList : MutableList<ChatRoom>

) : RecyclerView.Adapter<ChatRoomViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatRoomViewHolder(binding)
    }


    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {

        val chatRoom = chatList[position]

        Log.d("RecentChatAdapter", "onBindViewHolder: ${chatRoom.toString()}")
        var user1: User? = null
        var user2: User? = null
        CoroutineScope(Dispatchers.IO).launch {
            user1 = FirebaseUtil.getUserDetails(chatRoom.user1)
            user2 = FirebaseUtil.getUserDetails(chatRoom.user2)

            Log.d("RecentChatAdapter", user1.toString())
            Log.d("RecentChatAdapter", user2.toString())


            CoroutineScope(Dispatchers.Main).launch {
                if (user1 != null && user2 != null) {
                    holder.binding.root.setOnClickListener {
                        if (currentUid == chatRoom.user1) {
                            handleClick(chatRoom.user2)
                        } else {
                            handleClick(chatRoom.user1)
                        }
                    }

                    holder.binding.root.setOnLongClickListener {
                        Log.d("RecentChatAdapter", "onLongClick: Triggered")
                        // Show popup menu
                        showPopupMenu(holder.binding.root, chatRoom.chatRoomId, position)
                        true
                    }

                    if (chatRoom.lastMessageTimestamp != null) {
                        holder.binding.textTime.text =FirebaseUtil.timestampToString(chatRoom.lastMessageTimestamp!!)
                    }

                    if (currentUid == user1?.userId) {
                        holder.binding.textUserName.text = user2?.name
                    } else {
                        holder.binding.textUserName.text = user1?.name
                    }

                    holder.binding.textLastMessage.text = chatRoom.lastMessage


                    if (user2?.imageUrl!!.isNotEmpty() && user2?.userId != currentUid) {
                        Glide.with(holder.binding.root)
                            .load(user2?.imageUrl)
                            .thumbnail(0.3f,)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.binding.imageUser)
                    } else if (user1?.imageUrl!!.isNotEmpty() && chatRoom.lastMessageSenderId == currentUid) {
                        //reduce the quality of the image to 30%
                        Glide.with(holder.binding.root).load(user1?.imageUrl!!)
                            .thumbnail(0.3f,)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.binding.imageUser)
                    }

                }
            }

        }
    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    fun setChatList(chatList: List<ChatRoom>) {
        this.chatList.clear()
        this.chatList.addAll(chatList)
        notifyDataSetChanged()
    }
}

class ChatRoomViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

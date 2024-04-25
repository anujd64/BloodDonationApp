package com.example.blooddonationapp.ui.messages

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.FragmentRecentChatsBinding
import com.example.blooddonationapp.model.ChatRoom
import com.example.blooddonationapp.ui.messages.recentChats.RecentChatAdapter
import com.example.blooddonationapp.utils.FirebaseUtil
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class RecentChatsFragment : Fragment() {

    private var _binding: FragmentRecentChatsBinding? = null
    private val viewModel: RecentChatsViewModel by viewModels()
    private lateinit var recentChatAdapter: RecentChatAdapter
    private val binding get() = _binding!!

    private val chatList = mutableListOf<ChatRoom>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecentChatsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        setUpRecyclerView(viewModel.getUser1Id())
        return root
    }

    private fun setUpRecyclerView(user1Id: String) {

        binding.recyclerViewChats.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        recentChatAdapter = RecentChatAdapter(
            user1Id,
            handleClick = { userId ->
                val action = RecentChatsFragmentDirections.actionNavigationNotificationsToChatFragment(userId)
                findNavController().navigate(action)
            },
            showPopupMenu = { _view, chatRoomId, position ->
                // Show popup menu
                Log.d("RecentChatsFragment", "showPopupMenu called")
                val popUpMenu = PopupMenu(requireContext(), _view)
                popUpMenu.menuInflater.inflate(R.menu.menu_delete, popUpMenu.menu)
                popUpMenu.show()
                popUpMenu.setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.deleteChatRoom -> {
                            viewModel.deleteChatRoom(chatRoomId){result ->
                                if (result){
                                    viewModel.getChatListResource()
                                }else{
                                    Snackbar.make(binding.root,"Failed to delete chatRoom", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                            true
                        }
                        else -> false
                    }
                }
            },
            chatList = mutableListOf()
        )
        binding.recyclerViewChats.adapter = recentChatAdapter

        viewModel.getChatListResource()

        viewModel.chats.onEach {
            when(it){
                is Resource.Loading -> {
                    Log.d("RecentChatsFragment", "Loading")
                }
                is Resource.Success -> {
                    Log.d("RecentChatsFragment", "Success")
                    chatList.clear()
                    chatList.addAll(it.data as List<ChatRoom>)
                        recentChatAdapter.setChatList(chatList)
                        recentChatAdapter.notifyDataSetChanged()
                }
                is Resource.Error -> {
                    Log.d("RecentChatsFragment", "Error")
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
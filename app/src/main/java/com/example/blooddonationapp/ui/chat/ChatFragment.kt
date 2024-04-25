package com.example.blooddonationapp.ui.chat

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blooddonationapp.databinding.FragmentChatBinding
import com.example.blooddonationapp.model.ChatRoom
import com.example.blooddonationapp.ui.chat.message.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels()
    private val args: ChatFragmentArgs by navArgs()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getChatRoomId(auth.currentUser?.uid.toString(), args.userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user1Id = auth.currentUser?.uid.toString()
        val user2Id = args.userId

        if (user1Id == user2Id || user2Id.isEmpty()) {
            Log.e("ChatFragment", "User cannot chat with themselves")
            return
        }


        lifecycleScope.launch(Dispatchers.Main) {
            viewModel.getOtherUserDetails(user2Id)
            viewModel.otherUser.collectLatest {
                binding.textUsername.text = it.name
                Glide.with(requireContext()).load(it.imageUrl).into(binding.imageUser)
            }
        }

        binding.backButton.setOnClickListener {
            findNavController().popBackStack()
        }

        viewModel.getOrCreateChatRoom(user1Id, user2Id)

        var chatRoom: ChatRoom? = null

        viewModel.chatRoomState.onEach { state ->
            when (state) {
                is ChatRoomState.Success -> {
                    chatRoom = state.chatRoom
                }

                is ChatRoomState.Error -> {
                    Log.e("ChatFragment", "Error: ${state.message}")
                }

                is ChatRoomState.Loading -> {
                    Log.d("ChatFragment", "Loading")
                }
            }
        }.launchIn(CoroutineScope(Dispatchers.IO))

        binding.sendMessageButton.setOnClickListener {
            // Send message
            val message = binding.messageEditText.text.toString().trim()
            if (message.isEmpty()) {
                binding.messageEditText.error = "Message cannot be empty"
                return@setOnClickListener
            }

            if (chatRoom != null) {
                viewModel.sendMessage(chatRoom!!, message)
            }

            setUpRecyclerView(user1Id)
        }

        viewModel.sendMessageState.onEach {
            when (it) {
                is SendMessageState.Success -> {
                    binding.messageEditText.text.clear()
                }

                is SendMessageState.Error -> {
                    Log.e("ChatFragment", "Error sending message: ${it.message}")
                }

                is SendMessageState.Sending -> {
                    Log.d("ChatFragment", "Sending message")
                }
            }

        }.launchIn(viewLifecycleOwner.lifecycleScope)

        setUpRecyclerView(user1Id)


    }

    private fun setUpRecyclerView(user1Id: String) {
        val options = viewModel.getQueryOptions()

        binding.chatRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, true)
        messageAdapter = MessageAdapter(
            user1Id, options
        )
        binding.chatRecyclerView.adapter = messageAdapter

        messageAdapter.startListening()

        messageAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.chatRecyclerView.smoothScrollToPosition(0)
            }
        })
    }

    override fun onPause() {
        messageAdapter.stopListening()
        super.onPause()
    }

    override fun onResume() {
        messageAdapter.notifyDataSetChanged()
        super.onResume()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messageAdapter.stopListening()
        _binding = null
    }
}

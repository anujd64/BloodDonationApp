package com.example.blooddonationapp.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.FragmentHomeBinding
import com.example.blooddonationapp.model.Post
import com.example.blooddonationapp.ui.home.posts.PaginationHelper
import com.example.blooddonationapp.ui.home.posts.Post2Adapter
import com.example.blooddonationapp.ui.home.posts.PostAdapter
import com.example.blooddonationapp.ui.messages.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val homeViewModel: HomeViewModel by viewModels()
    private val binding get() = _binding!!

    private lateinit var postAdapter: PostAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_createPostFragment)
        }

        recyclerView = binding.postRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context) // Change layout manager if needed

        postAdapter = PostAdapter(
            currentUid = FirebaseAuth.getInstance().uid.toString(),
            handleRespondButton = { post ->
                val userId = post.authorId
                val action = HomeFragmentDirections.actionNavigationHomeToChatFragment(userId)
                findNavController().navigate(action)
            },
            handleDeleteButton = {
                homeViewModel.deletePost(it.postId, it.imageName)
            },
            posts = mutableListOf()
        )
        recyclerView.adapter = postAdapter

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        homeViewModel.getPosts()
        homeViewModel.posts.onEach { resource ->
            when (resource) {
                is Resource.Success -> {
                    postAdapter.setPosts(resource.data!!)
                }
                is Resource.Error -> {
                    Log.d("HomeFragment", "Error: ${resource.message}")
                }
                is Resource.Loading -> {
                    Log.d("HomeFragment", "Loading")
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
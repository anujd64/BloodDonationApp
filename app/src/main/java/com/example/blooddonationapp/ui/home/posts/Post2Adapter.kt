package com.example.blooddonationapp.ui.home.posts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.blooddonationapp.databinding.ItemLoadingBinding
import com.example.blooddonationapp.databinding.ItemPostBinding
import com.example.blooddonationapp.model.Post

class Post2Adapter(
private val currentUid: String,
    private val handleRespondButton: (Post) -> Unit,
    private val handleDeleteButton: (Post) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_POST = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    private val posts = mutableListOf<Post>()
    private var isLoading = false

    fun setPosts(posts: List<Post>) {
        this.posts.clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    fun addPosts(posts: List<Post>) {
        val startPosition = this.posts.size
        this.posts.addAll(posts)
        notifyItemRangeInserted(startPosition, posts.size)
    }

    fun setLoading(loading: Boolean) {
        if (isLoading == loading) return

        isLoading = loading
        if (loading) {
            posts.add(Post()) // Add a placeholder for loading footer
            notifyItemInserted(posts.size - 1)
        } else {
            posts.removeAt(posts.size - 1) // Remove the loading footer
            notifyItemRemoved(posts.size)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == posts.size - 1 && isLoading) VIEW_TYPE_LOADING else VIEW_TYPE_POST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_POST) {
            val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            PostViewHolder(binding)
        } else {
            val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            LoadingViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW_TYPE_POST) {
            val postHolder = holder as PostViewHolder
            val post = posts[position]
            // Bind data to the post view holder
            postHolder.bind(post)
        }
    }

    override fun getItemCount(): Int = posts.size

    inner class PostViewHolder(private val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            // Bind post data to the views using binding

            binding.postTitle.text = post.title
            binding.patientName.text = post.patientName
            binding.patientAddress.text = post.address
            binding.patientBloodType.text = post.bloodGroup
            binding.patientPhoneNumber.text = post.phoneNumber
            binding.respondButton.setOnClickListener{
                handleRespondButton(post)
            }
            if (currentUid == post.authorId){
                binding.deleteButton.visibility = View.VISIBLE
                binding.respondButton.visibility = View.GONE
            }
            else{
                binding.deleteButton.visibility = View.GONE
                binding.respondButton.visibility = View.VISIBLE
            }
            binding.deleteButton.setOnClickListener{
                handleDeleteButton(post)
            }
            Glide.with(binding.root.context)
                .load(post.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.postImage)
        }
    }

    inner class LoadingViewHolder(binding: ItemLoadingBinding) : RecyclerView.ViewHolder(binding.root)
}

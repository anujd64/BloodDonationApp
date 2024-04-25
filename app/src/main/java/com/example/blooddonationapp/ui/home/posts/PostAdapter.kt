package com.example.blooddonationapp.ui.home.posts

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.blooddonationapp.databinding.ItemPostBinding
import com.example.blooddonationapp.model.Post
import com.example.blooddonationapp.utils.FirebaseUtil
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions

class PostAdapter(
    private val currentUid: String,
    private val posts: MutableList<Post>,
    private val handleRespondButton: (Post) -> Unit,
    private val handleDeleteButton: (Post) -> Unit,
) : RecyclerView.Adapter<PostViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    fun setPosts(posts: List<Post>) {
        this.posts.clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]

        Log.d("PostAdapter", "Binding post: $post")
        holder.binding.postTitle.text = post.title
        holder.binding.patientName.text = post.patientName
        holder.binding.patientAddress.text = post.address
        holder.binding.patientBloodType.text = post.bloodGroup
        holder.binding.patientPhoneNumber.text = post.phoneNumber
        holder.binding.respondButton.setOnClickListener{
            handleRespondButton(post)
        }

        holder.binding.postTime.text = FirebaseUtil.timestampToString(post.timestamp)

        if (currentUid == post.authorId){
            holder.binding.deleteButton.visibility = View.VISIBLE
            holder.binding.respondButton.visibility = View.GONE
        }
        else{
            holder.binding.deleteButton.visibility = View.GONE
            holder.binding.respondButton.visibility = View.VISIBLE
        }
        holder.binding.deleteButton.setOnClickListener{
            handleDeleteButton(post)
        }
        Glide.with(holder.binding.root.context)
            .load(post.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.postImage)
    }

}

class PostViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

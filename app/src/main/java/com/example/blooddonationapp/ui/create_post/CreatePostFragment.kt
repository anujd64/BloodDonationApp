package com.example.blooddonationapp.ui.create_post

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.FragmentCreatePostBinding
import com.example.blooddonationapp.model.Post
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.UUID


class CreatePostFragment : Fragment() {

    private val binding: FragmentCreatePostBinding get() = _binding!!
    private var _binding: FragmentCreatePostBinding? = null
    private val viewModel: CreatePostViewModel by viewModels()
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)

        val bloodGroupOptions = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodGroupOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.bloodGroupSpinner.adapter = adapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSelectImage.setOnClickListener {
            selectImage()
        }

        binding.buttonCreatePost.setOnClickListener {
            val title = binding.editTextTitle.text.toString()
            val patientName = binding.editTextPatientName.text.toString()
            val address = binding.editTextAddress.text.toString()
            val bloodType = binding.bloodGroupSpinner.selectedItem.toString()
            val phoneNumber = binding.editTextPhoneNumber.text.toString()

            // Validate input
            if (patientName.isNotEmpty() && address.isNotEmpty() && bloodType.isNotEmpty() && phoneNumber.isNotEmpty()) {
                val post = Post(
                    postId = UUID.randomUUID().toString(),
                    title = title,
                    authorId = firebaseAuth.uid.toString(),
                    patientName = patientName,
                    address = address,
                    author = firebaseAuth.currentUser?.displayName ?: "",
                    bloodGroup = bloodType,
                    phoneNumber = phoneNumber,
                    imageUrl = selectedImageUri.toString()
                )

                viewModel.createPostState.onEach { state ->
                    when (state) {
                        CreatePostState.Loading -> {
                            // Show a progress indicator (e.g., ProgressBar)
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        CreatePostState.Success -> {
                            // Hide progress indicator, show success message
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(binding.root, "Post created successfully!", Snackbar.LENGTH_LONG).show()
                            findNavController().navigate(R.id.action_createPostFragment_to_navigation_home)
                        }
                        is CreatePostState.Error -> {
                            // Hide progress indicator, show error message
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        }
                    }
                }.launchIn(viewLifecycleOwner.lifecycleScope)

                viewModel.uploadImageAndCreatePost(post)

            } else {
                validateInputs(
                    title = title,
                    patientName = patientName,
                    address = address,
                    bloodGroup = bloodType,
                    phoneNumber = phoneNumber,
                    imageUrl = selectedImageUri.toString()
                )
            }
        }

    }

    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.imageViewSelectedImage.setImageURI(it)
        }
    }

    private fun selectImage() {
        selectImageLauncher.launch("image/*")
    }

    private fun validateInputs(
        title: String,
        patientName: String,
        address: String,
        bloodGroup: String,
        phoneNumber: String,
        imageUrl: String
    ): Boolean {
        if (title.isEmpty()) {
            binding.editTextTitle.error = "Title is required!"
        }

        if (patientName.isEmpty()) {
            binding.editTextPatientName.error = "Patient name is required!"
        }

        if (address.isEmpty()) {
            binding.editTextAddress.error = "Address is required!"
        }

        if (bloodGroup.isEmpty()) {
            (binding.bloodGroupSpinner.selectedView as TextView).error = "Blood type is required!"
        }

        if (phoneNumber.isEmpty()) {
            binding.editTextPhoneNumber.error = "Phone number is required!"
        }
        if (imageUrl.isEmpty()){
            Snackbar.make(binding.root, "Please select an image!", Snackbar.LENGTH_LONG).show()
        }
        return true
    }
}

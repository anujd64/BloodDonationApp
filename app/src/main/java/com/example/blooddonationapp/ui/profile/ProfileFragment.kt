package com.example.blooddonationapp.ui.profile

import android.content.Intent
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.blooddonationapp.AuthActivity
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.FragmentProfileBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels()
    private val binding: FragmentProfileBinding get() = _binding!!
    private var _binding: FragmentProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getUserProfile()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.profileState.onEach {
            when (it) {
                is ProfileState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.textViewName.text = buildString {
                        append("Name: ")
                        append(it.user.name)
                    }
                    binding.textViewEmail.text = buildString {
                        append("Email: ")
                        append(it.user.email)
                    }
                    binding.textViewPhoneNumber.text = buildString {
                        append("Phone: ")
                        append(it.user.phoneNumber)
                    }
                    binding.textViewAddress.text = buildString {
                        append("Address: ")
                        append(it.user.address)
                    }
                    binding.textViewBloodGroup.text = buildString {
                        append("Blood Group: ")
                        append(it.user.bloodGroup)
                    }
                    if (it.user.imageUrl.isEmpty()) {
                        Glide.with(requireContext())
                            .load(it.user.imageUrl)
                            .placeholder(R.drawable.baseline_person_24)
                            .into(binding.imageViewProfile)
                    } else {
                        Glide.with(requireContext())
                            .load(it.user.imageUrl)
                            .into(binding.imageViewProfile)
                        binding.buttonUploadImage.text = "Change Image"
                    }
                }

                is ProfileState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Log.d("ProfileFragment", "Error getting user profile: ${it.message}")
                }

                ProfileState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    Log.d("ProfileFragment", "Loading user profile")
                }
            }
        }.launchIn(viewLifecycleOwner.lifecycleScope)

        binding.buttonUploadImage.setOnClickListener {
            selectImage()
            if (selectedImageUri == null) {
                return@setOnClickListener
            }
            Log.d("ProfileFragment", "Selected image: $selectedImageUri")
            viewModel.uploadProfileImage(selectedImageUri!!)
        }

        binding.buttonLogout.setOnClickListener {
            viewModel.logout()
            val intent = Intent(requireContext(), AuthActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    private var selectedImageUri: Uri? = null

    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
        }
    }

    private fun selectImage() {
        selectImageLauncher.launch("image/*")
    }
}
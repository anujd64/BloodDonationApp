package com.example.blooddonationapp.ui.signup
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.blooddonationapp.MainActivity
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.FragmentSignupBinding
import com.example.blooddonationapp.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import java.util.UUID

class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()

    private val viewModel: SignupViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bloodGroupOptions = arrayOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, bloodGroupOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.bloodGroupSpinner.adapter = adapter

        binding.signupButton.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val bloodGroup = binding.bloodGroupSpinner.selectedItem.toString()
            val address = binding.addressEditText.text.toString()
            val phoneNumber = binding.phoneEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val isDonor = binding.donorToggleButton.isChecked


            val user = User(
                userId = "",
                name = name,
                email = email,
                password = password,
                bloodGroup = bloodGroup,
                phoneNumber = phoneNumber,
                address = address,
                donor = isDonor
            )

            if (validateInputs(email, password, bloodGroup)) {
                viewModel.signup(
                    user = user,
                    onSuccess = {
                        // Navigate to the next screen upon successful sign up
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    },
                    onError = { errorMessage ->
                        // Show error message for failed sign up
                        binding.errorTextView.visibility = View.VISIBLE
                        binding.errorTextView.text = errorMessage
                    }
                )
            }
        }

        binding.loginTextView.setOnClickListener {
            // Navigate to the login fragment
            findNavController().navigate(R.id.action_signupFragment2_to_loginFragment2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateInputs(email: String, password: String, bloodGroup: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email
            binding.emailEditText.error = "Enter a valid email address"
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            // Invalid password
            binding.passwordEditText.error = "Password must be at least 6 characters"
            return false
        }

        if (bloodGroup.isEmpty()) {
            // Invalid blood group selection
            return false
        }

        // All inputs are valid
        return true
    }
}
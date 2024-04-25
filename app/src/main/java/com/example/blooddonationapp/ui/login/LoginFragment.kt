package com.example.blooddonationapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.blooddonationapp.R
import com.example.blooddonationapp.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.blooddonationapp.MainActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            val email = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (validateInputs(email, password)) {
                viewModel.login(email, password, onSuccess =  {
                    // Navigate to the next screen upon successful login
                    Log.d("logging in", email)
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                    requireActivity().finish()
                }, onError =  { errorMessage ->
                    // Show error message for failed login
                    binding.errorTextView.visibility = View.VISIBLE
                    binding.errorTextView.text = errorMessage
                })
            }
        }

        binding.signupTextView.setOnClickListener {
            // Navigate to the sign-up fragment
            findNavController().navigate(R.id.action_loginFragment2_to_signupFragment2)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // Invalid email
            binding.usernameEditText.error = "Enter a valid email address"
            return false
        }

        if (password.isEmpty()) {
            // Invalid password
            binding.passwordEditText.error = "Password cannot be empty"
            return false
        }

        // All inputs are valid
        return true
    }


}


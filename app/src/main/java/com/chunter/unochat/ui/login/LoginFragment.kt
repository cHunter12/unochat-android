package com.chunter.unochat.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.chunter.unochat.R
import com.chunter.unochat.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private lateinit var binding: FragmentLoginBinding
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            findNavController().navigate(R.id.navToHome)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLoginBinding.bind(view)

        binding.registerButton.setOnClickListener { displayInputs(false) }
        binding.signInButton.setOnClickListener { displayInputs(true) }

        binding.googleSignInButton.setOnClickListener {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(requireContext(), options)
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            lifecycleScope.launch {
                try {
                    val account = GoogleSignIn.getSignedInAccountFromIntent(data).await()
                    firebaseAuthWithGoogle(account)
                } catch (throwable: Throwable) {
                    Timber.e(throwable)
                }
            }
        }
    }

    private fun displayInputs(isSignIn: Boolean) {
        binding.signIn.isVisible = false
        binding.inputs.isVisible = true

        binding.doneButton.text =
            if (isSignIn) getString(R.string.sign_in) else getString(R.string.register)
        binding.doneButton.setOnClickListener {
            val email = binding.emailInput.text?.toString() ?: return@setOnClickListener
            val password = binding.passwordInput.text?.toString() ?: return@setOnClickListener

            if (isSignIn) {
                firebaseAuthWithEmail(email, password)
            } else {
                firebaseRegisterWithEmail(email, password)
            }
        }

        binding.cancelButton.setOnClickListener {
            binding.inputs.isVisible = false
            binding.signIn.isVisible = true
        }
    }

    private fun firebaseAuthWithEmail(email: String, password: String) {
        toggleLoading(true)
        lifecycleScope.launch {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
                handleAuthResult(result.user)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                toggleLoading(false)
            }
        }
    }

    private fun firebaseRegisterWithEmail(email: String, password: String) {
        toggleLoading(true)
        lifecycleScope.launch {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
                handleAuthResult(result.user)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                toggleLoading(false)
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        toggleLoading(true)
        lifecycleScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = firebaseAuth.signInWithCredential(credential).await()
                handleAuthResult(result.user)
            } catch (throwable: Throwable) {
                Timber.e(throwable)
                toggleLoading(false)
            }
        }
    }

    private fun handleAuthResult(user: FirebaseUser?) {
        if (user != null) {
            Timber.d("Auth succeeded!")
            lifecycleScope.launch {
                val token = FirebaseInstanceId.getInstance().instanceId
                    .await()
                    .token

                Firebase.firestore.document("users/${user.uid}")
                    .update("token", token)
                    .await()

                withContext(Dispatchers.Main) {
                    findNavController().navigate(R.id.navToHome)
                }
            }
        } else {
            Timber.e("Auth failed!")
        }
        toggleLoading(false)
    }

    private fun toggleLoading(isLoading: Boolean) {
        with(binding) {
            inputs.isVisible = false
            signIn.isVisible = !isLoading
            loginLoading.isVisible = isLoading

            if (!isLoading) {
                emailInput.text = null
                passwordInput.text = null
            }
        }
    }

    companion object {

        private const val RC_SIGN_IN = 100
    }
}
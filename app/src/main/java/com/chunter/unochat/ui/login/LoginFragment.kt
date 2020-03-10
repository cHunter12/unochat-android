package com.chunter.unochat.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.chunter.unochat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

    private lateinit var register: Button
    private lateinit var signIn: Button
    private lateinit var loginLoading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        register = view.findViewById(R.id.register)
        register.setOnClickListener { displaySignInBottomSheet(false) }

        signIn = view.findViewById(R.id.signin)
        signIn.setOnClickListener { displaySignInBottomSheet(true) }

        loginLoading = view.findViewById(R.id.loginLoading)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_REGISTER || requestCode == RC_SIGN_IN) {
            toggleLoading(true)
            val email = data?.getStringExtra("name") ?: return
            val password = data.getStringExtra("password") ?: return

            if (requestCode == RC_SIGN_IN) {
                firebaseAuthWithEmail(email, password)
            } else {
                firebaseRegisterWithEmail(email, password)
            }
        }
    }

    private fun displaySignInBottomSheet(isSignIn: Boolean) {
        val signInSheet = SignInBottomSheet.getInstance(isSignIn)
        signInSheet.setTargetFragment(this, if (isSignIn) RC_SIGN_IN else RC_REGISTER)
        signInSheet.show(parentFragmentManager, null)
    }

    private fun firebaseAuthWithEmail(email: String, password: String) {
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
        register.isVisible = !isLoading
        signIn.isVisible = !isLoading
        loginLoading.isVisible = isLoading
    }

    companion object {

        const val RC_REGISTER = 101
        const val RC_SIGN_IN = 102
    }
}
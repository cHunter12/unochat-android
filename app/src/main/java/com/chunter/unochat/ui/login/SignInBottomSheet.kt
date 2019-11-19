package com.chunter.unochat.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import com.chunter.unochat.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_sign_in.*

class SignInBottomSheet private constructor() : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottom_sheet_sign_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.doneButton).apply {
            val isSignIn = requireArguments().getBoolean(SIGN_IN)

            text = if (isSignIn) getString(R.string.sign_in) else getString(R.string.register)
            setOnClickListener {
                val email = emailInput.text?.toString() ?: return@setOnClickListener
                val password = passwordInput.text?.toString() ?: return@setOnClickListener

                targetFragment?.onActivityResult(
                    targetRequestCode,
                    Activity.RESULT_OK,
                    Intent().apply {
                        putExtra(EMAIL, email)
                        putExtra(PASSWORD, password)
                    })
                dismiss()
            }
        }
    }

    companion object {

        const val EMAIL = "name"
        const val PASSWORD = "password"

        private const val SIGN_IN = "isSignIn"

        fun getInstance(isSignIn: Boolean): SignInBottomSheet {
            return SignInBottomSheet().apply {
                arguments = bundleOf(SIGN_IN to isSignIn)
            }
        }
    }
}
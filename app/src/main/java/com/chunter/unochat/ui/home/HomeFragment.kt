package com.chunter.unochat.ui.home

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chunter.unochat.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val userAdapter = UserAdapter(::onUserClicked)

    private val firestore = Firebase.firestore

    private lateinit var userList: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userList = view.findViewById(R.id.userList)
        userList.layoutManager = LinearLayoutManager(userList.context)
        userList.adapter = userAdapter

        loadUsers()
    }

    private fun loadUsers() {
        firestore.collection("users").addSnapshotListener { snapshot, error ->
            if (error != null) {
                Timber.e(error)
                if (error.code == FirebaseFirestoreException.Code.NOT_FOUND) {
                    loadUsers()
                } else {
                    return@addSnapshotListener
                }
            }

            if (snapshot == null) return@addSnapshotListener

            userAdapter.submitList(snapshot.toObjects(User::class.java)
                .filter { user -> user.name != FirebaseAuth.getInstance().currentUser?.email })
        }
    }

    private fun onUserClicked(user: User) {
        findNavController().navigate(HomeFragmentDirections.navToChat(user.id))
    }
}
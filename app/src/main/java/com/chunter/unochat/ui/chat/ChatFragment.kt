package com.chunter.unochat.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chunter.unochat.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.tasks.await
import java.util.*

class ChatFragment : Fragment(R.layout.fragment_chat) {

    private val firestore = Firebase.firestore

    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageList: RecyclerView

    private var roomId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Log user out, don't crash the app!
        val senderId = FirebaseAuth.getInstance().currentUser?.uid
            ?: throw IllegalStateException("User is not signed in!")

        chatAdapter = ChatAdapter(senderId)
        messageList = view.findViewById(R.id.messages)
        messageList.layoutManager = LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
        }
        messageList.adapter = chatAdapter

        val receiverId = ChatFragmentArgs.fromBundle(requireArguments()).userId
        lifecycleScope.launchWhenResumed {
            val rooms = firestore.collection("rooms")
            val room = rooms.get()
                .await()
                .documents
                .find { room -> (room["users"] as List<String>).containsAll(listOf(senderId, receiverId)) }

            if (room != null) {
                roomId = room.id
                observeMessages()
            } else {
                roomId = rooms.add(mapOf("users" to listOf(senderId, receiverId)))
                    .await()
                    .id
                observeMessages()
            }
        }

        enterMessageLayout.setEndIconOnClickListener {
            val message = enterMessage.text?.toString() ?: return@setEndIconOnClickListener

            firestore.collection("rooms/${roomId}/messages")
                .add(
                    mapOf(
                        "content" to message,
                        "senderId" to senderId,
                        "timestamp" to Timestamp(Date())
                    )
                )

            enterMessage.text?.clear()
        }
    }

    private fun observeMessages() {
        firestore.collection("rooms/${roomId}/messages")
            .addSnapshotListener { snapshot, exception ->
                if (snapshot == null || exception != null) return@addSnapshotListener

                val messages = snapshot.toObjects(Message::class.java)
                messages.sortByDescending { message -> message.timestamp }
                chatAdapter.submitList(messages) { messageList.scrollToPosition(0) }
            }
    }
}
package com.chunter.unochat.ui.chat

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Message(
    @DocumentId
    val id: String = "",
    val content: String = "",
    val senderId: String = "",
    val timestamp: Date = Date()
)
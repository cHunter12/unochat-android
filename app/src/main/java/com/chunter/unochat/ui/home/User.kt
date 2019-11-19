package com.chunter.unochat.ui.home

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.PropertyName
import com.google.gson.annotations.SerializedName

data class User(
    @DocumentId
    val id: String = "",
    val name: String = ""
)
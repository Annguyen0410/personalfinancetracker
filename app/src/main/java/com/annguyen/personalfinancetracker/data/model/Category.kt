package com.annguyen.personalfinancetracker.data.model

import com.google.firebase.firestore.DocumentId

data class Category(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val icon: String = "",
    val color: String = ""
)


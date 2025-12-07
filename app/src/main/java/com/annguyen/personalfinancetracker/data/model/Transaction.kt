package com.annguyen.personalfinancetracker.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Transaction(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: String = "",
    val categoryName: String = "",
    val description: String = "",
    val date: Timestamp = Timestamp.now(),
    val createdAt: Timestamp = Timestamp.now()
)

enum class TransactionType {
    INCOME,
    EXPENSE
}


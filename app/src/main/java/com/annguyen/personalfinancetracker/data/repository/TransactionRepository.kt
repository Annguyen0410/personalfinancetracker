package com.annguyen.personalfinancetracker.data.repository

import com.annguyen.personalfinancetracker.data.model.Transaction
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    fun getTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listenerRegistration = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getTransactionsByCategory(userId: String, categoryId: String): Flow<List<Transaction>> = callbackFlow {
        val listenerRegistration = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>> = callbackFlow {
        val listenerRegistration = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type.name)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            val docRef = firestore.collection("transactions").add(transaction).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            firestore.collection("transactions")
                .document(transaction.id)
                .set(transaction)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteTransaction(transactionId: String): Result<Unit> {
        return try {
            firestore.collection("transactions")
                .document(transactionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getTransaction(transactionId: String): Result<Transaction> {
        return try {
            val doc = firestore.collection("transactions")
                .document(transactionId)
                .get()
                .await()
            val transaction = doc.toObject(Transaction::class.java)?.copy(id = doc.id)
            if (transaction != null) {
                Result.success(transaction)
            } else {
                Result.failure(Exception("Transaction not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


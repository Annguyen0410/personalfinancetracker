package com.annguyen.personalfinancetracker.data.repository

import android.util.Log
import com.annguyen.personalfinancetracker.data.model.Transaction
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
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
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log error for debugging
                    Log.e("TransactionRepository", "Error loading transactions: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Sort by date descending in memory (avoids Firestore index requirement)
                val sortedTransactions = transactions.sortedByDescending { it.date.seconds }
                trySend(sortedTransactions)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getTransactionsByCategory(userId: String, categoryId: String): Flow<List<Transaction>> = callbackFlow {
        val listenerRegistration = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("categoryId", categoryId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TransactionRepository", "Error loading transactions by category: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Sort by date descending in memory
                val sortedTransactions = transactions.sortedByDescending { it.date.seconds }
                trySend(sortedTransactions)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getTransactionsByType(userId: String, type: TransactionType): Flow<List<Transaction>> = callbackFlow {
        val listenerRegistration = firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("TransactionRepository", "Error loading transactions by type: ${error.message}", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                // Sort by date descending in memory
                val sortedTransactions = transactions.sortedByDescending { it.date.seconds }
                trySend(sortedTransactions)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    suspend fun addTransaction(transaction: Transaction): Result<String> {
        return try {
            Log.d("TransactionRepository", "Adding transaction: userId=${transaction.userId}, amount=${transaction.amount}, type=${transaction.type}")
            val docRef = firestore.collection("transactions").add(transaction).await()
            Log.d("TransactionRepository", "Transaction added successfully with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error adding transaction: ${e.message}", e)
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


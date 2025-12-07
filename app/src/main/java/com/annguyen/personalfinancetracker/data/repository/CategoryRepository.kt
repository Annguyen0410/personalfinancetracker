package com.annguyen.personalfinancetracker.data.repository

import com.annguyen.personalfinancetracker.data.model.Category
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    
    fun getCategories(userId: String): Flow<List<Category>> = callbackFlow {
        val listenerRegistration = firestore.collection("categories")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    fun getCategoriesByType(userId: String, type: TransactionType): Flow<List<Category>> = callbackFlow {
        val listenerRegistration = firestore.collection("categories")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Category::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listenerRegistration.remove() }
    }
    
    suspend fun addCategory(category: Category): Result<String> {
        return try {
            val docRef = firestore.collection("categories").add(category).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            firestore.collection("categories")
                .document(category.id)
                .set(category)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        return try {
            firestore.collection("categories")
                .document(categoryId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getCategory(categoryId: String): Result<Category> {
        return try {
            val doc = firestore.collection("categories")
                .document(categoryId)
                .get()
                .await()
            val category = doc.toObject(Category::class.java)?.copy(id = doc.id)
            if (category != null) {
                Result.success(category)
            } else {
                Result.failure(Exception("Category not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}


package com.annguyen.personalfinancetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annguyen.personalfinancetracker.data.model.Transaction
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.annguyen.personalfinancetracker.data.repository.TransactionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Date

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val filteredTransactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val selectedType: TransactionType? = null
)

class TransactionViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository(),
    private val userId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()
    
    init {
        loadTransactions()
    }
    
    private fun loadTransactions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transactionRepository.getTransactions(userId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load transactions"
                    )
                }
                .collect { transactions ->
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        filteredTransactions = transactions, // Initialize filtered with all transactions
                        isLoading = false,
                        errorMessage = null
                    )
                    applyFilters() // Apply any active filters
                }
        }
    }
    
    fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: String,
        categoryName: String,
        description: String,
        date: Date,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (amount <= 0) {
            onError("Amount must be greater than 0")
            return
        }
        
        if (categoryId.isBlank()) {
            onError("Please select a category")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val transaction = Transaction(
                userId = userId,
                amount = amount,
                type = type,
                categoryId = categoryId,
                categoryName = categoryName,
                description = description,
                date = Timestamp(date),
                createdAt = Timestamp.now()
            )
            
            val result = transactionRepository.addTransaction(transaction)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to add transaction"
                    )
                    onError(exception.message ?: "Failed to add transaction")
                }
            )
        }
    }
    
    fun updateTransaction(
        transaction: Transaction,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (transaction.amount <= 0) {
            onError("Amount must be greater than 0")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = transactionRepository.updateTransaction(transaction)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update transaction"
                    )
                    onError(exception.message ?: "Failed to update transaction")
                }
            )
        }
    }
    
    fun deleteTransaction(
        transactionId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = transactionRepository.deleteTransaction(transactionId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to delete transaction"
                    )
                    onError(exception.message ?: "Failed to delete transaction")
                }
            )
        }
    }
    
    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }
    
    fun setSelectedType(type: TransactionType?) {
        _uiState.value = _uiState.value.copy(selectedType = type)
        applyFilters()
    }
    
    private fun applyFilters() {
        var filtered = _uiState.value.transactions
        
        // Filter by type
        _uiState.value.selectedType?.let { type ->
            filtered = filtered.filter { it.type == type }
        }
        
        // Filter by search query
        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery.lowercase()
            filtered = filtered.filter {
                it.description.lowercase().contains(query) ||
                it.categoryName.lowercase().contains(query)
            }
        }
        
        _uiState.value = _uiState.value.copy(filteredTransactions = filtered)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}


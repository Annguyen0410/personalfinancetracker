package com.annguyen.personalfinancetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annguyen.personalfinancetracker.data.model.Transaction
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.annguyen.personalfinancetracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: Map<String, Double> = emptyMap()
)

class HomeViewModel(
    private val transactionRepository: TransactionRepository = TransactionRepository(),
    private val userId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
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
                    calculateSummary(transactions)
                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        recentTransactions = transactions.take(5),
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    private fun calculateSummary(transactions: List<Transaction>) {
        val totalIncome = transactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        val totalExpense = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val balance = totalIncome - totalExpense
        
        val categoryBreakdown = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryName }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
        
        _uiState.value = _uiState.value.copy(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            balance = balance,
            categoryBreakdown = categoryBreakdown
        )
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}


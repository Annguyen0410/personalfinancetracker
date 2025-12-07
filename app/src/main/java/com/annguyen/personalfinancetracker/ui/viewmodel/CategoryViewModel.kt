package com.annguyen.personalfinancetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.annguyen.personalfinancetracker.data.model.Category
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.annguyen.personalfinancetracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CategoryViewModel(
    private val categoryRepository: CategoryRepository = CategoryRepository(),
    private val userId: String
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()
    
    init {
        loadCategories()
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            categoryRepository.getCategories(userId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load categories"
                    )
                }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    fun getCategoriesByType(type: TransactionType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            categoryRepository.getCategoriesByType(userId, type)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load categories"
                    )
                }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                        errorMessage = null
                    )
                }
        }
    }
    
    fun addCategory(
        name: String,
        type: TransactionType,
        icon: String = "",
        color: String = "",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (name.isBlank()) {
            onError("Category name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val category = Category(
                userId = userId,
                name = name,
                type = type,
                icon = icon,
                color = color
            )
            
            val result = categoryRepository.addCategory(category)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to add category"
                    )
                    onError(exception.message ?: "Failed to add category")
                }
            )
        }
    }
    
    fun updateCategory(
        category: Category,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (category.name.isBlank()) {
            onError("Category name cannot be empty")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = categoryRepository.updateCategory(category)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update category"
                    )
                    onError(exception.message ?: "Failed to update category")
                }
            )
        }
    }
    
    fun deleteCategory(
        categoryId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val result = categoryRepository.deleteCategory(categoryId)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    onSuccess()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to delete category"
                    )
                    onError(exception.message ?: "Failed to delete category")
                }
            )
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}


package com.annguyen.personalfinancetracker.ui.screen.transaction

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.annguyen.personalfinancetracker.data.model.Category
import com.annguyen.personalfinancetracker.data.model.TransactionType
import com.annguyen.personalfinancetracker.ui.viewmodel.CategoryViewModel
import com.annguyen.personalfinancetracker.ui.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    userId: String,
    transactionViewModel: TransactionViewModel = viewModel { TransactionViewModel(userId = userId) },
    categoryViewModel: CategoryViewModel = viewModel { CategoryViewModel(userId = userId) },
    onNavigateBack: () -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val categoryUiState by categoryViewModel.uiState.collectAsState()
    val transactionUiState by transactionViewModel.uiState.collectAsState()
    
    LaunchedEffect(selectedType) {
        categoryViewModel.getCategoriesByType(selectedType)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Transaction Type
            Text("Transaction Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedType == TransactionType.INCOME,
                    onClick = { selectedType = TransactionType.INCOME },
                    label = { Text("Income") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = selectedType == TransactionType.EXPENSE,
                    onClick = { selectedType = TransactionType.EXPENSE },
                    label = { Text("Expense") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { 
                    if (it.isEmpty() || it.matches(Regex("^\\d+(\\.\\d{0,2})?$"))) {
                        amount = it
                    }
                },
                label = { Text("Amount") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                prefix = { Text("$") }
            )
            
            // Category
            Text("Category", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = selectedCategory?.name ?: "",
                onValueChange = {},
                label = { Text("Select Category") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategoryDialog = true },
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showCategoryDialog = true }) {
                        Text("Select")
                    }
                }
            )
            
            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
            
            // Date
            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate),
                onValueChange = {},
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }) {
                        Text("Select")
                    }
                }
            )
            
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            Button(
                onClick = {
                    if (amount.isBlank() || amount.toDoubleOrNull() == null) {
                        errorMessage = "Please enter a valid amount"
                        return@Button
                    }
                    if (selectedCategory == null) {
                        errorMessage = "Please select a category"
                        return@Button
                    }
                    
                    transactionViewModel.addTransaction(
                        amount = amount.toDouble(),
                        type = selectedType,
                        categoryId = selectedCategory!!.id,
                        categoryName = selectedCategory!!.name,
                        description = description,
                        date = selectedDate,
                        onSuccess = {
                            onNavigateBack()
                        },
                        onError = {
                            errorMessage = it
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !transactionUiState.isLoading
            ) {
                if (transactionUiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Transaction")
                }
            }
        }
    }
    
    // Category Selection Dialog
    if (showCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showCategoryDialog = false },
            title = { Text("Select Category") },
            text = {
                if (categoryUiState.categories.isEmpty()) {
                    Text("No categories available. Please create a category first.")
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 400.dp)
                    ) {
                        items(categoryUiState.categories) { category ->
                            ListItem(
                                headlineContent = { Text(category.name) },
                                modifier = Modifier.clickable {
                                    selectedCategory = category
                                    showCategoryDialog = false
                                }
                            )
                            Divider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Date Picker - Simplified version
    if (showDatePicker) {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        var year by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
        var month by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
        var day by remember { mutableStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
        
        AlertDialog(
            onDismissRequest = { showDatePicker = false },
            title = { Text("Select Date") },
            text = {
                Column {
                    // Simple date input - in a real app, you'd use a proper date picker library
                    Text("Year: $year, Month: ${month + 1}, Day: $day")
                    Text("Note: For production, use a proper date picker library")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        calendar.set(year, month, day)
                        selectedDate = calendar.time
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}


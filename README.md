# Personal Finance Tracker

A comprehensive Android application for tracking personal finances with income and expense management, built with Jetpack Compose and Firebase.

## Features

### Authentication
- Email/Password sign up and login
- Persistent login (user stays logged in after app restart)
- Password validation (minimum 6 characters)
- Email format validation
- Loading states during authentication
- Error handling with user-friendly messages

### Data Persistence (Firebase Firestore)
- Full CRUD operations on Transactions and Categories
- User-specific data isolation
- Real-time data synchronization
- Query operations (filtering, sorting, limiting data)

### UI Screens (8 Screens)
1. **Login Screen** - Email/password authentication
2. **Sign Up Screen** - New user registration with validation
3. **Home/Dashboard Screen** - Overview of balance, recent transactions, income/expense summary
4. **Transactions List Screen** - All transactions with filter/sort options
5. **Add Transaction Screen** - Form to add income/expense
6. **Edit Transaction Screen** - Modify existing transaction
7. **Categories Management Screen** - View/add/edit/delete categories
8. **Profile/Settings Screen** - User info, sign out

### Additional Features
- Search and filter transactions
- Category-based transaction organization
- Real-time balance calculation
- Material Design 3 components
- Form validation with real-time feedback
- Loading states (progress indicators)
- Error states with proper handling
- Empty states with helpful messaging
- Confirmation dialogs for destructive actions

## Architecture

The app follows **MVVM (Model-View-ViewModel)** architecture pattern:

- **Data Layer**: Repository pattern with Firebase Firestore and Firebase Authentication
- **Domain Layer**: Business logic and data models
- **UI Layer**: Jetpack Compose screens with ViewModels
- **State Management**: StateFlow for reactive UI updates
- **Coroutines**: Proper scope usage for async operations

## Project Structure

```
app/src/main/java/com/annguyen/personalfinancetracker/
├── data/
│   ├── model/
│   │   ├── Transaction.kt
│   │   └── Category.kt
│   └── repository/
│       ├── AuthRepository.kt
│       ├── TransactionRepository.kt
│       └── CategoryRepository.kt
├── ui/
│   ├── navigation/
│   │   └── NavGraph.kt
│   ├── screen/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── SignUpScreen.kt
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── transaction/
│   │   │   ├── TransactionsListScreen.kt
│   │   │   ├── AddTransactionScreen.kt
│   │   │   └── EditTransactionScreen.kt
│   │   ├── category/
│   │   │   └── CategoriesScreen.kt
│   │   └── profile/
│   │       └── ProfileScreen.kt
│   └── viewmodel/
│       ├── AuthViewModel.kt
│       ├── TransactionViewModel.kt
│       ├── CategoryViewModel.kt
│       └── HomeViewModel.kt
└── MainActivity.kt
```

## Setup Guide

### Prerequisites
- Android Studio Hedgehog or later
- JDK 11 or higher
- Firebase account

### Step 1: Clone the Repository
```bash
git clone <repository-url>
cd personalfinancetracker
```

### Step 2: Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select an existing one
3. Add an Android app to your project:
   - Package name: `com.annguyen.personalfinancetracker`
   - Download `google-services.json`
4. Place `google-services.json` in `app/` directory
5. Enable Authentication:
   - Go to Authentication > Sign-in method
   - Enable Email/Password provider
6. Enable Firestore:
   - Go to Firestore Database
   - Create database in test mode (for development)
   - Set up security rules (see below)

### Step 3: Firestore Security Rules

Set up the following security rules in Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Transactions collection
    match /transactions/{transactionId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
    
    // Categories collection
    match /categories/{categoryId} {
      allow read, write: if request.auth != null && request.auth.uid == resource.data.userId;
      allow create: if request.auth != null && request.auth.uid == request.resource.data.userId;
    }
  }
}
```

### Step 4: Build and Run

1. Open the project in Android Studio
2. Sync Gradle files
3. Build the project (Build > Make Project)
4. Run on an emulator or physical device (API 24+)

## Dependencies

- **Jetpack Compose**: Modern UI toolkit
- **Firebase Authentication**: Email/password authentication
- **Firebase Firestore**: Cloud database
- **Navigation Compose**: Navigation between screens
- **ViewModel**: UI-related data holder
- **Coroutines**: Asynchronous programming
- **Material Design 3**: Material Design components

## Usage

1. **Sign Up**: Create a new account with email and password
2. **Sign In**: Login with your credentials
3. **Add Categories**: Go to Categories screen and add income/expense categories
4. **Add Transactions**: Use the + button to add income or expense transactions
5. **View Transactions**: Browse all transactions with search and filter options
6. **Edit/Delete**: Tap on a transaction to edit or delete it
7. **View Dashboard**: Home screen shows balance, income, expense, and recent transactions
8. **Sign Out**: Go to Profile screen to sign out

## Code Quality

- Clean architecture with separation of concerns
- MVVM pattern implementation
- Repository pattern for data abstraction
- Proper error handling
- Loading and empty states
- Form validation
- Material Design 3 theming
- No critical issues

## Team Members

An Nguyen - Email: AnNguyen0410@csu.fullerton.edu - CWID: 885598904
Liam Dwane - Email: lpdwane@csu.fullerton.edu - CWID: 888470812
Tri Bui - Email: triminhbui@csu.fullerton.edu - CWID: 885242487
Kendrik Deleoz - Email: kendrikdeleoz@csu.fullerton.edu - CWID: 886859461

## Demo Video

[Final CPSC 411A Project](https://youtu.be/rPfpgsRRnuo?si=hZWHsb8MKtJ65tRn) 

## Notes

- The app uses Firebase Firestore for real-time data synchronization
- All data is user-specific and isolated by userId
- Persistent login is handled automatically by Firebase Auth
- Date picker uses a simplified implementation (for production, consider using a proper date picker library)


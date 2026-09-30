package com.openclassrooms.hexagonal.games.screen.account

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * UI state of the account management screen.
 *
 * @property isSignedOut true once the user has been signed out (or their account deleted),
 * meaning the screen should be left.
 * @property hasError true when the last action failed and the generic error must be shown once.
 */
data class AccountUiState(
  val isSignedOut: Boolean = false,
  val hasError: Boolean = false,
)

/**
 * ViewModel handling the account management actions: sign out and account deletion.
 */
@HiltViewModel
class AccountViewModel @Inject constructor(private val firebaseAuth: FirebaseAuth) : ViewModel() {
  
  private val _uiState = MutableStateFlow(AccountUiState())
  
  /**
   * Observable state of the screen.
   */
  val uiState: StateFlow<AccountUiState>
    get() = _uiState
  
  /**
   * Signs the current user out.
   */
  fun signOut() {
    firebaseAuth.signOut()
    _uiState.update { it.copy(isSignedOut = true) }
  }
  
  /**
   * Deletes the current user's account. Firebase signs the user out locally once the deletion
   * succeeds. If it fails (network error, recent sign-in required...), the generic error is raised.
   */
  fun deleteAccount() {
    val user = firebaseAuth.currentUser
    if (user == null) {
      _uiState.update { it.copy(isSignedOut = true) }
      return
    }
    user.delete()
      .addOnSuccessListener { _uiState.update { it.copy(isSignedOut = true) } }
      .addOnFailureListener { _uiState.update { it.copy(hasError = true) } }
  }
  
  /**
   * To be called once the error has been displayed, so it is not shown again.
   */
  fun onErrorShown() {
    _uiState.update { it.copy(hasError = false) }
  }
}

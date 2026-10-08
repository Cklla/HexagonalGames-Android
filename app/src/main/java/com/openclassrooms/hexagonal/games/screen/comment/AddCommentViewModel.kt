package com.openclassrooms.hexagonal.games.screen.comment

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.userFromDisplayName
import com.openclassrooms.hexagonal.games.screen.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI state of the AddCommentScreen.
 *
 * @property content the text typed by the user.
 * @property isSaving true while the comment is being written, to prevent a double submission.
 * @property isSaved true once the comment has been saved, meaning the screen should be left.
 * @property messageRes resource ID of a message (toast) to show once, or null.
 */
data class AddCommentUiState(
  val content: String = "",
  val isSaving: Boolean = false,
  val isSaved: Boolean = false,
  @StringRes val messageRes: Int? = null,
) {
  /**
   * A comment is valid as soon as it contains something other than whitespace.
   */
  val isContentValid: Boolean
    get() = content.isNotBlank()
}

/**
 * ViewModel of the "add a comment" screen. The post being commented is identified by the navigation
 * argument [Screen.AddComment.ARG_POST_ID]. The comment is authored by the signed-in user, and the
 * offline and error cases raise a message (toast).
 */
@HiltViewModel
class AddCommentViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val commentRepository: CommentRepository,
  private val firebaseAuth: FirebaseAuth,
  private val networkChecker: NetworkChecker,
) : ViewModel() {

  private val postId: String = checkNotNull(savedStateHandle[Screen.AddComment.ARG_POST_ID])

  private val _uiState = MutableStateFlow(AddCommentUiState())

  /**
   * Observable state of the form and of the save operation.
   */
  val uiState: StateFlow<AddCommentUiState>
    get() = _uiState

  /**
   * To be called each time the user edits the comment.
   */
  fun onContentChanged(content: String) {
    _uiState.update { it.copy(content = content) }
  }

  /**
   * Saves the comment, authored by the signed-in user. Does nothing if the comment is empty or a
   * save is already running. Raises a message when nobody is signed in, when the device is offline,
   * or when the write fails.
   */
  fun addComment() {
    val state = _uiState.value
    if (state.isSaving || !state.isContentValid) return

    val user = firebaseAuth.currentUser
    if (user == null) {
      showMessage(R.string.error_generic)
      return
    }
    if (!networkChecker.isOnline()) {
      showMessage(R.string.error_no_network)
      return
    }

    _uiState.update { it.copy(isSaving = true) }
    viewModelScope.launch {
      try {
        commentRepository.addComment(
          postId,
          Comment(
            id = UUID.randomUUID().toString(),
            content = state.content.trim(),
            timestamp = System.currentTimeMillis(),
            author = userFromDisplayName(user.uid, user.displayName)
          )
        )
        _uiState.update { it.copy(isSaving = false, isSaved = true) }
      } catch (e: CancellationException) {
        throw e
      } catch (e: Exception) {
        _uiState.update { it.copy(isSaving = false, messageRes = R.string.error_generic) }
      }
    }
  }

  /**
   * To be called once the message has been displayed, so it is not shown again.
   */
  fun onMessageShown() {
    _uiState.update { it.copy(messageRes = null) }
  }

  private fun showMessage(@StringRes messageRes: Int) {
    _uiState.update { it.copy(messageRes = messageRes) }
  }

}

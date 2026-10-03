package com.openclassrooms.hexagonal.games.screen.ad

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.userFromDisplayName
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * UI state of the save operation of the AddScreen.
 *
 * @property isSaving true while the post is being written, to prevent a double submission.
 * @property isSaved true once the post has been saved, meaning the screen should be left.
 * @property messageRes resource ID of a message (toast) to show once, or null.
 */
data class AddUiState(
  val isSaving: Boolean = false,
  val isSaved: Boolean = false,
  @StringRes val messageRes: Int? = null,
)

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data,
 * the FirebaseAuth instance to find the author, and a NetworkChecker to detect the offline case.
 */
@HiltViewModel
class AddViewModel @Inject constructor(
  private val postRepository: PostRepository,
  private val firebaseAuth: FirebaseAuth,
  private val networkChecker: NetworkChecker,
) : ViewModel() {
  
  /**
   * Internal mutable state flow representing the current post being edited.
   */
  private var _post = MutableStateFlow(
    Post(
      id = UUID.randomUUID().toString(),
      title = "",
      description = "",
      photoUrl = null,
      timestamp = System.currentTimeMillis(),
      author = null
    )
  )
  
  /**
   * Public state flow representing the current post being edited.
   * This is immutable for consumers.
   */
  val post: StateFlow<Post>
    get() = _post
  
  /**
   * StateFlow derived from the post that emits the first FormError found (title, then description),
   * null if the form is valid.
   */
  val error = post.map {
    verifyPost()
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5_000),
    initialValue = null,
  )
  
  private val _uiState = MutableStateFlow(AddUiState())
  
  /**
   * Observable state of the save operation.
   */
  val uiState: StateFlow<AddUiState>
    get() = _uiState
  
  /**
   * Handles form events like title and description changes.
   *
   * @param formEvent The form event to be processed.
   */
  fun onAction(formEvent: FormEvent) {
    when (formEvent) {
      is FormEvent.DescriptionChanged -> {
        _post.value = _post.value.copy(
          description = formEvent.description
        )
      }
      
      is FormEvent.TitleChanged -> {
        _post.value = _post.value.copy(
          title = formEvent.title
        )
      }
    }
  }
  
  /**
   * Saves the current post, authored by the signed-in user. Does nothing if the form is invalid
   * or a save is already running. Raises a message when the device is offline, when nobody is
   * signed in, or when the write fails.
   */
  fun addPost() {
    if (_uiState.value.isSaving || verifyPost() != null) return
    
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
        postRepository.addPost(
          _post.value.copy(
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
  
  /**
   * Verifies mandatory fields of the post
   * and returns a corresponding FormError if so.
   *
   * @return A FormError.TitleError if the title is blank, a FormError.DescriptionError if the
   * description is blank, null otherwise.
   */
  private fun verifyPost(): FormError? {
    val post = _post.value
    return when {
      post.title.isBlank() -> FormError.TitleError
      post.description.isNullOrBlank() -> FormError.DescriptionError
      else -> null
    }
  }
  
}

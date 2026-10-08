package com.openclassrooms.hexagonal.games.screen.detail

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.screen.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel of the post detail screen. The post to display is identified by the navigation argument
 * [Screen.PostDetail.ARG_POST_ID]. It exposes the post and its comments (oldest first), both kept up
 * to date in real time by Firestore snapshot listeners, raises one-shot messages (toasts) for the
 * offline and error cases, and guards the "add a comment" action.
 */
@HiltViewModel
class PostDetailViewModel @Inject constructor(
  savedStateHandle: SavedStateHandle,
  private val postRepository: PostRepository,
  private val commentRepository: CommentRepository,
  private val firebaseAuth: FirebaseAuth,
  private val networkChecker: NetworkChecker,
) : ViewModel() {

  private val postId: String = checkNotNull(savedStateHandle[Screen.PostDetail.ARG_POST_ID])

  private val _post = MutableStateFlow<Post?>(null)

  /**
   * The post being displayed, or null while it is loading (or if it does not exist).
   */
  val post: StateFlow<Post?>
    get() = _post

  private val _comments = MutableStateFlow<List<Comment>>(emptyList())

  /**
   * The comments of the post, oldest first.
   */
  val comments: StateFlow<List<Comment>>
    get() = _comments

  private val _messageRes = MutableStateFlow<Int?>(null)

  /**
   * Resource ID of a message (toast) to show once, or null. Call [onMessageShown] once displayed.
   */
  val messageRes: StateFlow<Int?>
    get() = _messageRes

  private var hasReceivedPost = false

  init {
    viewModelScope.launch {
      postRepository.getPost(postId)
        .catch { showErrorMessage() }
        .collect { onPostReceived(it) }
    }
    viewModelScope.launch {
      commentRepository.getComments(postId)
        .catch { showErrorMessage() }
        .collect { _comments.value = it }
    }
  }

  /**
   * To be called when the user taps the "add a comment" button.
   *
   * @return true if the user is signed in and may open the add screen; otherwise raises the
   * "an account is mandatory" message and returns false.
   */
  fun onAddCommentClick(): Boolean {
    if (firebaseAuth.currentUser != null) return true
    showMessage(R.string.error_account_required_comment)
    return false
  }

  /**
   * To be called once the message has been displayed, so it is not shown again.
   */
  fun onMessageShown() {
    _messageRes.update { null }
  }

  /**
   * Publishes the post. A missing post (deleted, or not available offline) raises an error message,
   * and so does the first load while offline (Firestore then serves its local cache).
   */
  private fun onPostReceived(post: Post?) {
    val isFirstLoad = !hasReceivedPost
    hasReceivedPost = true
    _post.value = post

    when {
      post == null -> showErrorMessage()
      isFirstLoad && !networkChecker.isOnline() -> showMessage(R.string.error_no_network)
    }
  }

  /**
   * "No network" when offline, the generic error otherwise.
   */
  private fun showErrorMessage() {
    showMessage(if (networkChecker.isOnline()) R.string.error_generic else R.string.error_no_network)
  }

  private fun showMessage(@StringRes messageRes: Int) {
    _messageRes.value = messageRes
  }

}

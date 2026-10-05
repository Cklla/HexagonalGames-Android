package com.openclassrooms.hexagonal.games.screen.homefeed

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing data and events related to the Homefeed.
 * This ViewModel retrieves posts from the PostRepository and exposes them as a StateFlow<List<Post>>,
 * allowing UI components to observe and react to changes in the posts data (in real time, since the
 * repository flow is backed by a Firestore snapshot listener). It also raises one-shot messages
 * (toasts) for the empty, offline and error cases, and guards the "add a post" action.
 */
@HiltViewModel
class HomefeedViewModel @Inject constructor(
  private val postRepository: PostRepository,
  private val firebaseAuth: FirebaseAuth,
  private val networkChecker: NetworkChecker,
) : ViewModel() {

  private val _posts: MutableStateFlow<List<Post>> = MutableStateFlow(emptyList())

  /**
   * Returns a Flow observable containing the list of posts fetched from the repository.
   *
   * @return A Flow<List<Post>> object that can be observed for changes.
   */
  val posts: StateFlow<List<Post>>
    get() = _posts

  private val _messageRes = MutableStateFlow<Int?>(null)

  /**
   * Resource ID of a message (toast) to show once, or null. Call [onMessageShown] once displayed.
   */
  val messageRes: StateFlow<Int?>
    get() = _messageRes

  private var hasReceivedPosts = false

  init {
    viewModelScope.launch {
      postRepository.posts
        .catch {
          // The listener has been closed by Firestore (e.g. permission denied): nothing more to collect.
          showMessage(if (networkChecker.isOnline()) R.string.error_generic else R.string.error_no_network)
        }
        .collect { onPostsReceived(it) }
    }
  }

  /**
   * To be called when the user taps the "add a post" button.
   *
   * @return true if the user is signed in and may open the add screen; otherwise raises the
   * "an account is mandatory" message and returns false.
   */
  fun onAddPostClick(): Boolean {
    if (firebaseAuth.currentUser != null) return true
    showMessage(R.string.error_account_required_post)
    return false
  }

  /**
   * To be called once the message has been displayed, so it is not shown again.
   */
  fun onMessageShown() {
    _messageRes.update { null }
  }

  /**
   * Publishes the new list and raises the matching message: "No network" for an empty list or on
   * the first load while offline (Firestore then serves its local cache), "No posts" for an empty
   * list while online.
   */
  private fun onPostsReceived(posts: List<Post>) {
    val isFirstLoad = !hasReceivedPosts
    hasReceivedPosts = true
    _posts.value = posts

    val isOffline = !networkChecker.isOnline()
    when {
      isOffline && (posts.isEmpty() || isFirstLoad) -> showMessage(R.string.error_no_network)
      posts.isEmpty() -> showMessage(R.string.no_posts)
    }
  }

  private fun showMessage(@StringRes messageRes: Int) {
    _messageRes.value = messageRes
  }

}

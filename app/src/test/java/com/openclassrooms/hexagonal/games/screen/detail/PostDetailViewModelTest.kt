package com.openclassrooms.hexagonal.games.screen.detail

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.screen.Screen
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostDetailViewModelTest {

  private lateinit var postRepository: PostRepository
  private lateinit var commentRepository: CommentRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var networkChecker: NetworkChecker
  private val postFlow = MutableSharedFlow<Post?>(replay = 1)
  private val commentsFlow = MutableSharedFlow<List<Comment>>(replay = 1)

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    postRepository = mockk()
    commentRepository = mockk()
    firebaseAuth = mockk()
    networkChecker = mockk()
    every { postRepository.getPost(POST_ID) } returns postFlow
    every { commentRepository.getComments(POST_ID) } returns commentsFlow
    every { networkChecker.isOnline() } returns true
    every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel() = PostDetailViewModel(
    SavedStateHandle(mapOf(Screen.PostDetail.ARG_POST_ID to POST_ID)),
    postRepository,
    commentRepository,
    firebaseAuth,
    networkChecker
  )

  private fun post() = Post(
    id = POST_ID,
    title = "title",
    description = "description",
    photoUrl = null,
    timestamp = 1,
    author = null
  )

  private fun comment(id: String, timestamp: Long) = Comment(
    id = id,
    content = "comment $id",
    timestamp = timestamp,
    author = null
  )

  @Test
  fun `the post is exposed as emitted by the repository`() {
    val viewModel = createViewModel()

    postFlow.tryEmit(post())

    assertEquals(post(), viewModel.post.value)
    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `the post is updated in real time on each emission`() {
    val viewModel = createViewModel()
    postFlow.tryEmit(post())

    postFlow.tryEmit(post().copy(title = "new title"))

    assertEquals("new title", viewModel.post.value?.title)
  }

  @Test
  fun `comments are exposed in the order given by the repository`() {
    val viewModel = createViewModel()

    commentsFlow.tryEmit(listOf(comment("1", 1), comment("2", 2)))

    assertEquals(listOf(comment("1", 1), comment("2", 2)), viewModel.comments.value)
  }

  @Test
  fun `comments are updated in real time on each emission`() {
    val viewModel = createViewModel()
    commentsFlow.tryEmit(listOf(comment("1", 1)))

    commentsFlow.tryEmit(listOf(comment("1", 1), comment("2", 2)))

    assertEquals(2, viewModel.comments.value.size)
  }

  @Test
  fun `no comment is not an error`() {
    val viewModel = createViewModel()

    commentsFlow.tryEmit(emptyList())

    assertTrue(viewModel.comments.value.isEmpty())
    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `the first load offline raises the no network message`() {
    every { networkChecker.isOnline() } returns false
    val viewModel = createViewModel()

    postFlow.tryEmit(post())

    assertEquals(post(), viewModel.post.value)
    assertEquals(R.string.error_no_network, viewModel.messageRes.value)
  }

  @Test
  fun `a later emission offline raises no message`() {
    val viewModel = createViewModel()
    postFlow.tryEmit(post())
    every { networkChecker.isOnline() } returns false

    postFlow.tryEmit(post().copy(title = "new title"))

    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `a missing post online raises the generic message`() {
    val viewModel = createViewModel()

    postFlow.tryEmit(null)

    assertNull(viewModel.post.value)
    assertEquals(R.string.error_generic, viewModel.messageRes.value)
  }

  @Test
  fun `a missing post offline raises the no network message`() {
    every { networkChecker.isOnline() } returns false
    val viewModel = createViewModel()

    postFlow.tryEmit(null)

    assertEquals(R.string.error_no_network, viewModel.messageRes.value)
  }

  @Test
  fun `a post flow error online raises the generic message instead of crashing`() {
    every { postRepository.getPost(POST_ID) } returns flow { throw IllegalStateException("denied") }

    val viewModel = createViewModel()

    assertEquals(R.string.error_generic, viewModel.messageRes.value)
    assertNull(viewModel.post.value)
  }

  @Test
  fun `a comments flow error offline raises the no network message`() {
    every { networkChecker.isOnline() } returns false
    every { commentRepository.getComments(POST_ID) } returns flow { throw IllegalStateException("unavailable") }

    val viewModel = createViewModel()

    assertEquals(R.string.error_no_network, viewModel.messageRes.value)
    assertTrue(viewModel.comments.value.isEmpty())
  }

  @Test
  fun `onMessageShown clears the message`() {
    val viewModel = createViewModel()
    postFlow.tryEmit(null)

    viewModel.onMessageShown()

    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `add comment click is allowed when signed in`() {
    val viewModel = createViewModel()

    assertTrue(viewModel.onAddCommentClick())
    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `add comment click is refused with a message when signed out`() {
    every { firebaseAuth.currentUser } returns null
    val viewModel = createViewModel()

    assertFalse(viewModel.onAddCommentClick())
    assertEquals(R.string.error_account_required_comment, viewModel.messageRes.value)
  }

  private companion object {
    const val POST_ID = "post-1"
  }
}

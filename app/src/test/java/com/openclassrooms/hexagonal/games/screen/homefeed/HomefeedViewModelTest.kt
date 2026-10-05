package com.openclassrooms.hexagonal.games.screen.homefeed

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
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
class HomefeedViewModelTest {

  private lateinit var repository: PostRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var networkChecker: NetworkChecker
  private val postsFlow = MutableSharedFlow<List<Post>>(replay = 1)

  @Before
  fun setUp() {
    Dispatchers.setMain(UnconfinedTestDispatcher())
    repository = mockk()
    firebaseAuth = mockk()
    networkChecker = mockk()
    every { repository.posts } returns postsFlow
    every { networkChecker.isOnline() } returns true
    every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel() = HomefeedViewModel(repository, firebaseAuth, networkChecker)

  private fun post(id: String) = Post(
    id = id,
    title = "title $id",
    description = "description",
    photoUrl = null,
    timestamp = 1,
    author = null
  )

  @Test
  fun `posts are exposed as emitted by the repository`() {
    val viewModel = createViewModel()

    postsFlow.tryEmit(listOf(post("2"), post("1")))

    assertEquals(listOf(post("2"), post("1")), viewModel.posts.value)
    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `posts are updated in real time on each emission`() {
    val viewModel = createViewModel()
    postsFlow.tryEmit(listOf(post("1")))

    postsFlow.tryEmit(listOf(post("2"), post("1")))

    assertEquals(listOf(post("2"), post("1")), viewModel.posts.value)
  }

  @Test
  fun `an empty list online raises the no posts message`() {
    val viewModel = createViewModel()

    postsFlow.tryEmit(emptyList())

    assertTrue(viewModel.posts.value.isEmpty())
    assertEquals(R.string.no_posts, viewModel.messageRes.value)
  }

  @Test
  fun `an empty list offline raises the no network message`() {
    every { networkChecker.isOnline() } returns false
    val viewModel = createViewModel()

    postsFlow.tryEmit(emptyList())

    assertEquals(R.string.error_no_network, viewModel.messageRes.value)
  }

  @Test
  fun `the first load offline raises the no network message even with cached posts`() {
    every { networkChecker.isOnline() } returns false
    val viewModel = createViewModel()

    postsFlow.tryEmit(listOf(post("1")))

    assertEquals(1, viewModel.posts.value.size)
    assertEquals(R.string.error_no_network, viewModel.messageRes.value)
  }

  @Test
  fun `a later non empty emission offline raises no message`() {
    val viewModel = createViewModel()
    postsFlow.tryEmit(listOf(post("1")))
    every { networkChecker.isOnline() } returns false

    postsFlow.tryEmit(listOf(post("2"), post("1")))

    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `a flow error online raises the generic message instead of crashing`() {
    every { repository.posts } returns flow { throw IllegalStateException("permission denied") }

    val viewModel = createViewModel()

    assertEquals(R.string.error_generic, viewModel.messageRes.value)
    assertTrue(viewModel.posts.value.isEmpty())
  }

  @Test
  fun `a flow error offline raises the no network message`() {
    every { networkChecker.isOnline() } returns false
    every { repository.posts } returns flow { throw IllegalStateException("unavailable") }

    val viewModel = createViewModel()

    assertEquals(R.string.error_no_network, viewModel.messageRes.value)
  }

  @Test
  fun `onMessageShown clears the message`() {
    val viewModel = createViewModel()
    postsFlow.tryEmit(emptyList())

    viewModel.onMessageShown()

    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `add post click is allowed when signed in`() {
    val viewModel = createViewModel()

    assertTrue(viewModel.onAddPostClick())
    assertNull(viewModel.messageRes.value)
  }

  @Test
  fun `add post click is refused with a message when signed out`() {
    every { firebaseAuth.currentUser } returns null
    val viewModel = createViewModel()

    assertFalse(viewModel.onAddPostClick())
    assertEquals(R.string.error_account_required_post, viewModel.messageRes.value)
  }
}

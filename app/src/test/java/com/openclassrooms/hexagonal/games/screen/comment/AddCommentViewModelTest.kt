package com.openclassrooms.hexagonal.games.screen.comment

import androidx.lifecycle.SavedStateHandle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.CommentRepository
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.Screen
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddCommentViewModelTest {

  private lateinit var repository: CommentRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var networkChecker: NetworkChecker
  private lateinit var viewModel: AddCommentViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(StandardTestDispatcher())
    repository = mockk(relaxed = true)
    firebaseAuth = mockk()
    networkChecker = mockk()
    every { networkChecker.isOnline() } returns true
    every { firebaseAuth.currentUser } returns mockk<FirebaseUser> {
      every { uid } returns "uid-1"
      every { displayName } returns "Ada Lovelace King"
    }
    viewModel = AddCommentViewModel(
      SavedStateHandle(mapOf(Screen.AddComment.ARG_POST_ID to POST_ID)),
      repository,
      firebaseAuth,
      networkChecker
    )
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `an empty comment is not valid`() {
    assertFalse(viewModel.uiState.value.isContentValid)
  }

  @Test
  fun `a blank comment is not valid`() {
    viewModel.onContentChanged("   ")

    assertFalse(viewModel.uiState.value.isContentValid)
  }

  @Test
  fun `a non blank comment is valid`() {
    viewModel.onContentChanged("Nice game")

    assertTrue(viewModel.uiState.value.isContentValid)
    assertEquals("Nice game", viewModel.uiState.value.content)
  }

  @Test
  fun `saving an invalid comment writes nothing`() = runTest {
    viewModel.onContentChanged("  ")

    viewModel.addComment()
    advanceUntilIdle()

    coVerify(exactly = 0) { repository.addComment(any(), any()) }
    assertFalse(viewModel.uiState.value.isSaved)
  }

  @Test
  fun `saving writes the trimmed comment on the post, authored by the signed in user`() = runTest {
    val saved = slot<Comment>()
    coEvery { repository.addComment(POST_ID, capture(saved)) } returns Unit
    viewModel.onContentChanged("  Nice game  ")

    viewModel.addComment()
    advanceUntilIdle()

    assertEquals("Nice game", saved.captured.content)
    assertEquals(User("uid-1", "Ada", "Lovelace King"), saved.captured.author)
    assertTrue(saved.captured.id.isNotEmpty())
    assertTrue(viewModel.uiState.value.isSaved)
    assertFalse(viewModel.uiState.value.isSaving)
    assertNull(viewModel.uiState.value.messageRes)
  }

  @Test
  fun `saving offline raises the no network message and writes nothing`() = runTest {
    every { networkChecker.isOnline() } returns false
    viewModel.onContentChanged("Nice game")

    viewModel.addComment()
    advanceUntilIdle()

    assertEquals(R.string.error_no_network, viewModel.uiState.value.messageRes)
    assertFalse(viewModel.uiState.value.isSaved)
    coVerify(exactly = 0) { repository.addComment(any(), any()) }
  }

  @Test
  fun `saving signed out raises the generic message and writes nothing`() = runTest {
    every { firebaseAuth.currentUser } returns null
    viewModel.onContentChanged("Nice game")

    viewModel.addComment()
    advanceUntilIdle()

    assertEquals(R.string.error_generic, viewModel.uiState.value.messageRes)
    assertFalse(viewModel.uiState.value.isSaved)
    coVerify(exactly = 0) { repository.addComment(any(), any()) }
  }

  @Test
  fun `a failed write raises the generic message and allows a retry`() = runTest {
    coEvery { repository.addComment(any(), any()) } throws IllegalStateException("denied")
    viewModel.onContentChanged("Nice game")

    viewModel.addComment()
    advanceUntilIdle()

    assertEquals(R.string.error_generic, viewModel.uiState.value.messageRes)
    assertFalse(viewModel.uiState.value.isSaved)
    assertFalse(viewModel.uiState.value.isSaving)
    assertEquals("Nice game", viewModel.uiState.value.content)
  }

  @Test
  fun `a second save while the first one is running is ignored`() = runTest {
    val gate = CompletableDeferred<Unit>()
    coEvery { repository.addComment(any(), any()) } coAnswers { gate.await() }
    viewModel.onContentChanged("Nice game")

    viewModel.addComment()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isSaving)
    viewModel.addComment()
    gate.complete(Unit)
    advanceUntilIdle()

    coVerify(exactly = 1) { repository.addComment(any(), any()) }
    assertTrue(viewModel.uiState.value.isSaved)
  }

  @Test
  fun `onMessageShown clears the message`() = runTest {
    every { networkChecker.isOnline() } returns false
    viewModel.onContentChanged("Nice game")
    viewModel.addComment()

    viewModel.onMessageShown()

    assertNull(viewModel.uiState.value.messageRes)
  }

  private companion object {
    const val POST_ID = "post-1"
  }
}

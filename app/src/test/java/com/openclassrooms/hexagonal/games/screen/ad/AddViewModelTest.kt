package com.openclassrooms.hexagonal.games.screen.ad

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
class AddViewModelTest {
  
  private lateinit var repository: PostRepository
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var networkChecker: NetworkChecker
  private lateinit var viewModel: AddViewModel
  
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
    viewModel = AddViewModel(repository, firebaseAuth, networkChecker)
  }
  
  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }
  
  private fun fillValidForm() {
    viewModel.onAction(FormEvent.TitleChanged("A title"))
    viewModel.onAction(FormEvent.DescriptionChanged("A description"))
  }
  
  @Test
  fun `empty form reports the title error first`() = runTest {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.error.collect() }
    
    advanceUntilIdle()
    
    assertEquals(FormError.TitleError, viewModel.error.value)
  }
  
  @Test
  fun `title without description reports the description error`() = runTest {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.error.collect() }
    
    viewModel.onAction(FormEvent.TitleChanged("A title"))
    viewModel.onAction(FormEvent.DescriptionChanged("   "))
    advanceUntilIdle()
    
    assertEquals(FormError.DescriptionError, viewModel.error.value)
  }
  
  @Test
  fun `description without title reports the title error`() = runTest {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.error.collect() }
    
    viewModel.onAction(FormEvent.DescriptionChanged("A description"))
    advanceUntilIdle()
    
    assertEquals(FormError.TitleError, viewModel.error.value)
  }
  
  @Test
  fun `valid form has no error`() = runTest {
    backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.error.collect() }
    
    advanceUntilIdle()
    assertEquals(FormError.TitleError, viewModel.error.value)
    
    fillValidForm()
    advanceUntilIdle()
    
    assertNull(viewModel.error.value)
  }
  
  @Test
  fun `addPost does nothing when the form is invalid`() = runTest {
    viewModel.onAction(FormEvent.TitleChanged("A title"))
    
    viewModel.addPost()
    advanceUntilIdle()
    
    coVerify(exactly = 0) { repository.addPost(any()) }
    assertEquals(AddUiState(), viewModel.uiState.value)
  }
  
  @Test
  fun `addPost saves the post with the signed-in user as author and flags saved`() = runTest {
    val saved = slot<Post>()
    coEvery { repository.addPost(capture(saved)) } returns Unit
    fillValidForm()
    
    viewModel.addPost()
    advanceUntilIdle()
    
    assertEquals("A title", saved.captured.title)
    assertEquals("A description", saved.captured.description)
    assertEquals(User("uid-1", "Ada", "Lovelace King"), saved.captured.author)
    assertTrue(viewModel.uiState.value.isSaved)
    assertFalse(viewModel.uiState.value.isSaving)
    assertNull(viewModel.uiState.value.messageRes)
  }
  
  @Test
  fun `addPost raises the no network message when offline`() = runTest {
    every { networkChecker.isOnline() } returns false
    fillValidForm()
    
    viewModel.addPost()
    advanceUntilIdle()
    
    coVerify(exactly = 0) { repository.addPost(any()) }
    assertEquals(R.string.error_no_network, viewModel.uiState.value.messageRes)
    assertFalse(viewModel.uiState.value.isSaved)
  }
  
  @Test
  fun `addPost raises the generic error when nobody is signed in`() = runTest {
    every { firebaseAuth.currentUser } returns null
    fillValidForm()
    
    viewModel.addPost()
    advanceUntilIdle()
    
    coVerify(exactly = 0) { repository.addPost(any()) }
    assertEquals(R.string.error_generic, viewModel.uiState.value.messageRes)
  }
  
  @Test
  fun `addPost raises the generic error when the write fails`() = runTest {
    coEvery { repository.addPost(any()) } throws RuntimeException("PERMISSION_DENIED")
    fillValidForm()
    
    viewModel.addPost()
    advanceUntilIdle()
    
    assertEquals(R.string.error_generic, viewModel.uiState.value.messageRes)
    assertFalse(viewModel.uiState.value.isSaved)
    assertFalse(viewModel.uiState.value.isSaving)
  }
  
  @Test
  fun `addPost ignores a second click while saving`() = runTest {
    val gate = CompletableDeferred<Unit>()
    coEvery { repository.addPost(any()) } coAnswers { gate.await() }
    fillValidForm()
    
    viewModel.addPost()
    advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isSaving)
    viewModel.addPost()
    gate.complete(Unit)
    advanceUntilIdle()
    
    coVerify(exactly = 1) { repository.addPost(any()) }
    assertTrue(viewModel.uiState.value.isSaved)
  }
  
  @Test
  fun `onMessageShown clears the message`() = runTest {
    every { networkChecker.isOnline() } returns false
    fillValidForm()
    viewModel.addPost()
    
    viewModel.onMessageShown()
    
    assertNull(viewModel.uiState.value.messageRes)
  }
}

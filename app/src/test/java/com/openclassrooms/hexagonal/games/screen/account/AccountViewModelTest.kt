package com.openclassrooms.hexagonal.games.screen.account

import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AccountViewModelTest {
  
  private lateinit var firebaseAuth: FirebaseAuth
  private lateinit var viewModel: AccountViewModel
  
  @Before
  fun setUp() {
    firebaseAuth = mockk(relaxed = true)
    viewModel = AccountViewModel(firebaseAuth)
  }
  
  /** Mocks a signed-in user whose delete() task exposes its listeners through the returned slots. */
  private fun signedInUser(
    successListener: io.mockk.CapturingSlot<OnSuccessListener<in Void>>,
    failureListener: io.mockk.CapturingSlot<OnFailureListener>,
  ) {
    val task = mockk<Task<Void>>()
    every { task.addOnSuccessListener(capture(successListener)) } returns task
    every { task.addOnFailureListener(capture(failureListener)) } returns task
    val user = mockk<FirebaseUser>()
    every { user.delete() } returns task
    every { firebaseAuth.currentUser } returns user
  }
  
  @Test
  fun `initial state has no sign out and no error`() {
    assertEquals(AccountUiState(), viewModel.uiState.value)
  }
  
  @Test
  fun `signOut signs out from Firebase and flags the state`() {
    viewModel.signOut()
    
    verify(exactly = 1) { firebaseAuth.signOut() }
    assertTrue(viewModel.uiState.value.isSignedOut)
    assertFalse(viewModel.uiState.value.hasError)
  }
  
  @Test
  fun `deleteAccount flags signed out when deletion succeeds`() {
    val success = slot<OnSuccessListener<in Void>>()
    val failure = slot<OnFailureListener>()
    signedInUser(success, failure)
    
    viewModel.deleteAccount()
    assertFalse(viewModel.uiState.value.isSignedOut)
    success.captured.onSuccess(null)
    
    assertTrue(viewModel.uiState.value.isSignedOut)
    assertFalse(viewModel.uiState.value.hasError)
  }
  
  @Test
  fun `deleteAccount raises the error when deletion fails`() {
    val success = slot<OnSuccessListener<in Void>>()
    val failure = slot<OnFailureListener>()
    signedInUser(success, failure)
    
    viewModel.deleteAccount()
    failure.captured.onFailure(Exception("requires recent login"))
    
    assertTrue(viewModel.uiState.value.hasError)
    assertFalse(viewModel.uiState.value.isSignedOut)
  }
  
  @Test
  fun `deleteAccount without a current user leaves the screen`() {
    every { firebaseAuth.currentUser } returns null
    
    viewModel.deleteAccount()
    
    assertTrue(viewModel.uiState.value.isSignedOut)
    assertFalse(viewModel.uiState.value.hasError)
  }
  
  @Test
  fun `onErrorShown clears the error`() {
    val success = slot<OnSuccessListener<in Void>>()
    val failure = slot<OnFailureListener>()
    signedInUser(success, failure)
    viewModel.deleteAccount()
    failure.captured.onFailure(Exception())
    
    viewModel.onErrorShown()
    
    assertFalse(viewModel.uiState.value.hasError)
  }
}

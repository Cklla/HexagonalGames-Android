package com.openclassrooms.hexagonal.games.screen.settings

import com.google.firebase.messaging.FirebaseMessaging
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class SettingsViewModelTest {
  
  private lateinit var firebaseMessaging: FirebaseMessaging
  private lateinit var viewModel: SettingsViewModel
  
  @Before
  fun setUp() {
    firebaseMessaging = mockk(relaxed = true)
    viewModel = SettingsViewModel(firebaseMessaging)
  }
  
  @Test
  fun `enableNotifications subscribes to the campaigns topic`() {
    viewModel.enableNotifications()

    verify(exactly = 1) { firebaseMessaging.subscribeToTopic("campaigns") }
    verify(exactly = 0) { firebaseMessaging.unsubscribeFromTopic(any()) }
  }
  
  @Test
  fun `disableNotifications unsubscribes from the campaigns topic`() {
    viewModel.disableNotifications()

    verify(exactly = 1) { firebaseMessaging.unsubscribeFromTopic("campaigns") }
    verify(exactly = 0) { firebaseMessaging.subscribeToTopic(any()) }
  }
}

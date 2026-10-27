package com.openclassrooms.hexagonal.games.screen.settings

import androidx.lifecycle.ViewModel
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel responsible for managing user settings, specifically notification preferences.
 *
 * Notifications are FCM campaigns sent from the Firebase console to the [CAMPAIGNS_TOPIC] topic:
 * enabling them subscribes this installation to the topic, disabling them unsubscribes it.
 * FCM keeps the subscription across app restarts and retries it once the network is back.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
  private val firebaseMessaging: FirebaseMessaging
) : ViewModel() {
  /**
   * Enables notifications for the application by subscribing to the campaigns topic.
   */
  fun enableNotifications() {
    firebaseMessaging.subscribeToTopic(CAMPAIGNS_TOPIC)
  }
  
  /**
   * Disables notifications for the application by unsubscribing from the campaigns topic.
   */
  fun disableNotifications() {
    firebaseMessaging.unsubscribeFromTopic(CAMPAIGNS_TOPIC)
  }
  
  companion object {
    /**
     * FCM topic targeted by the notification campaigns.
     */
    const val CAMPAIGNS_TOPIC = "campaigns"
  }
}

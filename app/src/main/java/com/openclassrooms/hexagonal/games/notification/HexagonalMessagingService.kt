package com.openclassrooms.hexagonal.games.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives Firebase Cloud Messaging events while the app process is alive or woken up by the system.
 */
class HexagonalMessagingService : FirebaseMessagingService() {

  /**
   * Called when Firebase issues a new registration token for this app installation.
   */
  override fun onNewToken(token: String) {
    super.onNewToken(token)
    Log.d(TAG, "New token: $token")
  }

  /**
   * Called when a message must be handled by the app: any data message, or a notification
   * message received while the app is in the foreground.
   */
  override fun onMessageReceived(message: RemoteMessage) {
    super.onMessageReceived(message)
    Log.d(TAG, "Message received: title=${message.notification?.title}, body=${message.notification?.body}")
  }

  private companion object {
    const val TAG = "FCM"
  }
}

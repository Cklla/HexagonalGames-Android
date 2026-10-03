package com.openclassrooms.hexagonal.games.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Tells whether the device currently has a network connection. Kept behind an interface so that
 * ViewModels can be unit-tested without the Android framework.
 */
interface NetworkChecker {
  fun isOnline(): Boolean
}

/**
 * NetworkChecker backed by Android's [ConnectivityManager]: online when the active network
 * is able to reach the Internet.
 */
class AndroidNetworkChecker @Inject constructor(
  @ApplicationContext private val context: Context
) : NetworkChecker {
  
  override fun isOnline(): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }
}

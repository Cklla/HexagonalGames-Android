package com.openclassrooms.hexagonal.games.screen

import android.net.Uri
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

private const val POST_ID_ARG = "postId"

sealed class Screen(
  val route: String,
  val navArguments: List<NamedNavArgument> = emptyList()
) {
  data object Homefeed : Screen("homefeed")
  
  data object AddPost : Screen("addPost")
  
  data object Settings : Screen("settings")
  
  data object Account : Screen("account")

  data object PostDetail : Screen(
    route = "postDetail/{$POST_ID_ARG}",
    navArguments = listOf(navArgument(POST_ID_ARG) { type = NavType.StringType })
  ) {
    const val ARG_POST_ID = POST_ID_ARG

    /**
     * Builds the concrete route to open the detail of the given post.
     */
    fun createRoute(postId: String) = "postDetail/${Uri.encode(postId)}"
  }
}
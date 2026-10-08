package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.domain.model.Post
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for interacting with Post data from a data source.
 * It outlines the methods for retrieving and adding Posts, abstracting the underlying
 * implementation details of fetching and persisting data.
 */
interface PostApi {
  /**
   * Retrieves a list of Posts ordered by their creation date in descending order.
   *
   * @return A list of Posts sorted by creation date (newest first).
   */
  fun getPostsOrderByCreationDateDesc(): Flow<List<Post>>
  
  /**
   * Retrieves a single Post and keeps it up to date.
   *
   * @param postId The ID of the Post to retrieve.
   * @return A Flow emitting the Post each time it changes, or null if it does not exist (anymore).
   */
  fun getPost(postId: String): Flow<Post?>

  /**
   * Adds a new Post to the data source.
   *
   * @param post The Post object to be added.
   * @throws Exception if the post could not be persisted.
   */
  suspend fun addPost(post: Post)
}

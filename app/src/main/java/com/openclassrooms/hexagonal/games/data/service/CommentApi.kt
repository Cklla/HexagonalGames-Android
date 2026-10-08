package com.openclassrooms.hexagonal.games.data.service

import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow

/**
 * This interface defines the contract for reading and adding the comments of a Post from a data source,
 * abstracting the underlying implementation details.
 */
interface CommentApi {
  /**
   * Retrieves the comments of a Post ordered by their creation date in ascending order.
   *
   * @param postId The ID of the Post the comments belong to.
   * @return A Flow emitting the up-to-date list of comments (oldest first) each time it changes.
   */
  fun getCommentsOrderByCreationDateAsc(postId: String): Flow<List<Comment>>
  
  /**
   * Adds a new Comment to a Post.
   *
   * @param postId The ID of the Post the comment belongs to.
   * @param comment The Comment object to be added.
   * @throws Exception if the comment could not be persisted.
   */
  suspend fun addComment(postId: String, comment: Comment)
}

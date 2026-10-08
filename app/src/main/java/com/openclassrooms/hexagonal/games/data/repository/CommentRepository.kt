package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.CommentApi
import com.openclassrooms.hexagonal.games.domain.model.Comment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class provides a repository for accessing the Comment data of a Post.
 * It utilizes dependency injection to retrieve a CommentApi instance for interacting
 * with the data source.
 */
@Singleton
class CommentRepository @Inject constructor(private val commentApi: CommentApi) {

  /**
   * Retrieves a Flow containing the comments of a Post, oldest first.
   *
   * @param postId The ID of the Post the comments belong to.
   * @return Flow containing the list of Comments, re-emitted in real time on each change.
   */
  fun getComments(postId: String): Flow<List<Comment>> =
    commentApi.getCommentsOrderByCreationDateAsc(postId)

}

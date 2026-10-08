package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

/**
 * This class represents a Comment left on a Post. It holds the comment ID, its text, its creation
 * timestamp and its author (User object).
 */
data class Comment(
  /**
   * Unique identifier for the Comment.
   */
  val id: String,

  /**
   * Text of the Comment.
   */
  val content: String,

  /**
   * Timestamp representing the creation date and time of the Comment in milliseconds since epoch.
   */
  val timestamp: Long,

  /**
   * User object representing the author of the Comment.
   */
  val author: User?
) : Serializable

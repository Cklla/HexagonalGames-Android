package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

/**
 * This class represents a Post data object. It holds information about a post, including its
 * ID, title, description, photo URL, creation timestamp, and the author (User object).
 * The class implements Serializable to allow for potential serialization needs.
 */
data class Post(
  /**
   * Unique identifier for the Post.
   */
  val id: String,
  
  /**
   * Title of the Post.
   */
  val title: String,
  
  /**
   * Optional description for the Post.
   */
  val description: String?,
  
  /**
   * URL of an image associated with the Post, if any.
   */
  val photoUrl: String?,
  
  /**
   * Timestamp representing the creation date and time of the Post in milliseconds since epoch.
   */
  val timestamp: Long,
  
  /**
   * User object representing the author of the Post.
   */
  val author: User?
) : Serializable

package com.openclassrooms.hexagonal.games.domain.model

import java.io.Serializable

/**
 * This class represents a User data object. It holds basic information about a user, including
 * their ID, first name, and last name. The class implements Serializable to allow for potential
 * serialization needs.
 */
data class User(
  /**
   * Unique identifier for the User.
   */
  val id: String,
  
  /**
   * User's first name.
   */
  val firstname: String,
  
  /**
   * User's last name.
   */
  val lastname: String
) : Serializable


/**
 * Builds a User from a Firebase account. FirebaseUI collects a single "first name & surname"
 * field, stored as the account display name: the first word becomes the first name and the
 * remaining words the last name.
 *
 * @param id The Firebase user ID.
 * @param displayName The account display name, if any.
 */
fun userFromDisplayName(id: String, displayName: String?): User {
  val parts = displayName.orEmpty().trim().split(Regex("\\s+"), limit = 2)
  return User(id = id, firstname = parts[0], lastname = parts.getOrElse(1) { "" })
}

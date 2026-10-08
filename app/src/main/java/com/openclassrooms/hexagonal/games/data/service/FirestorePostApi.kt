package com.openclassrooms.hexagonal.games.data.service

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * PostApi implementation backed by Cloud Firestore ("low-level" SDK, no FirebaseUI).
 * Posts are stored in the `posts` collection, one document per post (the document ID is the post ID).
 */
class FirestorePostApi(private val firestore: FirebaseFirestore) : PostApi {
  
  /**
   * Listens to the `posts` collection, newest first. The flow emits a new list every time
   * the collection changes, and removes the Firestore listener when the collector goes away.
   */
  override fun getPostsOrderByCreationDateDesc(): Flow<List<Post>> = callbackFlow {
    val registration = firestore.collection(POSTS_COLLECTION)
      .orderBy(FIELD_TIMESTAMP, Query.Direction.DESCENDING)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }
        trySend(snapshot?.documents.orEmpty().mapNotNull { it.toPost() })
      }
    awaitClose { registration.remove() }
  }
  
  /**
   * Listens to a single post document. Emits null if the document does not exist or is malformed.
   */
  override fun getPost(postId: String): Flow<Post?> = callbackFlow {
    val registration = firestore.collection(POSTS_COLLECTION)
      .document(postId)
      .addSnapshotListener { snapshot, error ->
        if (error != null) {
          close(error)
          return@addSnapshotListener
        }
        trySend(snapshot?.toPost())
      }
    awaitClose { registration.remove() }
  }

  /**
   * Writes the post in Firestore and suspends until the server has acknowledged it.
   *
   * @throws Exception if the write fails (e.g. permission denied).
   */
  override suspend fun addPost(post: Post) {
    firestore.collection(POSTS_COLLECTION)
      .document(post.id)
      .set(post.toFirestoreMap())
      .await()
  }
  
  private fun Post.toFirestoreMap(): Map<String, Any?> = mapOf(
    FIELD_TITLE to title,
    FIELD_DESCRIPTION to description,
    FIELD_PHOTO_URL to photoUrl,
    FIELD_TIMESTAMP to timestamp,
    FIELD_AUTHOR to author?.let {
      mapOf(
        FIELD_AUTHOR_ID to it.id,
        FIELD_AUTHOR_FIRSTNAME to it.firstname,
        FIELD_AUTHOR_LASTNAME to it.lastname
      )
    }
  )
  
  /**
   * Maps a Firestore document to a Post, or returns null if the document is missing its mandatory
   * fields (e.g. a malformed document typed by hand in the console).
   */
  private fun DocumentSnapshot.toPost(): Post? {
    val title = getString(FIELD_TITLE) ?: return null
    val timestamp = getLong(FIELD_TIMESTAMP) ?: return null
    val authorFields = get(FIELD_AUTHOR) as? Map<*, *>
    val author = authorFields?.let {
      User(
        id = it[FIELD_AUTHOR_ID] as? String ?: "",
        firstname = it[FIELD_AUTHOR_FIRSTNAME] as? String ?: "",
        lastname = it[FIELD_AUTHOR_LASTNAME] as? String ?: ""
      )
    }
    return Post(
      id = id,
      title = title,
      description = getString(FIELD_DESCRIPTION),
      photoUrl = getString(FIELD_PHOTO_URL),
      timestamp = timestamp,
      author = author
    )
  }
  
  private companion object {
    const val POSTS_COLLECTION = "posts"
    const val FIELD_TITLE = "title"
    const val FIELD_DESCRIPTION = "description"
    const val FIELD_PHOTO_URL = "photoUrl"
    const val FIELD_TIMESTAMP = "timestamp"
    const val FIELD_AUTHOR = "author"
    const val FIELD_AUTHOR_ID = "id"
    const val FIELD_AUTHOR_FIRSTNAME = "firstname"
    const val FIELD_AUTHOR_LASTNAME = "lastname"
  }
}

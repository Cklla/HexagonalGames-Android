package com.openclassrooms.hexagonal.games.data.service

import com.firebase.ui.common.ChangeEventType
import com.firebase.ui.firestore.ChangeEventListener
import com.firebase.ui.firestore.FirestoreArray
import com.firebase.ui.firestore.SnapshotParser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * CommentApi implementation backed by FirebaseUI Firestore.
 * The comments of a post are stored in the `comments` sub-collection of the post document
 * (`posts/{postId}/comments`), one document per comment.
 *
 * FirebaseUI Firestore 9.x only ships adapters for `RecyclerView` and Paging, no composable. Its
 * [FirestoreArray] is the engine underneath those adapters: an observable list kept in sync with a
 * Firestore query. It is used here directly and exposed as a Flow, which Compose can consume.
 */
class FirestoreCommentApi(private val firestore: FirebaseFirestore) : CommentApi {

  /**
   * Listens to the comments of the post, oldest first. The flow emits the whole list every time
   * the sub-collection changes, and stops listening when the collector goes away.
   */
  override fun getCommentsOrderByCreationDateAsc(postId: String): Flow<List<Comment>> = callbackFlow {
    val query = firestore.collection(POSTS_COLLECTION)
      .document(postId)
      .collection(COMMENTS_COLLECTION)
      .orderBy(FIELD_TIMESTAMP, Query.Direction.ASCENDING)
    val comments = FirestoreArray(query, SnapshotParser { it.toComment() })

    val listener = object : ChangeEventListener {
      override fun onChildChanged(
        type: ChangeEventType,
        snapshot: DocumentSnapshot,
        newIndex: Int,
        oldIndex: Int
      ) {
        // Nothing to do per item: the whole list is re-emitted in onDataChanged()
      }

      override fun onDataChanged() {
        trySend(comments.toList())
      }

      override fun onError(e: FirebaseFirestoreException) {
        close(e)
      }
    }

    comments.addChangeEventListener(listener)
    awaitClose { comments.removeChangeEventListener(listener) }
  }

  /**
   * Maps a Firestore document to a Comment. FirebaseUI requires a non-null result, so missing
   * fields fall back to empty values instead of dropping the document.
   */
  private fun DocumentSnapshot.toComment(): Comment {
    val authorFields = get(FIELD_AUTHOR) as? Map<*, *>
    val author = authorFields?.let {
      User(
        id = it[FIELD_AUTHOR_ID] as? String ?: "",
        firstname = it[FIELD_AUTHOR_FIRSTNAME] as? String ?: "",
        lastname = it[FIELD_AUTHOR_LASTNAME] as? String ?: ""
      )
    }
    return Comment(
      id = id,
      content = getString(FIELD_CONTENT).orEmpty(),
      timestamp = getLong(FIELD_TIMESTAMP) ?: 0L,
      author = author
    )
  }

  private companion object {
    const val POSTS_COLLECTION = "posts"
    const val COMMENTS_COLLECTION = "comments"
    const val FIELD_CONTENT = "content"
    const val FIELD_TIMESTAMP = "timestamp"
    const val FIELD_AUTHOR = "author"
    const val FIELD_AUTHOR_ID = "id"
    const val FIELD_AUTHOR_FIRSTNAME = "firstname"
    const val FIELD_AUTHOR_LASTNAME = "lastname"
  }
}

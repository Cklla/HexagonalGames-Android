package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.domain.model.Post
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class PostRepositoryTest {
  
  private lateinit var api: PostApi
  private lateinit var repository: PostRepository
  
  private fun post(id: String) = Post(
    id = id,
    title = "title $id",
    description = "description",
    photoUrl = null,
    timestamp = 1,
    author = null
  )
  
  @Before
  fun setUp() {
    api = mockk()
    every { api.getPostsOrderByCreationDateDesc() } returns flowOf(listOf(post("2"), post("1")))
    repository = PostRepository(api)
  }
  
  @Test
  fun `posts are the ones of the api, in the same order`() = runTest {
    assertEquals(listOf(listOf(post("2"), post("1"))), repository.posts.toList())
  }
  
  @Test
  fun `getPost forwards the id to the api`() = runTest {
    every { api.getPost("1") } returns flowOf(post("1"), null)

    assertEquals(listOf(post("1"), null), repository.getPost("1").toList())
  }
  
  @Test
  fun `addPost forwards the post to the api`() = runTest {
    coEvery { api.addPost(any()) } returns Unit

    repository.addPost(post("3"))

    coVerify(exactly = 1) { api.addPost(post("3")) }
  }
  
  @Test
  fun `addPost propagates the api failure`() = runTest {
    coEvery { api.addPost(any()) } throws IllegalStateException("denied")

    try {
      repository.addPost(post("3"))
      fail("the exception should have been propagated")
    } catch (e: IllegalStateException) {
      assertEquals("denied", e.message)
    }
  }
}

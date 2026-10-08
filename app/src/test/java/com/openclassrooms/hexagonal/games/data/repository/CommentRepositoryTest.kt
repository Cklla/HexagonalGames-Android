package com.openclassrooms.hexagonal.games.data.repository

import com.openclassrooms.hexagonal.games.data.service.CommentApi
import com.openclassrooms.hexagonal.games.domain.model.Comment
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

class CommentRepositoryTest {
  
  private lateinit var api: CommentApi
  private lateinit var repository: CommentRepository
  
  private fun comment(id: String) = Comment(id = id, content = "comment $id", timestamp = 1, author = null)
  
  @Before
  fun setUp() {
    api = mockk()
    repository = CommentRepository(api)
  }
  
  @Test
  fun `getComments forwards the post id and keeps the api order`() = runTest {
    every { api.getCommentsOrderByCreationDateAsc("post-1") } returns
      flowOf(listOf(comment("1"), comment("2")))

    assertEquals(
      listOf(listOf(comment("1"), comment("2"))),
      repository.getComments("post-1").toList()
    )
  }
  
  @Test
  fun `addComment forwards the post id and the comment to the api`() = runTest {
    coEvery { api.addComment(any(), any()) } returns Unit

    repository.addComment("post-1", comment("3"))

    coVerify(exactly = 1) { api.addComment("post-1", comment("3")) }
  }
  
  @Test
  fun `addComment propagates the api failure`() = runTest {
    coEvery { api.addComment(any(), any()) } throws IllegalStateException("denied")

    try {
      repository.addComment("post-1", comment("3"))
      fail("the exception should have been propagated")
    } catch (e: IllegalStateException) {
      assertEquals("denied", e.message)
    }
  }
}

package com.openclassrooms.hexagonal.games.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {
  
  @Test
  fun `first word is the first name and the rest the last name`() {
    assertEquals(User("1", "Ada", "Lovelace King"), userFromDisplayName("1", "Ada Lovelace King"))
  }
  
  @Test
  fun `extra spaces are ignored`() {
    assertEquals(User("1", "Ada", "Lovelace"), userFromDisplayName("1", "  Ada   Lovelace "))
  }
  
  @Test
  fun `single word leaves the last name empty`() {
    assertEquals(User("1", "Ada", ""), userFromDisplayName("1", "Ada"))
  }
  
  @Test
  fun `missing display name gives empty names`() {
    assertEquals(User("1", "", ""), userFromDisplayName("1", null))
  }
}

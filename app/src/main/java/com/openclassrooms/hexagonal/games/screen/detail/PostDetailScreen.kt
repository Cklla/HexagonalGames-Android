package com.openclassrooms.hexagonal.games.screen.detail

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Comment
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
  modifier: Modifier = Modifier,
  viewModel: PostDetailViewModel = hiltViewModel(),
  onBackClick: () -> Unit = {},
  onFABClick: () -> Unit = {},
) {
  val post by viewModel.post.collectAsStateWithLifecycle()
  val comments by viewModel.comments.collectAsStateWithLifecycle()
  val messageRes by viewModel.messageRes.collectAsStateWithLifecycle()
  val context = LocalContext.current

  LaunchedEffect(messageRes) {
    messageRes?.let {
      Toast.makeText(context, context.getString(it), Toast.LENGTH_LONG).show()
      viewModel.onMessageShown()
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = post?.title.orEmpty(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        },
        navigationIcon = {
          IconButton(onClick = onBackClick) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        }
      )
    },
    floatingActionButtonPosition = FabPosition.End,
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          if (viewModel.onAddCommentClick()) {
            onFABClick()
          }
        }
      ) {
        Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = stringResource(id = R.string.description_button_add)
        )
      }
    }
  ) { contentPadding ->
    PostDetailContent(
      modifier = Modifier.padding(contentPadding),
      post = post,
      comments = comments
    )
  }
}

@Composable
private fun PostDetailContent(
  modifier: Modifier = Modifier,
  post: Post?,
  comments: List<Comment>,
) {
  LazyColumn(
    modifier = modifier.padding(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    if (post != null) {
      item(key = "post") {
        PostHeader(post = post)
      }
    }
    items(comments, key = { it.id }) { comment ->
      CommentCell(comment = comment)
    }
  }
}

@Composable
private fun PostHeader(post: Post) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(8.dp),
  ) {
    Text(
      text = authorLabel(post.author),
      style = MaterialTheme.typography.titleSmall
    )
    Text(
      text = post.title,
      style = MaterialTheme.typography.headlineSmall
    )
    if (post.photoUrl.isNullOrEmpty() == false) {
      AsyncImage(
        modifier = Modifier
          .padding(top = 8.dp)
          .fillMaxWidth()
          .heightIn(max = 200.dp)
          .aspectRatio(ratio = 16 / 9f),
        model = post.photoUrl,
        placeholder = ColorPainter(Color.DarkGray),
        contentDescription = "image",
        contentScale = ContentScale.Crop,
      )
    }
    if (post.description.isNullOrEmpty() == false) {
      Text(
        modifier = Modifier.padding(top = 8.dp),
        text = post.description,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@Composable
private fun CommentCell(comment: Comment) {
  ElevatedCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text(
        text = authorLabel(comment.author),
        style = MaterialTheme.typography.titleSmall
      )
      Text(
        text = comment.content,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

/**
 * "By {first name} {last name}", same format as the Home screen.
 */
@Composable
private fun authorLabel(author: User?): String = stringResource(
  id = R.string.by,
  author?.firstname ?: "",
  author?.lastname ?: ""
)

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun PostDetailContentPreview() {
  val author = User(id = "1", firstname = "firstname", lastname = "lastname")
  HexagonalGamesTheme {
    PostDetailContent(
      post = Post(
        id = "1",
        title = "title",
        description = "description",
        photoUrl = null,
        timestamp = 1,
        author = author
      ),
      comments = listOf(
        Comment(id = "1", content = "first comment", timestamp = 1, author = author),
        Comment(id = "2", content = "second comment", timestamp = 2, author = author)
      )
    )
  }
}

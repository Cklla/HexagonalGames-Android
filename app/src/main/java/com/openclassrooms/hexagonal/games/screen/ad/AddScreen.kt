package com.openclassrooms.hexagonal.games.screen.ad

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
  modifier: Modifier = Modifier,
  viewModel: AddViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit
) {
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.add_fragment_label))
        },
        navigationIcon = {
          IconButton(onClick = {
            onBackClick()
          }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        }
      )
    }
  ) { contentPadding ->
    val post by viewModel.post.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    CreatePost(
      modifier = Modifier.padding(contentPadding),
      error = error,
      title = post.title,
      onTitleChanged = { viewModel.onAction(FormEvent.TitleChanged(it)) },
      description = post.description ?: "",
      onDescriptionChanged = { viewModel.onAction(FormEvent.DescriptionChanged(it)) },
      onSaveClicked = {
        viewModel.addPost()
        onSaveClick()
      }
    )
  }
}

@Composable
private fun CreatePost(
  modifier: Modifier = Modifier,
  title: String,
  onTitleChanged: (String) -> Unit,
  description: String,
  onDescriptionChanged: (String) -> Unit,
  onSaveClicked: () -> Unit,
  error: FormError?
) {
  val scrollState = rememberScrollState()
  
  Column(
    modifier = modifier
      .padding(16.dp)
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = modifier
        .fillMaxSize()
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = title,
        isError = error is FormError.TitleError,
        onValueChange = { onTitleChanged(it) },
        label = { Text(stringResource(id = R.string.hint_title)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        singleLine = true
      )
      if (error is FormError.TitleError) {
        Text(
          text = stringResource(id = error.messageRes),
          color = MaterialTheme.colorScheme.error,
        )
      }
      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = description,
        onValueChange = { onDescriptionChanged(it) },
        label = { Text(stringResource(id = R.string.hint_description)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
      )
    }
    Button(
      enabled = error == null,
      onClick = { onSaveClicked() }
    ) {
      Text(
        modifier = Modifier.padding(8.dp),
        text = stringResource(id = R.string.action_save)
      )
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreatePostPreview() {
  HexagonalGamesTheme {
    CreatePost(
      title = "test",
      onTitleChanged = { },
      description = "description",
      onDescriptionChanged = { },
      onSaveClicked = { },
      error = null
    )
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreatePostErrorPreview() {
  HexagonalGamesTheme {
    CreatePost(
      title = "test",
      onTitleChanged = { },
      description = "description",
      onDescriptionChanged = { },
      onSaveClicked = { },
      error = FormError.TitleError
    )
  }
}
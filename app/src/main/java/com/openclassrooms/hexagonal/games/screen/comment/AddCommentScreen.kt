package com.openclassrooms.hexagonal.games.screen.comment

import android.widget.Toast
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
fun AddCommentScreen(
  modifier: Modifier = Modifier,
  viewModel: AddCommentViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSaveClick: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current

  LaunchedEffect(uiState.isSaved) {
    if (uiState.isSaved) {
      onSaveClick()
    }
  }
  LaunchedEffect(uiState.messageRes) {
    uiState.messageRes?.let {
      Toast.makeText(context, context.getString(it), Toast.LENGTH_LONG).show()
      viewModel.onMessageShown()
    }
  }

  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.add_comment_label))
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
    }
  ) { contentPadding ->
    CreateComment(
      modifier = Modifier.padding(contentPadding),
      content = uiState.content,
      onContentChanged = viewModel::onContentChanged,
      isContentValid = uiState.isContentValid,
      isSaving = uiState.isSaving,
      onSaveClicked = viewModel::addComment
    )
  }
}

@Composable
private fun CreateComment(
  modifier: Modifier = Modifier,
  content: String,
  onContentChanged: (String) -> Unit,
  isContentValid: Boolean,
  onSaveClicked: () -> Unit,
  isSaving: Boolean = false
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = modifier
      .padding(16.dp)
      .fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .weight(1f)
        .verticalScroll(scrollState)
    ) {
      OutlinedTextField(
        modifier = Modifier
          .padding(top = 16.dp)
          .fillMaxWidth(),
        value = content,
        isError = !isContentValid,
        onValueChange = onContentChanged,
        label = { Text(stringResource(id = R.string.hint_comment)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
      )
      if (!isContentValid) {
        Text(
          text = stringResource(id = R.string.error_comment),
          color = MaterialTheme.colorScheme.error,
        )
      }
    }
    Button(
      enabled = isContentValid && !isSaving,
      onClick = onSaveClicked
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
private fun CreateCommentPreview() {
  HexagonalGamesTheme {
    CreateComment(
      content = "A comment",
      onContentChanged = { },
      isContentValid = true,
      onSaveClicked = { }
    )
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun CreateCommentErrorPreview() {
  HexagonalGamesTheme {
    CreateComment(
      content = "",
      onContentChanged = { },
      isContentValid = false,
      onSaveClicked = { }
    )
  }
}

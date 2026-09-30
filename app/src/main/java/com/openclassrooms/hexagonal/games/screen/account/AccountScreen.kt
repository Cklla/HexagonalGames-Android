package com.openclassrooms.hexagonal.games.screen.account

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
  modifier: Modifier = Modifier,
  viewModel: AccountViewModel = hiltViewModel(),
  onBackClick: () -> Unit,
  onSignedOut: () -> Unit,
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val errorMessage = stringResource(id = R.string.error_unknown)
  
  LaunchedEffect(uiState.isSignedOut) {
    if (uiState.isSignedOut) {
      onSignedOut()
    }
  }
  LaunchedEffect(uiState.hasError) {
    if (uiState.hasError) {
      Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
      viewModel.onErrorShown()
    }
  }
  
  Scaffold(
    modifier = modifier,
    topBar = {
      TopAppBar(
        title = {
          Text(stringResource(id = R.string.action_account))
        },
        navigationIcon = {
          IconButton(onClick = { onBackClick() }) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = stringResource(id = R.string.contentDescription_go_back)
            )
          }
        }
      )
    }
  ) { contentPadding ->
    Account(
      modifier = Modifier.padding(contentPadding),
      onSignOutClick = { viewModel.signOut() },
      onDeleteAccountClick = { viewModel.deleteAccount() }
    )
  }
}

@Composable
private fun Account(
  modifier: Modifier = Modifier,
  onSignOutClick: () -> Unit,
  onDeleteAccountClick: () -> Unit,
) {
  Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceEvenly
  ) {
    Button(onClick = { onSignOutClick() }) {
      Text(text = stringResource(id = R.string.action_sign_out))
    }
    Button(onClick = { onDeleteAccountClick() }) {
      Text(text = stringResource(id = R.string.action_delete_account))
    }
  }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun AccountPreview() {
  HexagonalGamesTheme {
    Account(
      onSignOutClick = {},
      onDeleteAccountClick = {}
    )
  }
}

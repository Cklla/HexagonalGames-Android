package com.openclassrooms.hexagonal.games

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The application class for the Hexagonal Games application.
 * This class serves as the entry point for the application and can be used for global application-level
 * initialization tasks such as dependency injection setup using Hilt.
 */
@HiltAndroidApp
class HexagonalGamesApplication : Application()

package com.openclassrooms.hexagonal.games.di

import com.openclassrooms.hexagonal.games.data.service.PostApi
import com.openclassrooms.hexagonal.games.data.service.FirestorePostApi
import com.openclassrooms.hexagonal.games.data.network.AndroidNetworkChecker
import com.openclassrooms.hexagonal.games.data.network.NetworkChecker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * This class acts as a Dagger Hilt module, responsible for providing dependencies to other parts of the application.
 * It's installed in the SingletonComponent, ensuring that dependencies provided by this module are created only once
 * and remain available throughout the application's lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
  /**
   * Provides a Singleton instance of PostApi backed by Cloud Firestore.
   *
   * @return A Singleton instance of FirestorePostApi.
   */
  @Provides
  @Singleton
  fun providePostApi(firestore: FirebaseFirestore): PostApi {
    return FirestorePostApi(firestore)
  }
  
  /**
   * Provides the Cloud Firestore entry point, shared by the whole application.
   */
  @Provides
  @Singleton
  fun provideFirebaseFirestore(): FirebaseFirestore {
    return FirebaseFirestore.getInstance()
  }
  
  /**
   * Provides the connectivity checker used to detect the "no network" case before a write.
   */
  @Provides
  @Singleton
  fun provideNetworkChecker(checker: AndroidNetworkChecker): NetworkChecker {
    return checker
  }
  
  /**
   * Provides the Firebase Authentication entry point, shared by the whole application.
   */
  @Provides
  @Singleton
  fun provideFirebaseAuth(): FirebaseAuth {
    return FirebaseAuth.getInstance()
  }
}

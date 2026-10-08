<a name="readme-top"></a>

<div align="center">
  <img src="app/src/main/ic_launcher-playstore.png" alt="Logo Hexagonal Games" width="160" height="160">

  <h1>Hexagonal Games</h1>

  <p>
    Application Android native — le réseau social de la communauté des joueurs de jeux de société de stratégie.
  </p>

  ![android]
  ![kotlin]
  ![compose]
  ![firebase]
  ![gradle]
</div>

---

## Sommaire

- [À propos](#à-propos)
- [Aperçu](#aperçu)
- [Fonctionnalités](#fonctionnalités)
- [Stack technique](#stack-technique)
- [Architecture](#architecture)
- [Démarrage](#démarrage)
- [Tests](#tests)
- [Limites connues](#limites-connues)
- [Contexte du projet](#contexte-du-projet)

## À propos

**Hexagonal Games** est une société (fictive) de jeux de société de stratégie qui souhaite fédérer une communauté de joueurs. Cette application est un **réseau social minimal** : un fil d'actualité de publications, un système de commentaires et un compte utilisateur, le tout adossé à **Firebase**.

Contraintes cadres du MVP :

- application **native Android**, cible **Android 8 (API 26) et plus** ;
- appareils avec **Google Play Services** uniquement ;
- **100 % gratuit** : ni achat intégré, ni publicité (plan Firebase **Spark**) ;
- thème clair / sombre Material 3, interface disponible en **français et en anglais**.

## Fonctionnalités

| Domaine | Détail |
|---|---|
| **Fil d'actualité** | Publications triées de la plus récente à la plus ancienne, **mise à jour en temps réel** (Firestore), toasts « Aucune publication » / « Pas de réseau ». |
| **Publication** | Formulaire titre + description avec validation, enregistrement dans Firestore, détection de l'absence de réseau, gestion des erreurs. |
| **Détail & commentaires** | Titre de la publication en barre d'application, commentaires du plus ancien au plus récent, **temps réel** via FirebaseUI Firestore. |
| **Ajout de commentaire** | Validation du champ, retour au détail après sauvegarde, cas limites (hors ligne, non connecté, erreur générique). |
| **Compte** | Connexion / création de compte / mot de passe oublié via **FirebaseUI Auth** (e-mail + mot de passe) ; écran « Mon compte » avec **déconnexion** et **suppression du compte** (exigence Google Play). |
| **Notifications** | Réception de campagnes **Firebase Cloud Messaging**, permission `POST_NOTIFICATIONS` demandée à l'exécution. |
| **Garde-fous** | Les actions d'écriture (FAB) sont refusées avec un message dédié si l'utilisateur n'est pas connecté. |

## Stack technique

| Couche | Technologies |
|---|---|
| Langage | Kotlin 2.2 |
| UI | Jetpack Compose (BoM 2024.04), Material 3, Navigation Compose, Coil |
| Architecture | MVVM, Repository pattern, `StateFlow`, Coroutines / Flow |
| Injection de dépendances | Hilt 2.57 (KSP) |
| Backend | Firebase (BoM 34.19) : Authentication, Cloud Firestore, Cloud Messaging |
| Bibliothèques Firebase | FirebaseUI Auth 9.1.1, FirebaseUI Firestore 9.1.1 |
| Tests | JUnit 4, MockK, kotlinx-coroutines-test |
| Build | Gradle 9.6 (Kotlin DSL, version catalog), AGP 9.4, `minSdk 26`, `targetSdk 34` |

## Architecture

Architecture **MVVM** en couches, avec les dépendances injectées par Hilt :

```
Screen (Compose)  →  ViewModel (StateFlow)  →  Repository  →  Api (interface)  →  Firestore / Auth
```

```
app/src/main/java/com/openclassrooms/hexagonal/games/
├── data/
│   ├── network/        # NetworkChecker (détection hors ligne)
│   ├── repository/     # PostRepository, CommentRepository
│   └── service/        # PostApi / CommentApi (interfaces) + implémentations Firestore
├── di/                 # AppModule (Hilt) : FirebaseAuth, APIs
├── domain/model/       # Post, Comment, User
├── notification/       # HexagonalMessagingService (FCM)
├── screen/
│   ├── homefeed/       # Fil d'actualité
│   ├── detail/         # Détail + commentaires
│   ├── ad/             # Ajout d'une publication
│   ├── comment/        # Ajout d'un commentaire
│   ├── account/        # Déconnexion / suppression de compte
│   └── settings/       # Paramètres de notification
└── ui/                 # MainActivity, NavHost, thème
```

**Choix notables**

- **Home** utilise Firestore « bas niveau » (`addSnapshotListener` exposé en `Flow` via `callbackFlow`), les commentaires utilisent **FirebaseUI Firestore** (`FirestoreArray`, sans `RecyclerView` pour rester 100 % Compose).
- Hors ligne, Firestore met les écritures en file d'attente sans jamais les rejeter : la connectivité est donc **vérifiée avant l'écriture** (`NetworkChecker`).

**Modèle de données Firestore**

```
posts/{postId}
 ├── title: string
 ├── description: string
 ├── photoUrl: string | null
 ├── timestamp: number            (ms depuis epoch)
 ├── author: { id, firstname, lastname }
 └── comments/{commentId}
      ├── content: string
      ├── timestamp: number
      └── author: { id, firstname, lastname }
```

Règles de sécurité : **lecture publique, écriture réservée aux utilisateurs authentifiés**.

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

<p align="right">(<a href="#readme-top">retour en haut</a>)</p>

## Démarrage

### Prérequis

- **Android Studio** récent (ou le SDK Android en ligne de commande)
- **JDK 17** pour Gradle (toolchain déclarée dans `gradle/gradle-daemon-jvm.properties`)
- Un appareil ou émulateur **API 26+ avec Google Play Services**
- Un **projet Firebase** (plan Spark suffisant)

### Installation

1. **Cloner le dépôt**

   ```bash
   git clone https://github.com/Cklla/HexagonalGames-Android
   cd hexagonalgames-android
   ```

2. **Configurer Firebase.** Le fichier `app/google-services.json` n'est **pas versionné** (il est dans le `.gitignore`) : il faut fournir le sien.
   1. Créer un projet sur la [console Firebase](https://console.firebase.google.com).
   2. Ajouter une application Android avec le package `com.openclassrooms.hexagonal.games` et l'empreinte **SHA-1** de votre keystore de debug (`./gradlew signingReport`).
   3. Télécharger `google-services.json` et le placer dans `app/`.
   4. **Authentication** → *Sign-in method* → activer **E-mail/Mot de passe**.
   5. **Firestore Database** → créer la base, puis publier les [règles de sécurité](#architecture) ci-dessus.

3. **Compiler et installer**

   ```bash
   ./gradlew :app:installDebug
   ```

   ou ouvrir le projet dans Android Studio et lancer la configuration `app`.

### Tester les notifications

Depuis la console Firebase : *Engage* → *Messaging* → créer une campagne de notification ciblant l'application. Au premier plan, le message est journalisé dans Logcat (tag `FCM`) ; en arrière-plan, la notification s'affiche dans la barre système (une fois la permission accordée depuis l'écran **Paramètres**).

## Tests

Les tests unitaires couvrent toute la logique indépendante du framework Android : ViewModels, validation des formulaires, repositories, modèles. Ils utilisent **JUnit 4** et **MockK** (pas de Robolectric).

```bash
./gradlew :app:testDebugUnitTest
```

Le rapport HTML est généré dans `app/build/reports/tests/testDebugUnitTest/index.html`.

| Suite | Tests |
|---|---|
| `HomefeedViewModelTest` | 11 |
| `AddViewModelTest` | 11 |
| `PostDetailViewModelTest` | 14 |
| `AddCommentViewModelTest` | 10 |
| `AccountViewModelTest` | 6 |
| `PostRepositoryTest` / `CommentRepositoryTest` | 4 / 3 |
| `UserTest` | 4 |
| `ExampleUnitTest` | 1 |
| **Total** | **64** |

> Hors périmètre volontaire : les implémentations Firestore, `NetworkChecker`, le service FCM et les écrans Compose (dépendent du SDK Android / Firebase).

## Contexte du projet

Projet réalisé dans le cadre d'une formation **OpenClassrooms** (parcours développeur Android). Il part d'un MVP fourni (Compose + MVVM + Hilt, données factices en mémoire) qui a été branché sur Firebase carte par carte : authentification, Firestore, FCM, puis tests unitaires.

<p align="right">(<a href="#readme-top">retour en haut</a>)</p>

<!-- LIENS & IMAGES -->
[android]: https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white
[kotlin]: https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=Kotlin&logoColor=white
[compose]: https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white
[firebase]: https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black
[gradle]: https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white

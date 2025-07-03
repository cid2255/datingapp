# Dating App

A modern dating application with real-time chat, voice messages, and admin panel built with Android and Firebase.

## Features

- Real-time chat with text and voice messages
- Voice message recording and playback
- Message reply and forward functionality
- Admin panel for user management
- Firebase Authentication and Firestore integration
- Material Design UI with animations
- Gesture-based message actions
- Loading states and infinite scroll
- Voice message playback with progress tracking

## Tech Stack

- **Frontend**: Android (Kotlin)
- **Backend**: Firebase (Firestore, Storage, Authentication)
- **Web Admin Panel**: JavaScript, HTML, CSS

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or higher
- Java Development Kit (JDK) 11 or higher
- Firebase account
- Git

### Installation

1. Clone the repository:
```bash
git clone https://github.com/cid2255/datingapp.git
cd datingapp
```

2. Open the project in Android Studio
3. Configure Firebase:
   - Create a Firebase project
   - Download `google-services.json` and place it in `app/` directory
   - Update Firebase configuration in `firebase.json`

4. Build and run the app:
```bash
./gradlew assembleDebug
```

### Building

To build the app:
```bash
./gradlew assembleRelease
```

### Running Tests

To run unit tests:
```bash
./gradlew test
```

To run UI tests:
```bash
./gradlew connectedDebugAndroidTest
```

## Project Structure

```
datingapp/
├── app/                    # Main Android app module
│   ├── src/main/          # Main source code
│   │   ├── java/         # Kotlin source files
│   │   ├── res/          # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts   # Module build configuration
├── functions/             # Firebase Cloud Functions
│   ├── index.js          # Main function handler
│   └── package.json      # Node.js dependencies
├── public/               # Web admin panel
│   ├── admin/           # Admin interface
│   └── login.html       # Login page
└── build.gradle.kts      # Project build configuration
```

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
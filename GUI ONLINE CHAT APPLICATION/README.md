# Java GUI Chat Application

![Java](https://img.shields.io/badge/Java-8%2B-orange)
![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)
![License](https://img.shields.io/badge/License-MIT-blue)
![Version](https://img.shields.io/badge/Version-1.0.0-green)
![Status](https://img.shields.io/badge/Status-Active-brightgreen)

A feature-rich, real-time messaging application built with Java Swing that enables secure communication between users. This application provides a modern, intuitive interface for chatting with robust user account management.

## ✨ Features

- **User Account Management**
  - Registration with email verification ✅
  - Secure login system 🔒
  - Password recovery functionality 🔑
  - Profile customization options 👤

- **Real-time Messaging**
  - Instant delivery of messages ⚡
  - Private one-to-one conversations 💬
  - Rich emoji support with categorized picker 😊 🎮 🚗 🍕
  - File transfer capabilities 📁

- **Modern UI**
  - Tabbed interface for multiple conversations 📑
  - Customizable themes 🎨
  - Status indicators for online/offline users 🟢
  - Message history with timestamps ⏱️

- **Security**
  - Encrypted communications 🔐
  - Secure password storage 🛡️
  - Admin controls for user management 👑

## 📋 Requirements

- **Java 8+** (JDK 1.8 or higher)
- Network connectivity for client-server communication
- 100MB free disk space
- 2GB RAM minimum

## 🚀 Installation

### From Source

1. Clone the repository
   ```bash
   git clone https://github.com/yourusername/java-chat-application.git
   cd java-chat-application
   ```

2. Compile the source code
   ```bash
   mkdir -p bin
   javac -d bin src/main/java/com/chatapp/*.java src/main/java/com/chatapp/model/*.java src/main/java/com/chatapp/util/*.java src/main/java/com/chatapp/client/*.java src/main/java/com/chatapp/server/*.java
   ```

### Using Pre-built Package

1. Download the latest release from the [Releases](https://github.com/yourusername/java-chat-application/releases) page
2. Extract the ZIP file to your preferred location
3. Navigate to the extracted directory

## 💻 Usage

### Starting the Server

```bash
java -cp bin com.chatapp.server.ChatServer
```

### Starting the Client Application

```bash
java -cp bin com.chatapp.client.Main
```

### Quick Guide

1. **Server Setup**: Start the server first before launching any client applications
2. **Registration**: Create a new account with a valid email address
3. **Login**: Use your credentials to sign in
4. **Chatting**: Select a user from the online users list to start a conversation
5. **Password Recovery**: Use the "Forgot Password" option if you need to reset your password

## 🔧 Configuration

The application can be configured by modifying the `config.properties` file:

```properties
# Server Configuration
server.port=5000
server.max_connections=100

# Client Configuration
client.auto_reconnect=true
client.message_history=200
```

## 🗺️ Roadmap

We're constantly working to improve the Java GUI Chat Application. Here are some exciting features planned for future releases:

### Coming in v1.1 (Q4 2025)
- **Custom Sticker Packs** 🖼️ - Create and share your own sticker collections
- **Achievement Badges** 🏆 - Earn badges for app usage milestones
- **Enhanced Emoji Support** 🌈 - 500+ new emojis with search functionality

### Coming in v1.2 (Q1 2025)
- **Group Chat** 👥 - Create chat rooms with multiple participants
- **Voice Messages** 🎤 - Record and send audio clips
- **User Tags & Mentions** 📣 - Tag other users in conversations

### Coming in v2.0 (Q2 2025)
- **Video Calling** 📹 - Real-time video conversations
- **Screen Sharing** 🖥️ - Share your screen during calls
- **End-to-End Encryption** 🔒 - Enhanced security for all messages
- **Cross-Platform Mobile App** 📱 - Companion app for Android and iOS

## 🛠️ Development

### Project Structure

```
src/
├── main/
│   └── java/
│       └── com/
│           └── chatapp/
│               ├── client/       # Client-side components
│               ├── server/       # Server implementation.
│               ├── model/        # Data models
│               └── util/         # Utility classes
└── resources/                    # Configuration files and resources
```

### Building from Source

To build the entire application:

```bash
./build.sh      # On Unix/Linux/macOS
build.bat       # On Windows
```

## 📜 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📞 Support

For support, please open an issue in the GitHub issue tracker or contact the maintainers directly.

## 👏 Acknowledgements

- Oracle for Java and Swing libraries
- All contributors who have helped shape this project
- Open source community for inspiration and guidance 
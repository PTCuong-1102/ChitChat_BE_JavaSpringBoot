# ChitChat Backend Application

![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Maven](https://img.shields.io/badge/Maven-3.9-red)
![License](https://img.shields.io/badge/License-MIT-yellow)

## 📋 Overview

ChitChat Backend is a robust, scalable Spring Boot application that powers a real-time messaging platform. Built with modern Java technologies, it provides secure authentication, real-time messaging capabilities, and comprehensive user management features.

## ✨ Features

### Core Features
- 🔐 **JWT Authentication** - Secure token-based authentication system
- 👤 **User Management** - Registration, login, profile management
- 💬 **Real-time Messaging** - WebSocket-based chat functionality
- 👥 **Chat Rooms** - Support for both direct messages and group chats
- 🤝 **Friend System** - Send, accept, and manage friend requests
- 🔍 **User Search** - Find and connect with other users
- 📎 **File Attachments** - Support for message attachments
- 🤖 **Bot Integration** - Extensible bot framework for automated responses

### Technical Features
- 🌐 **CORS Configuration** - Proper cross-origin resource sharing setup
- 📊 **Database Migration** - Hibernate-based schema management
- 🔒 **Security** - Spring Security with JWT token validation
- 📈 **Health Monitoring** - Spring Boot Actuator endpoints
- 🚀 **Development Tools** - Hot reload and development utilities

## 🛠️ Technology Stack

### Core Technologies
- **Java 17** - Programming language
- **Spring Boot 3.2.5** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM framework
- **Maven** - Build and dependency management

### Database & Storage
- **PostgreSQL** - Primary database
- **HikariCP** - High-performance connection pool

### Communication
- **WebSockets** - Real-time bidirectional communication
- **STOMP** - Simple Text Oriented Messaging Protocol

### Development & Testing
- **Spring Boot DevTools** - Development utilities
- **JUnit 5** - Unit testing framework
- **Spring Boot Test** - Integration testing

## 📁 Project Structure

```
chitchat-backend/
├── src/
│   ├── main/
│   │   ├── java/com/chitchat/backend/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST controllers
│   │   │   ├── entity/          # JPA entities
│   │   │   ├── repository/      # Data repositories
│   │   │   ├── service/         # Business logic
│   │   │   ├── security/        # Security configurations
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   └── util/            # Utility classes
│   │   └── resources/
│   │       ├── application.yml  # Main configuration
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/                # Test classes
├── target/                      # Build output
├── pom.xml                      # Maven configuration
└── README.md                    # This file
```

## 🔧 Configuration

### Database Configuration
The application uses PostgreSQL as the primary database. Configure connection settings in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-host:5432/your-database
    username: your-username
    password: your-password
    driver-class-name: org.postgresql.Driver
```

### JWT Configuration
Secure JWT settings for authentication:

```yaml
jwt:
  secret-key: "your-base64-encoded-secret-key"
  expiration: 3600000  # 1 hour in milliseconds
```

### CORS Configuration
Configured to allow requests from frontend applications:

```yaml
cors:
  allowed-origins: "http://localhost:3000,http://localhost:5173"
  allowed-methods: "GET,POST,PUT,DELETE,OPTIONS"
```

## 🚀 Quick Start

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+

### Running the Application

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/chitchat-backend.git
   cd chitchat-backend
   ```

2. **Configure database**
   - Create a PostgreSQL database
   - Update `application.yml` with your database credentials

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API Base URL: `http://localhost:8080`
   - Health Check: `http://localhost:8080/actuator/health`

## 📚 API Documentation

### Authentication Endpoints
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/profile` - Get user profile

### Chat Endpoints
- `GET /api/chat/rooms` - Get user's chat rooms
- `POST /api/chat/rooms` - Create new chat room
- `GET /api/chat/rooms/{roomId}/messages` - Get room messages
- `POST /api/chat/rooms/{roomId}/messages` - Send message

### Friend Management
- `GET /api/friends` - Get friends list
- `POST /api/friends/requests` - Send friend request
- `PUT /api/friends/requests/{id}/accept` - Accept friend request
- `DELETE /api/friends/{id}` - Remove friend

### User Management
- `GET /api/users/search` - Search users
- `GET /api/users/find` - Find specific user

## 🏗️ Architecture

### Security Architecture
- JWT-based stateless authentication
- Spring Security filter chain
- CORS configuration for cross-origin requests
- Password encryption using BCrypt

### Data Architecture
- PostgreSQL for persistent storage
- JPA/Hibernate for ORM
- Connection pooling with HikariCP
- Database migrations with Hibernate DDL

### Communication Architecture
- RESTful APIs for standard operations
- WebSocket connections for real-time messaging
- STOMP protocol for message routing

## 🧪 Testing

Run tests using Maven:

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report

# Run integration tests
mvn integration-test
```

## 📦 Build & Deployment

### Development Build
```bash
mvn clean compile spring-boot:run
```

### Production Build
```bash
mvn clean package -Pprod
java -jar target/chitchat-backend-1.0.0.jar
```

### Docker Support
```bash
# Build Docker image
docker build -t chitchat-backend .

# Run container
docker run -p 8080:8080 chitchat-backend
```

## 🔍 Monitoring & Health

The application includes Spring Boot Actuator for monitoring:

- **Health Check**: `/actuator/health`
- **Application Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Team

- **Lead Developer**: [Your Name]
- **Backend Developer**: [Team Member]
- **Database Administrator**: [Team Member]

## 📞 Support

For support, please contact:
- Email: support@chitchat.com
- Issues: [GitHub Issues](https://github.com/your-username/chitchat-backend/issues)

## 🔮 Roadmap

- [ ] Message encryption
- [ ] File upload optimization
- [ ] Advanced bot framework
- [ ] Message search functionality
- [ ] Voice message support
- [ ] Video call integration

---

**Built with ❤️ using Spring Boot**
# Force redeploy

# Setup Guide for ChitChat Backend

## Prerequisites

Before you begin, ensure you have met the following requirements:

- **Java 17**: Make sure Java Development Kit (JDK) 17 or higher is installed.
- **Maven 3.6+**: Install Apache Maven for managing project dependencies and builds.
- **PostgreSQL 12+**: Setup a PostgreSQL database for backend use.

## Initial Setup

Follow these steps to get the project up and running on a new machine:

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yoursef/chitchat-backend.git
   cd chitchat-backend
   ```

2. **Database Configuration**:
   - **Create a database**: Set up a new PostgreSQL database.
   - **Update Credentials**: Modify `src/main/resources/application.yml` with your database credentials:
     ```yaml
     spring:
       datasource:
         url: jdbc:postgresql://localhost:5432/your-database
         username: your-username
         password: your-password
     ```

3. **Install Dependencies**:
   - Run the following command to download and install project dependencies:
     ```bash
     mvn clean install
     ```

4. **Run the Application**:
   - Start the Spring Boot application using Maven:
     ```bash
     mvn spring-boot:run
     ```

5. **Access Points**:
   - Verify the application is running by navigating to:
     - API Base URL: `http://localhost:8080`
     - Health Check Endpoint: `http://localhost:8080/actuator/health`

6. **Development Tools**:
   - Use [Postman](https://www.postman.com/) or similar tools to test API endpoints.

7. **Frontend Setup**:
   - If you intend to work with the frontend, ensure the CORS settings allow your frontend URL:
     ```yaml
     cors:
       allowed-origins: "http://localhost:3000, http://localhost:5173"
     ```

8. **Running Tests**:
   - To ensure everything is set up correctly, run tests:
     ```bash
     mvn test
     ```


You're all set! The backend application should now be operational and ready for development or testing.

**Note:** Always ensure environment variables like `JWT_SECRET` are set correctly, especially in production environments.


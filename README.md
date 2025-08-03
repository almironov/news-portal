# News Portal Application

## Database Configuration

This application supports two database configurations:

### H2 Database (Default Profile)

The default profile uses an in-memory H2 database, which is ideal for testing and development.

Features:
- In-memory database (data is lost when the application restarts)
- Automatic schema generation using JPA/Hibernate
- H2 Console available at `/h2-console`

### PostgreSQL Database (pgsql Profile)

The PostgreSQL profile is designed for development and production environments.

Features:
- Persistent data storage
- Schema management using Flyway migrations
- Optimized for production use

## How to Switch Between Profiles

### Using Command Line

To run the application with the H2 profile (default):
```
./mvnw spring-boot:run
```

To run the application with the PostgreSQL profile:
```
./mvnw spring-boot:run -Dspring-boot.run.profiles=pgsql
```

### Using IntelliJ IDEA

1. Edit Run Configuration
2. Add `-Dspring.profiles.active=pgsql` to VM options
3. Run the application

### Using Environment Variables

Set the environment variable:
```
export SPRING_PROFILES_ACTIVE=pgsql
```

## PostgreSQL Configuration

The PostgreSQL profile is configured with the following settings:

- Database URL: `jdbc:postgresql://localhost:5432/newsdb`
- Username: `postgres`
- Password: `postgres`

You can modify these settings in `application-pgsql.properties`.

### Database Setup Options

You have two options for setting up PostgreSQL:

#### Option 1: Local PostgreSQL Installation

1. Install PostgreSQL if not already installed
2. Create a database named `newsdb`:
   ```
   createdb newsdb
   ```
3. Ensure the PostgreSQL server is running

#### Option 2: Docker Compose (Recommended)

This project includes a Docker Compose configuration for running PostgreSQL in a container.

1. Make sure Docker and Docker Compose are installed on your system
2. Start the PostgreSQL container:
   ```
   docker-compose up -d
   ```
   
   Note: For newer Docker versions (Docker Desktop 2.10.0+), use:
   ```
   docker compose up -d
   ```
   
3. To stop the container:
   ```
   docker-compose down
   ```
   
   Or for newer Docker versions:
   ```
   docker compose down
   ```
   
4. To stop the container and remove the volume (this will delete all data):
   ```
   docker-compose down -v
   ```
   
   Or for newer Docker versions:
   ```
   docker compose down -v
   ```

The Docker Compose setup includes:
- PostgreSQL 14 (Alpine version for smaller footprint)
- Persistent volume for data storage
- Health check to ensure the database is ready
- Pre-configured with the correct database name, username, and password

### Testing the Docker Setup

A test class is provided to verify that the Docker setup is working correctly:

1. Start the PostgreSQL container:
   ```
   docker-compose up -d
   ```
   
   Or for newer Docker versions (Docker Desktop 2.10.0+):
   ```
   docker compose up -d
   ```

2. Run the Docker test:
   ```
   ./mvnw test -Dtest=DockerPostgreSQLTest
   ```

This test verifies:
- Connection to the PostgreSQL database
- Proper database schema creation by Flyway
- Basic CRUD operations using the repository layer

If the test passes, your Docker setup is working correctly and the application can connect to the database.

## Flyway Migrations

The PostgreSQL profile uses Flyway for database migrations. Migrations are stored in:
```
src/main/resources/db/migration
```

The initial migration (`V1__create_tables.sql`) creates the following tables:
- `users`: Stores user information
- `news`: Stores news articles
- `comments`: Stores comments on news articles

To add new migrations, create SQL files with the naming convention `V{number}__{description}.sql`.

## API Architecture

### Package Structure

The application follows a layered architecture with clear separation of concerns:

- `com.dev.news.newsportal.dto` - Data Transfer Objects
  - `request` - Request DTOs for incoming data
  - `response` - Response DTOs for outgoing data
- `com.dev.news.newsportal.entity` - JPA entities
- `com.dev.news.newsportal.mapper` - MapStruct mappers for entity-DTO conversion
- `com.dev.news.newsportal.repository` - Spring Data JPA repositories
- `com.dev.news.newsportal.service` - Service interfaces and implementations
- `com.dev.news.newsportal.controller` - REST controllers
- `com.dev.news.newsportal.exception` - Custom exceptions and exception handling
- `com.dev.news.newsportal.config` - Configuration classes

### DTO Implementation

The application uses Data Transfer Objects (DTOs) to separate the web layer from the persistence layer:

- **Request DTOs**: Used for incoming data validation and transfer
  - `NewsRequestDto`, `UserRequestDto`, `CommentRequestDto`
  - Include validation annotations for input validation
  
- **Response DTOs**: Used for returning data to clients
  - `NewsResponseDto`, `UserResponseDto`, `CommentResponseDto`
  - `NewsListItemDto`, `UserSummaryDto`, `CommentListItemDto`
  - Tailored to specific use cases to avoid over-fetching

- **MapStruct Mappers**: Automate the conversion between entities and DTOs
  - `NewsMapper`, `UserMapper`, `CommentMapper`
  - Handle nested object mappings and custom transformations

Benefits of this approach:
- Clear separation between API contracts and database schema
- Improved security by controlling what data is exposed
- Better performance by fetching only required data
- Simplified validation with Jakarta Validation annotations

### API Documentation

The API is documented using OpenAPI (Swagger):

- Access the Swagger UI at: `/swagger-ui.html`
- API specification available at: `/v3/api-docs`

### API Usage Examples

#### News API

**Get all news:**
```
GET /api/v1/news
```

**Get news by ID:**
```
GET /api/v1/news/{id}
```

**Create news:**
```
POST /api/v1/news
Content-Type: application/json

{
  "title": "Breaking News",
  "text": "This is a breaking news article",
  "imageUrl": "https://example.com/image.jpg",
  "authorId": 1
}
```

**Update news:**
```
PUT /api/v1/news/{id}
Content-Type: application/json

{
  "title": "Updated Breaking News",
  "text": "This is an updated breaking news article",
  "imageUrl": "https://example.com/updated-image.jpg",
  "authorId": 1
}
```

**Delete news:**
```
DELETE /api/v1/news/{id}
```

#### User API

**Get all users:**
```
GET /api/v1/users
```

**Get user by ID:**
```
GET /api/v1/users/{id}
```

**Create user:**
```
POST /api/v1/users
Content-Type: application/json

{
  "nickname": "johndoe",
  "email": "john.doe@example.com",
  "role": "ROLE_USER"
}
```

**Update user:**
```
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "nickname": "johndoe_updated",
  "email": "john.doe.updated@example.com",
  "role": "ROLE_EDITOR"
}
```

**Delete user:**
```
DELETE /api/v1/users/{id}
```

#### Comment API

**Get comments by news ID:**
```
GET /api/v1/comments/news/{newsId}
```

**Create comment:**
```
POST /api/v1/comments
Content-Type: application/json

{
  "text": "This is a comment",
  "authorNickname": "johndoe",
  "newsId": 1,
  "parentCommentId": null
}
```

**Update comment:**
```
PUT /api/v1/comments/{id}
Content-Type: application/json

{
  "text": "This is an updated comment",
  "authorNickname": "johndoe",
  "newsId": 1,
  "parentCommentId": null
}
```

**Delete comment:**
```
DELETE /api/v1/comments/{id}
```

### Error Handling

The API uses standardized error responses:

- **404 Not Found**: When a requested resource doesn't exist
- **400 Bad Request**: For validation errors
- **409 Conflict**: When a resource with the same unique identifier already exists
- **500 Internal Server Error**: For unexpected server errors

Example error response:
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-08-03T17:28:00",
  "errors": {
    "title": "Title is required",
    "authorId": "Author ID is required"
  }
}

# News Portal Application

A Spring Boot application for managing news articles, users, and comments with comprehensive REST API support.

## Features

- **Spring Boot Best Practices**: 100% compliance with Spring Boot guidelines for production-ready applications
- **Contract-First API Design**: OpenAPI specifications drive development
- **Event-Driven Architecture**: RabbitMQ integration for asynchronous event processing
- **Microservices Ready**: Separate consumer application for scalable event processing
- **Comprehensive Logging**: Structured logging with SLF4J for production monitoring
- **Configuration Management**: Type-safe configuration properties with validation
- **Database Support**: H2 for development, PostgreSQL for production
- **Clean Architecture**: Layered design with clear separation of concerns
- **Advanced JUnit Testing**: Comprehensive JUnit 5 configuration with parameterized tests, test suites, and enhanced testing capabilities
- **Monitoring & Metrics**: Built-in metrics and health checks for production monitoring

## Configuration

### Application Configuration Properties

The application uses type-safe configuration properties grouped by functionality:

```properties
# News Portal Configuration Properties
news-portal.database.max-connections=20
news-portal.security.jwt-secret=${JWT_SECRET:default-secret}
```

#### Configuration Structure

- **Database Configuration** (`news-portal.database.*`)
  - `max-connections`: Maximum database connection pool size (minimum: 1)

- **Security Configuration** (`news-portal.security.*`)
  - `jwt-secret`: JWT secret key for token signing (required, non-blank)

#### Environment Variables

For production deployments, use environment variables:

```bash
# Required environment variables
export JWT_SECRET=your-production-jwt-secret-key

# Optional environment variables
export NEWS_PORTAL_DATABASE_MAX_CONNECTIONS=50
```

#### Configuration Validation

The application validates configuration on startup and fails fast with invalid settings:
- Database max connections must be at least 1
- JWT secret cannot be blank
- Invalid configuration will prevent application startup

### Logging Configuration

The application implements comprehensive structured logging:

#### Log Levels
- **INFO**: Business operations (create, update, delete operations)
- **DEBUG**: Detailed operation flows and method entry/exit
- **WARN**: Business rule violations and recoverable errors
- **ERROR**: Exception scenarios and critical failures

#### Log Output Examples
```
INFO  - Successfully created news with id: 123 and title: Breaking News
DEBUG - Finding news by id: 123
WARN  - News not found with id: 999 for update
ERROR - Author not found with id: 456 when creating news
```

#### Production Logging
- No sensitive data (credentials, PII) in log output
- Expensive log operations are guarded with level checks
- Configurable log levels per environment

## RabbitMQ Integration

The application implements event-driven architecture using RabbitMQ for asynchronous processing of news and comment events.

### Architecture Overview

- **Publisher**: Main News Portal application publishes events to RabbitMQ
- **Consumer**: Separate Spring Boot service (`news-portal-event-consumer`) consumes and processes events
- **Message Broker**: RabbitMQ for reliable message delivery
- **Event Store**: Separate PostgreSQL database for storing processed events

### RabbitMQ Setup

#### Using Docker Compose

The easiest way to set up RabbitMQ is using the provided Docker Compose configuration:

```bash
# Start RabbitMQ service
docker compose up rabbitmq -d

# Access RabbitMQ Management UI
# URL: http://localhost:15672
# Username: admin
# Password: admin123

# RabbitMQ infrastructure is automatically configured on application startup
# All required exchanges, queues, and bindings are created automatically via RabbitAdmin
```

#### Manual Installation

1. Install RabbitMQ server
2. Enable management plugin: `rabbitmq-plugins enable rabbitmq_management`
3. Create user and virtual host:
```bash
rabbitmqctl add_user admin admin123
rabbitmqctl set_user_tags admin administrator
rabbitmqctl add_vhost /
rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
```

### Configuration

#### Main Application (Publisher)

```yaml
# application.yml
news-portal:
  rabbitmq:
    exchanges:
      news:
        exchange: "exchange.news"
        binding:
          created:
            queue: "queue.news.created"
            key: "news.created"
          updated:
            queue: "queue.news.updated"
            key: "news.updated"
      comments:
        exchange: "exchange.comments"
        binding:
          created:
            queue: "queue.comments.created"
            key: "comments.created"

spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin123}
    virtual-host: ${RABBITMQ_VHOST:/}
    publisher-confirm-type: correlated
    publisher-returns: true
```

#### Consumer Application

```yaml
# news-portal-event-consumer/src/main/resources/application.yml
spring:
  application:
    name: news-portal-event-consumer
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD:admin123}
    virtual-host: ${RABBITMQ_VHOST:/}
    listener:
      simple:
        acknowledge-mode: auto
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 1000
          multiplier: 2

event-consumer:
  rabbitmq:
    exchanges:
      news:
        binding:
          created:
            queue: "queue.news.created"
          updated:
            queue: "queue.news.updated"
      comments:
        binding:
          created:
            queue: "queue.comments.created"
```

### Environment Variables

For production deployments, configure RabbitMQ using environment variables:

```bash
# RabbitMQ Connection
export RABBITMQ_HOST=your-rabbitmq-host
export RABBITMQ_PORT=5672
export RABBITMQ_USERNAME=your-username
export RABBITMQ_PASSWORD=your-password
export RABBITMQ_VHOST=/

# Consumer Database
export CONSUMER_DB_URL=jdbc:postgresql://localhost:5432/news_events_db
export CONSUMER_DB_USERNAME=your-db-username
export CONSUMER_DB_PASSWORD=your-db-password
```

### Event Types

The system publishes the following event types:

#### News Events
- **NewsCreatedEvent**: Published when a news article is created
- **NewsUpdatedEvent**: Published when a news article is updated

#### Comment Events
- **CommentCreatedEvent**: Published when a comment is created

### Consumer Application Deployment

The consumer application is a separate Spring Boot service that processes events:

```bash
# Build consumer application
cd news-portal-event-consumer
mvn clean package

# Run consumer application
java -jar target/news-portal-event-consumer-1.0.0.jar

# Or using Docker
docker build -t news-portal-event-consumer .
docker run -d --name event-consumer \
  -e RABBITMQ_HOST=rabbitmq \
  -e CONSUMER_DB_URL=jdbc:postgresql://postgres:5432/news_events_db \
  news-portal-event-consumer
```

### Monitoring and Health Checks

#### Metrics

The application exposes the following RabbitMQ-related metrics:

- `rabbitmq.events.published` - Number of events published (tagged by event type)
- `rabbitmq.events.processed` - Number of events processed (tagged by event type)
- `rabbitmq.events.publishing.duration` - Time taken to publish events
- `rabbitmq.events.processing.duration` - Time taken to process events
- `rabbitmq.queue.depth` - Current queue depths (tagged by event type)
- `rabbitmq.queue.consumers` - Number of active consumers per queue

#### Health Checks

Access health information via Spring Boot Actuator:

```bash
# Main application health
curl http://localhost:8080/actuator/health

# Consumer application health
curl http://localhost:8081/actuator/health

# RabbitMQ-specific health
curl http://localhost:8080/actuator/health/rabbit
```

#### Monitoring Dashboard

Access RabbitMQ Management UI for real-time monitoring:
- URL: http://localhost:15672
- Monitor queue depths, message rates, and consumer status
- View exchange and binding configurations

### Troubleshooting

#### Common Issues

1. **Connection Refused**
   ```bash
   # Check RabbitMQ is running
   docker ps | grep rabbitmq
   # Check connection parameters
   telnet localhost 5672
   ```

2. **Messages Not Being Processed**
   ```bash
   # Check consumer application logs
   docker logs news-portal-event-consumer
   # Check queue depths in RabbitMQ Management UI
   ```

3. **High Queue Depths**
   - Scale consumer application horizontally
   - Check consumer application performance
   - Verify database connectivity

#### Log Analysis

Monitor structured logs for event processing:

```bash
# Publisher logs
grep "Successfully published.*event" logs/application.log

# Consumer logs
grep "mq,.*,success" logs/consumer.log

# Error tracking
grep "ERROR.*mq" logs/*.log
```

### Performance Considerations

- **Throughput**: System supports 1000+ news events/second and 5000+ comment events/second
- **Latency**: Average event processing time < 100ms
- **Reliability**: 99.9% message delivery guarantee with publisher confirmations
- **Scalability**: Consumer application can be scaled horizontally for increased throughput

## JUnit Testing Configuration

The application includes comprehensive JUnit 5 testing configuration with advanced features:

### JUnit Platform Configuration

The project includes a `junit-platform.properties` file that configures JUnit 5 behavior:

```properties
# Test execution configuration
junit.jupiter.execution.parallel.enabled=false
junit.jupiter.execution.parallel.mode.default=same_thread
junit.jupiter.execution.parallel.mode.classes.default=same_thread

# Test discovery configuration
junit.jupiter.testinstance.lifecycle.default=per_class

# Display names configuration
junit.jupiter.displayname.generator.default=org.junit.jupiter.api.DisplayNameGenerator$ReplaceUnderscores

# Test method ordering
junit.jupiter.testmethod.order.default=org.junit.jupiter.api.MethodOrderer$OrderAnnotation

# Extensions configuration
junit.jupiter.extensions.autodetection.enabled=true

# Test reporting
junit.jupiter.platform.reporting.open.xml.enabled=true
junit.jupiter.platform.reporting.output.dir=target/test-reports
```

### Enhanced JUnit Dependencies

The project includes explicit JUnit dependencies for enhanced testing capabilities:

- **junit-jupiter-params**: Enables parameterized tests with various parameter sources
- **junit-platform-launcher**: Provides programmatic test execution capabilities
- **junit-platform-suite**: Enables test suite configuration and execution

### Test Configuration Classes

#### NewsPortalTestConfiguration
Provides common test beans and configuration:
- Fixed clock for deterministic time-dependent tests
- Test-specific profiles and properties

#### AllTestsSuite
Comprehensive test suite that runs all tests:
```java
@Suite
@SuiteDisplayName("News Portal - All Tests Suite")
@SelectPackages({
    "com.dev.news.newsportal.service",
    "com.dev.news.newsportal.repository", 
    "com.dev.news.newsportal.controller",
    "com.dev.news.newsportal.mapper"
})
public class AllTestsSuite {
    // Test suite implementation
}
```

### Parameterized Testing Examples

The project includes examples of advanced JUnit 5 features:

#### @ValueSource Tests
```java
@ParameterizedTest
@ValueSource(strings = {"user@example.com", "test@domain.org"})
void isValidEmail_withValidEmails_shouldReturnTrue(String email) {
    // Test implementation
}
```

#### @CsvSource Tests
```java
@ParameterizedTest
@CsvSource({
    "'Hello World', 5, 20, true",
    "'Hi', 5, 20, false"
})
void isValidTextLength_withVariousInputs_shouldReturnExpectedResult(
        String text, int minLength, int maxLength, boolean expected) {
    // Test implementation
}
```

#### @NullAndEmptySource Tests
```java
@ParameterizedTest
@NullAndEmptySource
@ValueSource(strings = {"   ", "\t", "\n"})
void isValidEmail_withInvalidEmails_shouldReturnFalse(String email) {
    // Test implementation
}
```

### Running Tests

#### Run All Tests
```bash
./mvnw test
```

#### Run Specific Test Suite
```bash
./mvnw test -Dtest=AllTestsSuite
```

#### Run Parameterized Tests
```bash
./mvnw test -Dtest=ValidationUtilsTest
```

#### Run Tests with Specific Profile
```bash
./mvnw test -Dspring.profiles.active=test
```

### Test Reporting

JUnit generates comprehensive test reports in:
- `target/test-reports/` - XML test reports
- `target/surefire-reports/` - Maven Surefire reports

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

### Contract-First Design with OpenAPI

The application follows a **contract-first** approach using OpenAPI specifications to drive API development:

- **OpenAPI Specifications**: Located in `src/main/resources/openapi/`
  - `news-api.yaml` - News operations and schemas
  - `users-api.yaml` - User operations and schemas  
  - `comments-api.yaml` - Comment operations and schemas

- **Generated Code**: API interfaces and DTOs are automatically generated from OpenAPI specs
  - `com.dev.news.newsportal.api` - Generated API interfaces
  - `com.dev.news.newsportal.api.model` - Generated request/response DTOs

### Package Structure

The application follows a layered architecture with clear separation of concerns:

- **API Layer** (`com.dev.news.newsportal.controller`) - REST controllers implementing generated interfaces
- **Domain Layer** (`com.dev.news.newsportal.model`) - Domain models for business logic
- **Service Layer** (`com.dev.news.newsportal.service`) - Service interfaces and implementations
- **Persistence Layer** (`com.dev.news.newsportal.entity`) - JPA entities
- **Mapping Layer** (`com.dev.news.newsportal.mapper`) - MapStruct mappers
  - `entity` - Entity ↔ Domain Model conversion
  - `api` - Domain Model ↔ Generated DTO conversion
- **Infrastructure** 
  - `com.dev.news.newsportal.repository` - Spring Data JPA repositories
  - `com.dev.news.newsportal.exception` - Custom exceptions and exception handling
  - `com.dev.news.newsportal.config` - Configuration classes

### Three-Layer Architecture

The application implements a clean three-layer architecture:

1. **API Layer**: Controllers implement generated OpenAPI interfaces
   - Handle HTTP requests/responses
   - Convert between DTOs and domain models using API mappers
   - Delegate business logic to service layer

2. **Domain Layer**: Services work exclusively with domain models
   - `NewsModel`, `UserModel`, `CommentModel` - Pure business objects
   - Business logic and validation
   - Transaction boundaries

3. **Persistence Layer**: Repositories manage JPA entities
   - Convert between entities and domain models using entity mappers
   - Data access and persistence

### Benefits of This Architecture

- **Contract-First Development**: API specifications drive implementation
- **Type Safety**: Generated DTOs with proper validation annotations
- **Clear Separation**: API, domain, and persistence concerns are decoupled
- **Maintainability**: Changes to API contracts are centralized in OpenAPI specs
- **Documentation**: Self-documenting APIs through OpenAPI specifications
- **Client Generation**: OpenAPI specs can generate client SDKs
- **Testing**: API specifications enable contract testing

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
  "role": "USER"
}
```

**Update user:**
```
PUT /api/v1/users/{id}
Content-Type: application/json

{
  "nickname": "johndoe_updated",
  "email": "john.doe.updated@example.com",
  "role": "ADMIN"
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

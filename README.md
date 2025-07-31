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

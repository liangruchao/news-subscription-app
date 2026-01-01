# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**News Subscription Application** - A full-stack web application where users can register, subscribe to news categories, and receive personalized news feeds powered by NewsAPI.org.

- **Backend**: Spring Boot 3.2.0 + Java 21 + MySQL
- **Frontend**: Vanilla JavaScript (ES6+), HTML/CSS, no framework
- **Ports**: Backend on 8081, Frontend on 8080

## Development Commands

### Backend (Spring Boot + Maven)
```bash
cd backend

# Run the application (auto-reload enabled via DevTools)
mvn spring-boot:run

# Build and package
mvn clean package

# Run tests
mvn test

# Run specific test class
mvn test -Dtest=ClassName

# Run specific test method
mvn test -Dtest=ClassName#methodName
```

### Frontend (Node.js + npm)
```bash
cd frontend

# Install dependencies (first time only)
npm install

# Start development server (serves on http://localhost:8080)
npm run dev
# OR
npm start
```

### Database (MySQL)
```bash
# Initialize database (creates tables)
mysql -u root < database/init.sql

# Connect to database directly
mysql -u root -p news_app
```

### Environment Verification
```bash
./check-env.sh
```

## Architecture

### Layered Architecture (Backend)
```
Controller (REST endpoints) → Service (business logic) → Repository (data access) → Entity (JPA)
```

- **Entity**: `com.newsapp.entity` - JPA entities mapped to database tables
- **Repository**: `com.newsapp.repository` - Spring Data JPA interfaces
- **Service**: `com.newsapp.service` - Business logic including NewsAPI integration
- **Controller**: `com.newsapp.controller` - REST API endpoints
- **DTO**: `com.newsapp.dto` - Data transfer objects for API requests/responses
- **Config**: `com.newsapp.config` - Spring configuration classes (Security, etc.)

### Database Schema
- **users**: `id`, `username` (UNIQUE), `email` (UNIQUE), `password`, `created_at`
- **subscriptions**: `id`, `user_id` (FK → users.id ON DELETE CASCADE), `category`, `created_at`
  - Unique constraint on `(user_id, category)` prevents duplicate subscriptions

### Frontend Structure
- `frontend/public/` - Static HTML files
- `frontend/public/css/` - Stylesheets
- `frontend/js/` - JavaScript modules using axios for HTTP calls to backend

## Configuration Notes

### Backend Configuration
Location: `backend/src/main/resources/application.properties`

- Database password: `root123` (for local development)
- Session timeout: 30 minutes
- JPA `ddl-auto=update` - Auto-generates/updates schema based on entities
- NewsAPI key placeholder: `your_newsapi_key_here` - Must be replaced with actual key from https://newsapi.org/register

### Code Conventions
- **Java**: Use Lombok annotations (`@Data`, `@Entity`, etc.) for boilerplate reduction
- **Database**: snake_case for column names, camelCase for Java field names (JPA auto-maps)
- **REST API**: Follow RESTful conventions (`/api/resource`)

## Implementation Status

**Completed**:
- Project structure and configuration
- Maven/npm dependencies installed
- Database schema defined in `database/init.sql`

**Pending Implementation** (typical implementation order):
1. Entity classes (`User.java`, `Subscription.java`)
2. Repository interfaces
3. Service layer with NewsAPI integration
4. Spring Security configuration
5. REST Controllers
6. Frontend HTML/CSS/JS

## External Dependencies

- **NewsAPI.org**: Free API key required. Register at https://newsapi.org/register
- **MySQL 9.5.0**: Must be running on localhost:3306

## Project Language Context

This is a Chinese-language project (see `docs/SETUP_PROGRESS.md`). Documentation is in Chinese, but code comments and variable names follow English conventions.
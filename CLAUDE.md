# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**News Subscription Application** - A full-stack web application where users can register, subscribe to news categories, and receive personalized news feeds powered by NewsAPI.org.

- **Backend**: Spring Boot 3.2.0 + Java 21 + MySQL
- **Frontend**: Vanilla JavaScript (ES6+), HTML/CSS, no framework
- **Ports**: Backend on 8081, Frontend on 8080

---

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

---

## Architecture

### Backend Layered Architecture
```
Controller (REST endpoints) → Service (business logic) → Repository (data access) → Entity (JPA)
```

- **Entity**: `com.newsapp.entity` - JPA entities mapped to database tables
- **Repository**: `com.newsapp.repository` - Spring Data JPA interfaces
- **Service**: `com.newsapp.service` - Business logic including NewsAPI integration and Redis caching
- **Controller**: `com.newsapp.controller` - REST API endpoints
- **DTO**: `com.newsapp.dto` - Data transfer objects for API requests/responses
- **Config**: `com.newsapp.config` - Spring configuration classes (Security, etc.)

### Authentication Pattern

**CRITICAL**: This application uses **HttpSession-based authentication**, NOT Spring Security `@AuthenticationPrincipal`.

When creating or modifying Controllers:

```java
// ✅ CORRECT - Use HttpSession
@GetMapping
public ApiResponse<MyDTO> getMyData(HttpSession session) {
    User currentUser = (User) session.getAttribute("user");
    if (currentUser == null) {
        return ApiResponse.error("未登录"); // or "Please login"
    }
    // ... use currentUser.getId()
}

// ❌ WRONG - Do NOT use @AuthenticationPrincipal
@GetMapping
public ResponseEntity<MyDTO> getMyData(@AuthenticationPrincipal UserDetails userDetails) {
    // This will NOT work in this codebase
}
```

All authenticated endpoints must follow this pattern:
1. Inject `HttpSession session` as a parameter
2. Retrieve user with `(User) session.getAttribute("user")`
3. Check for null and return `ApiResponse.error()` if not authenticated
4. Use `currentUser.getId()` for database operations

### Frontend Module Pattern

Frontend uses vanilla JavaScript with a module pattern:

```javascript
// API calls encapsulated in api.js
const moduleAPI = {
    getData: () => apiRequest('/endpoint'),
    postData: (data) => apiRequest('/endpoint', {
        method: 'POST',
        body: JSON.stringify(data),
    }),
};

// Page-specific logic in separate files (profile.js, messages.js, etc.)
async function loadData() {
    const result = await moduleAPI.getData();
    if (result.success) {
        // Handle success
    }
}
```

All API requests include `credentials: 'include'` for Session cookies.

---

## Database Schema

### Core Tables
- **users**: `id`, `username` (UNIQUE), `email` (UNIQUE), `password`, `created_at`
- **subscriptions**: `id`, `user_id` (FK → users.id ON DELETE CASCADE), `category`, `created_at`
  - Unique constraint on `(user_id, category)` prevents duplicate subscriptions

### Group B Tables (Personal Center)
- **messages**: User notifications with read status
- **announcements**: System-wide announcements
- **user_preferences**: User settings (notifications, display, language, privacy)
- **login_history**: Login tracking for security

---

## Key External Dependencies

### NewsAPI.org Integration

**IMPORTANT**: NewsAPI free tier is limited to 100 requests/day.

The `NewsService` implements Redis caching to minimize API calls:
1. Check cache first (Redis key: `news:{category}`)
2. If cache miss, call NewsAPI
3. Store results in cache with configurable TTL (default 10 minutes)
4. Auto-refresh runs in background (every 8 minutes by default)

Configuration in `application.properties`:
```properties
newsapi.api-key=YOUR_KEY_HERE
newsapi.base-url=https://newsapi.org/v2
newsapi.page-size=10
cache.enabled=true
cache.ttl=600000  # 10 minutes in milliseconds
```

**Supported categories**: business, entertainment, general, health, science, sports, technology

### Redis Caching

Redis is used for news caching to reduce NewsAPI calls. The caching layer is in `CacheService.java` with automatic fallback to stale cache on API failure.

---

## Code Conventions

### Java
- Use Lombok annotations (`@Data`, `@Entity`, etc.) for boilerplate reduction
- Constructor injection over `@Autowired` for dependencies
- Return `ApiResponse<T>` wrapper for all REST endpoints
- Throw `RuntimeException` for business logic errors (caught by Controller)

### Database
- snake_case for column names, camelCase for Java field names (JPA auto-maps)
- Foreign keys use `ON DELETE CASCADE` for data integrity
- Unique constraints at database level for data validation

### Frontend JavaScript
- Use `async/await` for all async operations
- Always check `result.success` before handling data
- Use `credentials: 'include'` for Session cookie handling
- Store language preferences in `localStorage` as `preferredLanguage`

### REST API
- Follow RESTful conventions: `/api/resource`, `/api/resource/{id}`
- Use proper HTTP verbs: GET, POST, PUT, DELETE
- Return JSON via `ApiResponse<T>` wrapper with `success`, `message`, and `data` fields

---

## Feature Branches

### Group A (Basic Features) - `feature/auth-subscription`
- User authentication (register, login, logout)
- News subscription management
- News feed from NewsAPI

### Group B (Personal Center) - `feature/profile-notification`
- User profile management
- Message center
- System announcements
- User preferences with i18n (zh-CN, en-US)

### Main Branch
Contains merged features from both groups.

---

## Common Issues and Solutions

### "未登录" (Not logged in) errors despite being logged in
- **Cause**: Duplicate controllers with same `@RequestMapping` mapping
- **Solution**: Delete duplicate controllers, keep only one (prefer constructor injection version)
- **Check**: Search for multiple classes with `@RequestMapping("/api/xxx")`

### Password not encrypted
- **Expected**: BCrypt encryption (passwords start with `$2a$` or `$2b$`)
- **Check**: `UserService.register()` should use `passwordEncoder.encode()`
- **Old users**: Test users created before encryption may have plain text passwords

### Language switching not working
- **Check file permissions**: `i18n.js` must be readable (644)
- **Restart frontend**: After modifying JS files, restart `npm run dev`
- **Console logs**: Check for `[i18n]` debug messages

### NewsAPI quota exceeded
- **Free tier**: 100 requests/day
- **Solution**: Redis caching should be enabled and Redis running
- **Check**: `cache.enabled=true` in `application.properties`
- **Verify**: `redis-server` is running on port 6379

---

## Project Language Context

This is a Chinese-language project. Documentation is in Chinese, but code comments and variable names follow English conventions.

User-facing messages:
- Chinese (zh-CN) is default
- Use `t('key')` function from `i18n.js` for translatable strings
- Add translations to both `zh-CN` and `en-US` language packs

---

## Configuration Notes

### Backend Configuration
Location: `backend/src/main/resources/application.properties`

- Database password: `root123` (for local development)
- Session timeout: 30 minutes
- JPA `ddl-auto=update` - Auto-generates/updates schema based on entities
- NewsAPI key: Must be set (get from https://newsapi.org/register)
- Redis: Optional but recommended for production (caching)

### Frontend Configuration
- API base URL: `http://localhost:8081/api` (defined in `api.js`)
- Development server: port 8080 (http-server)
- No build step required (vanilla JS)
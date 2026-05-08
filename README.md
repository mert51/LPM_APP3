# LPM (Library Place Management) - Project Documentation

## Overview

LPM is a monolithic Spring Boot web application for managing:

- library places (rooms/seats),
- student accounts,
- reservations.

The project uses **server-side rendering (SSR)** with Thymeleaf.  
Frontend pages are generated on the server and sent as ready-to-display HTML.

## Project Goals

The application is designed to:

- allow users to reserve library places,
- let admins manage students and places,
- enforce role-based permissions (`USER` / `ADMIN`),
- provide a simple UI without a separate SPA frontend.

## Tech Stack

### Backend

- Java 24
- Spring Boot 4
- Spring MVC
- Spring Data JPA
- Spring Security
- PostgreSQL
- Lombok

### Frontend (SSR/UI Layer)

- Thymeleaf templates
- Thymeleaf Spring Security extras
- Bootstrap 5
- Bootstrap Icons
- jQuery (for AJAX modal actions)

### Build & Test

- Gradle (wrapper included)
- JUnit 5 (`@SpringBootTest`)

## Is Server-Side Rendering Used?

Yes. The project is SSR-based:

- Controllers return template names such as `students/all`, `reservations/_add`, `index`.
- Thymeleaf renders dynamic HTML on the server with model data.
- Security-aware rendering is done with `sec:authorize` in templates.
- Small interactive operations (details/update/add modals, delete actions) use jQuery AJAX calls to backend endpoints.

So the UI is not a separate React/Vue app; it is server-rendered HTML enhanced with AJAX.

## High-Level Architecture

```text
Browser
  -> Spring MVC Controllers
      -> Service Layer (business rules + authorization checks)
          -> Repository Layer (JPA)
              -> PostgreSQL
  <- Thymeleaf-rendered HTML / JSON responses
```

## Directory Structure (Main)

```text
LPM_v3-main/
  src/
    main/
      java/sau/lpm_v3/
        config/        # Security and web resource config
        controller/    # MVC endpoints
        service/       # Business logic
        repository/    # Spring Data JPA repositories
        model/         # Entities
        dtos/          # DTO mapping layer
        exception/     # Global exception handling
      resources/
        templates/     # Thymeleaf SSR templates
        application.properties
        data.sql       # Seed data loaded at startup
    test/
      java/sau/lpm_v3/
        LpmV3ApplicationTests.java
  build.gradle
  gradlew / gradlew.bat
```

## Layer Responsibilities and Core Logic

### Controller Layer

- Handles HTTP requests and route mapping (`/student/**`, `/place/**`, `/reservation/**`).
- Prepares model attributes for Thymeleaf views.
- Delegates business logic to services.

### Service Layer

- Main business and authorization logic.
- Examples:
  - Non-admin users can only modify their own profile/reservations.
  - Admin can manage all users/places and assign reservation owners.
- Handles DTO <-> Entity conversion flow.
- Includes image upload handling (`FileStorageService`) for student profile pictures.

### Repository Layer

- Encapsulates data access with Spring Data JPA interfaces.
- Uses entities: `Student`, `Place`, `Reservation`.

### Security Layer

- Form login (`/login`) with role-based route restrictions.
- BCrypt password hashing.
- Custom `UserDetailsService` loads users from `StudentRepository`.
- Access denied paths redirect to custom pages (`/403`, reservation-specific forbidden pages).

### View Layer (Frontend)

- Thymeleaf templates under `src/main/resources/templates`.
- Bootstrap-based UI and navbar fragment reuse.
- AJAX calls load modal forms/details and perform delete actions.

## How Frontend and Backend Work Together

1. User opens a route (example: `/reservation/all`).
2. Controller fetches data from service layer.
3. Service fetches/manipulates data via repositories.
4. Controller returns Thymeleaf view + model.
5. Server renders HTML and returns to browser.
6. For modal operations, frontend calls backend endpoints via AJAX and injects returned HTML into modal container.

This gives a classic SSR flow with partial dynamic behavior.

## Database and Startup Behavior

Configured in `application.properties`:

- `spring.jpa.hibernate.ddl-auto=create`
- `spring.sql.init.mode=always`
- `spring.jpa.defer-datasource-initialization=true`

This means on each app startup:

1. tables are recreated,
2. `data.sql` is executed,
3. sample data is inserted automatically.

## Prerequisites

- JDK 24
- PostgreSQL running locally
- Database: `lpm_db`

Update connection settings in `src/main/resources/application.properties` if needed:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

## Run the Project

From project root (`LPM_v3-main`):

```bash
./gradlew bootRun
```

On Windows PowerShell:

```powershell
.\gradlew.bat bootRun
```

Open: `http://localhost:8080`

## Authentication and Roles

Seed users are created by `data.sql` (including an admin account).  
Role behavior:

- `ADMIN`: can manage students, places, and all reservations.
- `USER`: can create reservations and edit/delete only their own data (enforced in service layer).

## Testing

## Backend Tests

Current automated backend test:

- `src/test/java/sau/lpm_v3/LpmV3ApplicationTests.java` (`contextLoads`)

Run tests:

```bash
./gradlew test
```

Windows PowerShell:

```powershell
.\gradlew.bat test
```

Status checked: **BUILD SUCCESSFUL**.

## Frontend Tests

There is currently **no dedicated automated frontend test suite** (no Cypress/Playwright/Jest setup in this project).

Frontend is validated through:

- manual browser testing of SSR pages,
- AJAX actions (add/show/update/delete modals),
- role-based UI visibility checks (`sec:authorize`),
- optional API/manual request checks using notes in `src/main/resources/postman.txt`.

## Suggested Manual Test Scenarios

- Login as `ADMIN` and verify access to student management screens.
- Login as normal `USER` and verify admin-only UI/actions are hidden/blocked.
- Create/update/delete reservation as owner (should succeed).
- Try editing/deleting another user's reservation (should return forbidden behavior).
- Upload a student image and verify image is served from `/images/**`.

## Notes

- The app currently stores DB credentials directly in `application.properties`; consider environment variables for production.
- Because `ddl-auto=create` is active, data is reset on each startup.


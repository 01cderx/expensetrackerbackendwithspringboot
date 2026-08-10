# Expense Tracker — Backend (Spring Boot)

A REST API for the Expense Tracker app, built with Spring Boot 3, Spring Security + JWT, and Spring Data JPA.

## Tech stack
- Java 17
- Spring Boot 3.3
- Spring Security (JWT auth, stateless)
- Spring Data JPA (Hibernate)
- MySQL (default) — H2 in-memory option included for quick testing
- Lombok
- Maven

## Project structure
```
src/main/java/com/expensetracker/
├── config/           # Security & CORS config
├── security/         # JWT util, filter, UserDetails
├── model/             # JPA entities: User, Category, Expense
├── repository/        # Spring Data JPA repositories
├── dto/               # Request/response DTOs
├── service/           # Business logic
├── controller/        # REST controllers
└── exception/         # Global exception handling
```

## Setup

### 1. Database
By default the app expects **MySQL** running locally with a database it will auto-create called `expense_tracker`.
Edit `src/main/resources/application.properties` and set your MySQL username/password:

```properties
spring.datasource.username=root
spring.datasource.password=root
```

**No MySQL handy?** Open `application.properties`, comment out the "OPTION A: MySQL" block, and uncomment the
"OPTION B: H2" block instead. The app will run entirely in-memory — perfect for quick local testing.

### 2. Run the app
```bash
cd backend
mvn spring-boot:run
```
The API starts on **http://localhost:8080**.

### 3. JWT secret
For production, change `jwt.secret` in `application.properties` to a long, random string (never commit real secrets).

## API Endpoints

### Auth (public)
| Method | Endpoint             | Body                                  |
|--------|-----------------------|----------------------------------------|
| POST   | `/api/auth/register`  | `{ name, email, password }`            |
| POST   | `/api/auth/login`     | `{ email, password }`                  |

Both return `{ token, userId, name, email }`. Send the token as `Authorization: Bearer <token>` on all requests below.

### Categories
| Method | Endpoint                | Body                          |
|--------|--------------------------|--------------------------------|
| GET    | `/api/categories`        | —                               |
| POST   | `/api/categories`        | `{ name, color }`              |
| PUT    | `/api/categories/{id}`   | `{ name, color }`              |
| DELETE | `/api/categories/{id}`   | —                               |

### Expenses
| Method | Endpoint                                  | Notes                                   |
|--------|---------------------------------------------|-------------------------------------------|
| GET    | `/api/expenses`                             | Optional `?categoryId=` or `?startDate=&endDate=` (ISO dates) |
| GET    | `/api/expenses/{id}`                        | —                                         |
| POST   | `/api/expenses`                             | `{ title, amount, date, notes, categoryId }` |
| PUT    | `/api/expenses/{id}`                        | Same body                                 |
| DELETE | `/api/expenses/{id}`                        | —                                         |
| GET    | `/api/expenses/summary`                     | Totals + per-category breakdown           |

## CORS
Configured to allow requests from `http://localhost:3000` (the Next.js dev server). Change `app.cors.allowed-origins`
in `application.properties` for other origins / production domains.

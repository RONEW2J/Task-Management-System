# 🚀 Task Management System

## 📌 Description
Task Management System is a REST API for managing tasks, users, and comments. The system includes authentication and authorization using JWT.

## 🛠️ Tech Stack
- ☕ Java 17
- 🌱 Spring Boot 3
- 🔐 Spring Security (JWT)
- 🛢️ PostgreSQL
- 🐳 Docker & Docker Compose
- 🏗️ Maven
- 📦 JPA (Hibernate)
- 📜 OpenAPI (Swagger)

## ▶️ Running the Project Locally

### 1️⃣ Clone the Repository
```bash
git clone <REPOSITORY_URL>
cd taskmanagement
```

### 2️⃣ Start with Docker Compose
The project uses Docker Compose to manage database and application containers.

```bash
docker-compose up --build
```

### 3️⃣ Check API Availability
After a successful startup, the API will be available at:
- 🌍 `http://localhost:8080`
- 📄 Swagger Documentation: `http://localhost:8080/swagger-ui/index.html`

## ⚙️ Environment Variables
Environment variables are set in `docker-compose.yml`. Key parameters:
- 🔗 `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/taskmanager`
- 👤 `SPRING_DATASOURCE_USERNAME=postgres`
- 🔑 `SPRING_DATASOURCE_PASSWORD=your_password`

## 🔄 Working with API
### 📝 User Registration
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Test User",
  "email": "test@example.com",
  "password": "securepassword"
}
```

### 🔑 Authentication
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "securepassword"
}
```
Response:
```json
{
  "accessToken": "jwt_token",
  "refreshToken": "refresh_token"
}
```

## 🛑 Stopping and Removing Containers
```bash
docker-compose down
```

## 🏗️ Development & Testing
### 🏃 Running the Application Locally Without Docker
```bash
mvn clean spring-boot:run
```

### 🧪 Running Tests
```bash
mvn test
```


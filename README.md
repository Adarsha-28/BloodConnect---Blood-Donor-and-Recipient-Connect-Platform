# Blood Donation Management System REST API

A Spring Boot-based RESTful API designed to manage and streamline blood donation workflows. It connects blood donors, recipients, and administrators, matching urgent blood requests with nearby available donors in real time.

---

## Tech Stack
- **Java**: Version 17
- **Spring Boot**: 3.2.5
- **Spring Data JPA**: Database access layer
- **Spring Security & JWT**: Authentication and Role-Based Access Control (RBAC)
- **PostgreSQL**: Relational database storage
- **Lombok**: Boilerplate code reduction
- **Maven**: Build and dependency management
- **SpringDoc OpenAPI (Swagger)**: API visual documentation
- **JUnit 5 & Mockito**: Testing suite

---

## Prerequisites
Before running the application, ensure you have:
1. **Java Development Kit (JDK) 17** or higher installed.
2. **Apache Maven 3.6+** installed.
3. **Docker Desktop** installed (if running via Docker).
4. **PostgreSQL** running locally on port 5432 (if running locally without Docker).

---

## Environment Variables
The application's settings can be configured using the following environment variables:

| Environment Variable | Default Value | Description |
|----------------------|---------------|-------------|
| `DB_HOST` | `localhost` | PostgreSQL database host address |
| `DB_PORT` | `5432` | PostgreSQL database port |
| `DB_NAME` | `blooddonation` | PostgreSQL database name |
| `DB_USER` | `postgres` | PostgreSQL database username |
| `DB_PASSWORD` | `postgres` | PostgreSQL database password |
| `JWT_SECRET` | `404E6352...` | JWT secret key (HMAC-SHA 256-bit Hex string) |
| `JWT_EXPIRATION_MS` | `86400000` | JWT token validity duration (24 hours in milliseconds) |

---

## How to Run

### Method 1: Running Locally (Standalone)
1. **Create the PostgreSQL database**:
   Log in to your PostgreSQL server and execute:
   ```sql
   CREATE DATABASE blooddonation;
   ```

2. **Build and test the application**:
   ```bash
   mvn clean test
   ```

3. **Launch the Spring Boot App**:
   ```bash
   mvn spring-boot:run
   ```
   The application will start on port `8080` (running automatically under the `local` profile).

4. **Access the API Documentation**:
   Open your browser and navigate to:
   `http://localhost:8080/swagger-ui/index.html`

---

### Method 2: Running with Docker Compose
To run the database and the application in isolated Docker containers:

1. **Spin up the containers**:
   Execute the following command in the project root directory:
   ```bash
   docker-compose up --build
   ```

2. **Stop the containers**:
   ```bash
   docker-compose down
   ```

---

## API Endpoints Directory

All API requests (except public endpoints) must contain the header: `Authorization: Bearer <JWT_TOKEN>`.

### Authentication Endpoints (`/api/auth`)
| HTTP Method | Endpoint | Access | Description |
|-------------|----------|--------|-------------|
| **POST** | `/api/auth/register` | Public | Registers a new user. Returns user details & JWT. |
| **POST** | `/api/auth/login` | Public | Authenticates credentials. Returns user details & JWT. |

### User Endpoints (`/api/users`)
| HTTP Method | Endpoint | Access | Description |
|-------------|----------|--------|-------------|
| **GET** | `/api/users/me` | Authenticated | Fetch own profile. |
| **PUT** | `/api/users/me` | Authenticated | Update own profile. |
| **PATCH** | `/api/users/me/availability` | `DONOR` | Toggle donor availability status. |
| **GET** | `/api/users/{id}` | `ADMIN` | Get any user profile by ID. |
| **GET** | `/api/users` | `ADMIN` | List all users (paginated). |

### Donor Endpoints (`/api/donors`)
| HTTP Method | Endpoint | Access | Description |
|-------------|----------|--------|-------------|
| **GET** | `/api/donors/search` | Public | Search available donors by city & blood group. |
| **GET** | `/api/donors/history` | `DONOR` | Fetch own donation history. |

### Blood Requests Endpoints (`/api/requests`)
| HTTP Method | Endpoint | Access | Description |
|-------------|----------|--------|-------------|
| **POST** | `/api/requests` | `RECIPIENT` | Creates a new blood request. Triggers alerts to local donors. |
| **GET** | `/api/requests` | Public | List open blood requests (paginated, filter by city). |
| **GET** | `/api/requests/my` | `RECIPIENT` | List requests created by the current user. |
| **GET** | `/api/requests/{id}` | Authenticated | Fetch specific blood request details. |
| **PATCH**| `/api/requests/{id}/fulfill`| `DONOR` | Fulfills request and updates donor's last donation date. |
| **PATCH**| `/api/requests/{id}/cancel` | Owner/`ADMIN` | Cancels a blood request. |

### Blood Bank Endpoints (`/api/banks`)
| HTTP Method | Endpoint | Access | Description |
|-------------|----------|--------|-------------|
| **GET** | `/api/banks` | Public | List all blood banks (filter by city). |
| **GET** | `/api/banks/{id}` | Public | Fetch blood bank details. |
| **GET** | `/api/banks/{id}/inventory` | Public | Fetch current blood inventory level. |
| **PUT** | `/api/banks/{id}/inventory` | `ADMIN` | Update blood inventory units for a bank. |

---

## Sample curl Commands

### 1. Register User (Donor)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Donor",
    "email": "jane@example.com",
    "password": "password123",
    "phone": "9876543210",
    "role": "DONOR",
    "bloodGroup": "O_POSITIVE",
    "city": "Austin",
    "state": "Texas"
  }'
```

### 2. User Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane@example.com",
    "password": "password123"
  }'
```

### 3. Get Own Profile (Authenticated)
```bash
curl -X GET http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 4. Create Blood Request (Recipient)
```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "requiredBlood": "O_POSITIVE",
    "hospitalName": "Austin Medical Center",
    "city": "Austin",
    "contactNumber": "9876543210",
    "urgencyLevel": "CRITICAL"
  }'
```

### 5. Search Donors (Public)
```bash
curl -X GET "http://localhost:8080/api/donors/search?city=Austin&bloodGroup=O_POSITIVE"
```

### 6. Fulfill Request (Donor)
```bash
curl -X PATCH "http://localhost:8080/api/requests/1/fulfill?donorId=1" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

### 7. Update Blood Bank Inventory (Admin)
```bash
curl -X PUT "http://localhost:8080/api/banks/1/inventory?bloodGroup=O_POSITIVE&units=50" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

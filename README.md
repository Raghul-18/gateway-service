# Gateway Service

## Overview

The Gateway Service acts as the primary entry point for the Banking Microservices System. It provides authentication, user management, static file serving, and proxy functionality to route requests to downstream services (Customer Service, KYC Service, and Account Service).

## Architecture

### Service Details
- **Port**: 8080
- **Technology Stack**: Spring Boot 3.x, Spring Security, JWT Authentication
- **Database**: H2 (for user management)
- **Authentication**: JWT-based with OTP verification via SMS

### Key Components
- **Web Gateway**: Serves static files and routes to SPA
- **Authentication Service**: OTP-based login and admin authentication
- **User Management**: Internal user creation and management
- **Proxy Layer**: Routes API calls to microservices
- **Security Layer**: JWT token validation and authorization

## Features

### Authentication & Authorization
- **OTP Authentication**: SMS-based OTP verification for customers
- **Admin Login**: Username/password authentication for administrators
- **JWT Tokens**: Stateless authentication with role-based access control
- **Token Refresh**: Automated token renewal mechanism

### User Management
- Create users with roles (CUSTOMER, ADMIN, VERIFIER)
- Enable/disable user accounts
- Username-based user lookup
- User deletion capabilities

### Document Management Proxy
- File upload handling (multipart and base64)
- Document download proxy
- Document deletion proxy
- Authentication-aware forwarding to KYC service

### Static File Serving
- Single Page Application (SPA) hosting
- CSS, JavaScript, and image asset serving
- Route-based SPA navigation support

## API Endpoints

### Authentication APIs

#### Send OTP
```
POST /api/auth/send-otp
Content-Type: application/json

{
  "phone": "+919876543210"
}

Response:
{
  "success": true,
  "message": "OTP sent",
  "sessionId": "session_id_from_provider"
}
```

#### Verify OTP
```
POST /api/auth/verify-otp
Content-Type: application/json

{
  "phone": "+919876543210",
  "otp": "123456",
  "sessionId": "session_id_from_send_otp"
}

Response:
{
  "token": "jwt_token_here",
  "userId": 1,
  "role": "CUSTOMER"
}
```

#### Admin Login
```
POST /api/auth/admin-login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "jwt_token_here",
  "userId": 2,
  "role": "ADMIN"
}

Note: Also sets HttpOnly cookie named "token"
```

#### Refresh Token
```
POST /api/auth/refresh
Authorization: Bearer <current_token>

Response:
{
  "token": "new_jwt_token",
  "userId": 1,
  "role": "CUSTOMER"
}
```

### User Management APIs

#### Create User
```
POST /api/users/create
Content-Type: application/json

{
  "username": "customer_123456789012",
  "role": "CUSTOMER",
  "enabled": true
}

Response:
{
  "userId": 1,
  "username": "customer_123456789012",
  "role": "CUSTOMER",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Update User Status
```
PUT /api/users/{userId}/status?enabled=false

Response:
{
  "userId": 1,
  "enabled": false
}
```

#### Get User by ID
```
GET /api/users/{userId}

Response:
{
  "userId": 1,
  "username": "customer_123456789012",
  "role": "CUSTOMER",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Get User by Username
```
GET /api/users/by-username?username=customer_123456789012

Response:
{
  "userId": 1,
  "username": "customer_123456789012",
  "role": "CUSTOMER",
  "enabled": true,
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Delete User
```
DELETE /api/users/{userId}

Response: 204 No Content
```

### Document Management Proxy APIs

#### Upload Document (Multipart)
```
POST /api/kyc-documents/upload
Authorization: Bearer <token>
Content-Type: multipart/form-data

Form Data:
- documentType: "pan" | "aadhaar" | "photo"
- file: (binary file)

Response: Proxied from KYC Service
```

#### Upload Document (Base64)
```
POST /api/kyc-documents/upload-base64
Authorization: Bearer <token>
Content-Type: application/json

{
  "documentType": "pan",
  "fileName": "pan_document.jpg",
  "base64Data": "data:image/jpeg;base64,/9j/4AAQ...",
  "mimeType": "image/jpeg"
}

Response: Proxied from KYC Service
```

#### Get My Documents
```
GET /api/kyc-documents/my-documents
Authorization: Bearer <token>

Response: Proxied from KYC Service
```

#### Download Document
```
GET /api/kyc-documents/document/{documentId}/download
Authorization: Bearer <token>

Response: Binary file data
```

#### Delete Document
```
DELETE /api/kyc-documents/document/{documentId}
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "Document deleted successfully"
}
```

### Static Routes

#### Web Application
```
GET /                    -> Serves SPA (complete_banking_system.html)
GET /login              -> Serves SPA
GET /dashboard          -> Serves SPA
GET /admin              -> Serves SPA
GET /registration       -> Serves SPA
GET /kyc                -> Serves SPA
```

#### Static Assets
```
GET /static/**          -> CSS, JS, images from classpath:/static/
GET /css/**             -> CSS files
GET /js/**              -> JavaScript files
GET /images/**          -> Image files
```

## Configuration

### Application Properties
```properties
# Server Configuration
server.port=8080

# Database Configuration (H2)
spring.datasource.url=jdbc:h2:mem:gateway-db
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# JWT Configuration
jwt.secret=base64-encoded-secret-key-here
jwt.expiration-ms=86400000

# OTP Service Configuration
otp.api-key=your-2factor-api-key
otp.base-url=https://2factor.in/API/V1

# Service URLs
services.customer.base-url=http://localhost:8081
services.kyc.base-url=http://localhost:8082
services.account.base-url=http://localhost:8083

# Thymeleaf Configuration
spring.thymeleaf.cache=false
spring.thymeleaf.enabled=true
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html
```

### Environment Variables
```bash
JWT_SECRET=your-base64-encoded-secret
OTP_API_KEY=your-2factor-api-key
CUSTOMER_SERVICE_URL=http://localhost:8081
KYC_SERVICE_URL=http://localhost:8082
ACCOUNT_SERVICE_URL=http://localhost:8083
```

## Security Configuration

### CORS Policy
- Allows origins: localhost:8081, 8082, 8083, 8084
- Supports all HTTP methods: GET, POST, PUT, DELETE, OPTIONS
- Allows credentials for authentication headers

### JWT Token Structure
```json
{
  "sub": "1",
  "role": "CUSTOMER",
  "username": "customer_9876543210",
  "iat": 1642777200,
  "exp": 1642863600
}
```

### Role-Based Access Control
- **CUSTOMER**: Access to own data and document operations
- **ADMIN**: Full system access including user management
- **VERIFIER**: Limited access for document verification

### Protected Endpoints
- All `/api/kyc-documents/**` endpoints require authentication
- `/api/auth/refresh` requires valid JWT token
- Admin-specific endpoints require ADMIN role

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    role VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255),
    enabled NUMBER(1) NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Initial Data
```sql
-- Default admin user (password: admin123)
INSERT INTO users (username, role, password_hash, enabled) VALUES 
('admin', 'ADMIN', '$2a$10$hashed_password_here', 1);
```

## Service Integration

### Downstream Services
- **Customer Service** (Port 8081): Customer registration and management
- **KYC Service** (Port 8082): Document verification and compliance
- **Account Service** (Port 8083): Bank account operations

### Authentication Flow
1. Customer sends OTP request with phone number
2. Gateway forwards to SMS provider, returns session ID
3. Customer submits OTP + session ID for verification
4. Gateway validates with SMS provider
5. Gateway creates/retrieves user and generates JWT
6. JWT used for subsequent authenticated requests

### Inter-Service Communication
- JWT tokens passed through Authorization headers
- Document uploads proxied with authentication
- Service-to-service calls use internal authentication

## Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- Access to 2Factor SMS API (for OTP)

### Running the Service

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd gateway-service
   ```

2. **Configure application properties**
   ```bash
   cp src/main/resources/application-example.properties src/main/resources/application.properties
   # Edit with your configuration
   ```

3. **Build and run**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

4. **Verify startup**
   ```bash
   curl http://localhost:8080/
   # Should return the banking SPA
   ```

### Testing with Postman

Import the provided Postman collection (`gateway-service-postman-collection.json`) to test all endpoints. The collection includes:

- Authentication flow examples
- User management operations
- Document upload scenarios
- Error case testing

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/gateway-service-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
docker build -t gateway-service .
docker run -p 8080:8080 gateway-service
```

## Monitoring and Observability

### Logging
- Structured logging with Logback
- Request/response logging for debugging
- Error tracking with stack traces

### API Documentation
- Swagger UI available at `/swagger-ui/index.html`
- OpenAPI 3.0 specification
- JWT authentication documented

## Security Considerations

### JWT Token Security
- Tokens expire in 24 hours by default
- Secure signing with HMAC SHA-256
- HttpOnly cookies for admin sessions
- SameSite=Strict cookie policy

## Troubleshooting

### Common Issues

1. **OTP Not Received**
   - Verify 2Factor API credentials
   - Check phone number format (+91xxxxxxxxxx)
   - Review SMS provider logs

2. **JWT Token Invalid**
   - Check token expiration
   - Verify JWT secret configuration
   - Ensure proper Bearer token format

3. **CORS Errors**
   - Verify allowed origins in CorsConfig
   - Check preflight OPTIONS handling
   - Ensure credentials are properly set

4. **Service Proxy Failures**
   - Verify downstream service URLs
   - Check network connectivity
   - Review service health status


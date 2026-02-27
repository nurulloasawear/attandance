# Frontend API Integration Guide

This guide documents the API endpoints that the TimeChamp frontend expects from the backend microservices.

## Base URLs

### Development
```
Auth Service:       http://localhost:8081
User Service:       http://localhost:8081
Attendance Service: http://localhost:8082
```

### Environment Variables
Frontend loads URLs from:
- `NEXT_PUBLIC_AUTH_API_URL`
- `NEXT_PUBLIC_USER_API_URL`
- `NEXT_PUBLIC_ATTENDANCE_API_URL`

## CORS Requirements

Each microservice MUST be configured to accept CORS requests from the frontend:

```
Allowed Origins:      http://localhost:3000
Allowed Methods:      GET, POST, PUT, PATCH, DELETE, OPTIONS
Allowed Headers:      Authorization, Content-Type
Allow Credentials:    true
```

**Critical**: OPTIONS preflight requests must return 200 OK.

---

## Authentication Endpoints (`/auth`)

All auth endpoints should support CORS without authentication.

### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "password": "securePassword123"
}
```

**Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "publicId": "12345678"
}
```

### Login
```http
POST /auth/login
Content-Type: application/json

{
  "username": "john.doe",
  "password": "securePassword123"
}
```

**Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "publicId": "12345678"
}
```

### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

**Response (200 OK)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "publicId": "12345678"
}
```

### Logout
```http
POST /auth/logout
Authorization: Bearer {accessToken}
```

**Response (200 OK)**: Empty body

---

## User Profile Endpoints (`/api/internal/users`)

All user endpoints require authentication header:
```
Authorization: Bearer {accessToken}
```

### Get Current User Profile
```http
GET /api/internal/users/me/profile
Authorization: Bearer {accessToken}
```

**Response (200 OK)**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "publicId": "12345678",
  "username": "john.doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+1234567890",
  "role": "USER",
  "active": true,
  "createdAt": "2024-01-01T10:00:00Z",
  "updatedAt": "2024-01-01T10:00:00Z"
}
```

### Get User by Public ID (for admin/team features)
```http
GET /api/internal/users/{publicId}
Authorization: Bearer {accessToken}
```

**Response (200 OK)**: Same as above

### List All Users (Team page)
```http
GET /api/internal/users?skip=0&take=50&search=john
Authorization: Bearer {accessToken}
```

**Response (200 OK)**:
```json
{
  "total": 100,
  "data": [
    {
      "id": "123e4567-e89b-12d3-a456-426614174000",
      "publicId": "12345678",
      "username": "john.doe",
      "email": "john@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "phone": "+1234567890",
      "role": "USER",
      "active": true,
      "createdAt": "2024-01-01T10:00:00Z",
      "updatedAt": "2024-01-01T10:00:00Z"
    }
  ]
}
```

### Update User Profile
```http
PATCH /api/internal/users/me/profile
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "firstName": "Jonathan",
  "lastName": "Doe",
  "phone": "+1987654321",
  "email": "jonathan@example.com"
}
```

**Response (200 OK)**: Updated user object

### List Users (Admin)
```http
GET /api/admin/users
Authorization: Bearer {accessToken}
```

**Response (200 OK)**: Array of user objects

### Create User (Admin)
```http
POST /api/admin/users?password=defaultPass123
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "username": "new.user",
  "email": "new@example.com",
  "firstName": "New",
  "lastName": "User",
  "phone": "+1122334455",
  "role": "USER"
}
```

**Response (200 OK)**: Created user object

### Update User (Admin)
```http
PATCH /api/admin/users/by-publicId/{publicId}?password=newpass
Authorization: Bearer {accessToken}
Content-Type: application/json

{
  "firstName": "Updated John",
  "lastName": "Updated Doe",
  "phone": "+9998887777",
  "role": "MANAGER"
}
```

**Response (200 OK)**: Updated user object

---

## Attendance Endpoints (`/api/v1/events`)

### Get Attendance Events
```http
GET /api/v1/events/attendance?from=2024-01-01&to=2024-01-31
Authorization: Bearer {accessToken}
```

**Response (200 OK)**:
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "userPublicId": "12345678",
    "eventType": "CHECK_IN",
    "timestamp": "2024-01-04T09:00:00Z",
    "deviceId": "223e4567-e89b-12d3-a456-426614174001",
    "ipAddress": "192.168.1.100",
    "confidence": 0.98
  },
  {
    "id": "323e4567-e89b-12d3-a456-426614174002",
    "userPublicId": "12345678",
    "eventType": "CHECK_OUT",
    "timestamp": "2024-01-04T18:00:00Z",
    "deviceId": "223e4567-e89b-12d3-a456-426614174001",
    "ipAddress": "192.168.1.100",
    "confidence": 0.95
  }
]
```

---

## Error Handling

All endpoints should return appropriate HTTP status codes and error messages:

### 400 Bad Request
```json
{
  "error": "Validation Failed",
  "message": "Email is required",
  "details": { "email": "required" }
}
```

### 401 Unauthorized
```json
{
  "error": "Unauthorized",
  "message": "Missing or invalid access token"
}
```

### 403 Forbidden
```json
{
  "error": "Forbidden",
  "message": "You do not have permission to perform this action"
}
```

### 404 Not Found
```json
{
  "error": "Not Found",
  "message": "User not found",
  "details": { "publicId": "99999999" }
}
```

### 500 Internal Server Error
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

---

## Frontend API Client

The frontend uses a custom `ApiClient` that:

1. Automatically adds `Authorization: Bearer {token}` header
2. Handles 401 responses by refreshing tokens
3. Sets `mode: 'cors'` and `credentials: 'include'`
4. Times out after 30 seconds by default
5. Parses JSON responses automatically

### Usage Example

```typescript
import { userClient, attendanceClient } from '@/lib/api-client';

// Fetch user profile
const profile = await userClient.get('/api/internal/users/me/profile');

// Fetch attendance events
const events = await attendanceClient.get('/api/v1/events/attendance', {
  params: {
    from: '2024-01-01',
    to: '2024-01-31'
  }
});

// Create user (admin)
const newUser = await userClient.post('/api/admin/users', userData, {
  params: { password: 'defaultPass123' }
});
```

---

## Testing the Frontend

### Local Development

1. Ensure all microservices are running
2. Frontend on `http://localhost:3000`
3. Update `.env.local` with correct service URLs
4. Test login/register flow
5. Check API calls in browser DevTools Network tab

### Common Issues

**CORS Error**: Backend not allowing requests from `http://localhost:3000`

**401 on Every Request**: Token not being sent or backend not validating correctly

**Network Connection Error**: Service URL incorrect or service not running

---

## Rate Limiting (Recommended)

Consider implementing rate limiting on backend:
- Authentication endpoints: 5 requests per minute per IP
- Other endpoints: 100 requests per minute per user
- Return `429 Too Many Requests` when exceeded

---

## Security Considerations

1. **HTTPS in Production**: Always use HTTPS
2. **Secure Cookies**: Mark tokens as `HttpOnly`, `Secure`, `SameSite=Strict`
3. **CORS Whitelist**: Only allow specific origins
4. **Token Expiry**: Access tokens should expire in 1-3 hours
5. **Refresh Token Rotation**: Issue new refresh tokens on refresh
6. **Input Validation**: Validate all user inputs on backend

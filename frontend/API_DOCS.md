# Authentication API Documentation

## Port Configuration
**All API endpoints are configured for PORT 8081** (`http://localhost:8081/api`)

## API Endpoints

### 1. User Registration
**Endpoint:** `POST /api/auth/register`

**Request Body:**
```json
{
  "username": "doniyor",
  "email": "doniyor@mail.com",
  "password": "Password123"
}
```

**Success Response (200/201):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900,
  "user": {
    "publicId": "uuid-or-string",
    "username": "doniyor",
    "email": "doniyor@mail.com",
    "role": "USER"
  }
}
```

**Error Responses:**
- `400`: Validation error (empty fields, weak password)
- `409`: Username or email already exists

---

### 2. User Login
**Endpoint:** `POST /api/auth/login`

**Request Body:**
```json
{
  "username": "doniyor",
  "password": "Password123"
}
```

**Success Response (200):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

**Error Responses:**
- `400`: Invalid credentials
- `401`: Unauthorized

---

### 3. Check Username Availability
**Endpoint:** `GET /api/auth/check-username?username=doniyor`

**Response (200):**
```json
{
  "available": true
}
```

---

### 4. Check Email Availability
**Endpoint:** `GET /api/auth/check-email?email=doniyor@mail.com`

**Response (200):**
```json
{
  "available": true
}
```

---

### 5. Get Current User
**Endpoint:** `GET /api/users/me`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Response (200):**
```json
{
  "publicId": "uuid-string",
  "username": "doniyor",
  "email": "doniyor@mail.com",
  "role": "USER"
}
```

**Error Responses:**
- `401`: Unauthorized (invalid or missing token)

---

### 6. Refresh Access Token
**Endpoint:** `POST /api/auth/refresh`

**Request Body:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc..."
}
```

**Error Responses:**
- `401`: Invalid refresh token

---

### 7. Logout
**Endpoint:** `POST /api/auth/logout`

**Headers:**
```
Authorization: Bearer <accessToken>
```

**Request Body:**
```json
{
  "refreshToken": "eyJhbGc..."
}
```

**Response (200):**
```json
{
  "ok": true
}
```

---

## Frontend Implementation

### Using the API Client

The project includes a pre-configured `ApiClient` in `lib/api.ts` that handles:
- Automatic token storage
- Token refresh logic
- Request/response handling
- Authentication state management

### Using the Auth Hook

```tsx
'use client';

import { useAuth } from '@/lib/auth-context';

function MyComponent() {
  const { user, isAuthenticated, login, logout, isLoading } = useAuth();

  return (
    <>
      {isAuthenticated ? (
        <>
          <p>Welcome, {user?.username}</p>
          <button onClick={logout}>Logout</button>
        </>
      ) : (
        <p>Not logged in</p>
      )}
    </>
  );
}
```

---

## Pages

### 1. Homepage (`/`)
- Welcome page with navigation
- Shows login/register links for guests
- Shows dashboard link for authenticated users

### 2. Register Page (`/register`)
- User registration form
- Fields: Username, Email, Password, Confirm Password
- Real-time validation
- Redirects to dashboard on success

### 3. Login Page (`/login`)
- User login form
- Fields: Username, Password
- Redirects to dashboard on success

### 4. Dashboard Page (`/dashboard`)
- Protected route (requires authentication)
- Displays user profile information
- Shows logout button
- Auto-redirects to login if not authenticated

---

## Token Management

### Storage
- **Access Token**: Stored in localStorage
- **Refresh Token**: Stored in localStorage
- Automatically loaded on app startup

### Auto-Refresh
- When access token expires, the client automatically requests a new one
- No user action required

### Logout
- Tokens are cleared from localStorage
- Optional server-side blacklisting of refresh tokens

---

## Testing with cURL

### Register
```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123"
  }'
```

### Login
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Password123"
  }'
```

### Get Current User
```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

### Logout
```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"refreshToken": "YOUR_REFRESH_TOKEN"}'
```

---

## Environment Variables

The API client is configured to use:
- **API_BASE_URL**: `http://localhost:8081/api`

No additional environment variables are required for the frontend.

---

## Error Handling

All error responses follow this format:
```json
{
  "message": "Error description",
  "statusCode": 400,
  "timestamp": "2024-02-24T10:30:00Z"
}
```

The frontend automatically displays error messages to users.

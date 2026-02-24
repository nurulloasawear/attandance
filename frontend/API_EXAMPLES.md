# API Примеры использования

## Тестирование API на порту 8081

### 1. Регистрация

```bash
curl -X POST http://localhost:8081/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test1234"
  }'
```

Ответ (200 OK):
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "user",
    "publicId": "usr_xxx",
    "createdAt": "2024-02-24T10:30:00Z"
  }
}
```

### 2. Вход (Login)

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test1234"
  }'
```

Ответ (200 OK):
```json
{
  "message": "Login successful",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "user",
    "publicId": "usr_xxx"
  }
}
```

### 3. Получение информации о пользователе

```bash
curl -X GET http://localhost:8081/me \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Ответ (200 OK):
```json
{
  "user": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "role": "user",
    "publicId": "usr_xxx",
    "createdAt": "2024-02-24T10:30:00Z"
  }
}
```

### 4. Обновление токена (Refresh)

```bash
curl -X POST http://localhost:8081/refresh \
  -H "Content-Type: application/json" \
  -d '{
    "refreshToken": "YOUR_REFRESH_TOKEN"
  }'
```

Ответ (200 OK):
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 5. Проверка доступности имени пользователя

```bash
curl -X GET "http://localhost:8081/check-username?username=testuser"
```

Ответ (200 OK):
```json
{
  "available": false,
  "message": "Username is already taken"
}
```

### 6. Проверка доступности email

```bash
curl -X GET "http://localhost:8081/check-email?email=test@example.com"
```

Ответ (200 OK):
```json
{
  "available": false,
  "message": "Email is already registered"
}
```

### 7. Выход (Logout)

```bash
curl -X POST http://localhost:8081/logout \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

Ответ (200 OK):
```json
{
  "message": "Logout successful"
}
```

## Обработка ошибок

### Неверные учетные данные (401)

```json
{
  "error": "Invalid username or password",
  "statusCode": 401
}
```

### Неавторизованный доступ (401)

```json
{
  "error": "Unauthorized",
  "statusCode": 401
}
```

### Неверный запрос (400)

```json
{
  "error": "Username is required",
  "statusCode": 400
}
```

### Конфликт данных (409)

```json
{
  "error": "Username already exists",
  "statusCode": 409
}
```

### Ошибка сервера (500)

```json
{
  "error": "Internal server error",
  "statusCode": 500
}
```

## Переменные окружения

Убедитесь, что в файле `.env.local` установлены следующие переменные:

```env
# API Configuration
NEXT_PUBLIC_API_BASE_URL=http://localhost:8081
NEXT_PUBLIC_API_PORT=8081

# API Timeout
NEXT_PUBLIC_API_TIMEOUT=10000
```

## Фронтенд интеграция

### Использование API через клиент

```typescript
import { apiClient } from '@/lib/api';

// Регистрация
const response = await apiClient.register({
  username: 'testuser',
  email: 'test@example.com',
  password: 'Test1234'
});

// Вход
const loginResponse = await apiClient.login({
  username: 'testuser',
  password: 'Test1234'
});

// Получение пользователя
const user = await apiClient.getMe();

// Выход
await apiClient.logout();

// Обновление токена
const tokens = await apiClient.refreshToken(refreshToken);
```

## Тестирование в Postman

1. Скачайте Postman: https://www.postman.com/downloads/
2. Создайте новую коллекцию "AuthApp"
3. Добавьте следующие запросы:
   - POST `/register`
   - POST `/login`
   - GET `/me` (с Authorization header)
   - POST `/refresh`
   - GET `/check-username`
   - GET `/check-email`
   - POST `/logout` (с Authorization header)

## WebSocket поддержка (опционально)

Если сервер поддерживает WebSocket, можно использовать для real-time обновлений:

```typescript
const ws = new WebSocket('ws://localhost:8081');

ws.onopen = () => {
  ws.send(JSON.stringify({ 
    type: 'auth', 
    token: accessToken 
  }));
};

ws.onmessage = (event) => {
  console.log('Message from server:', event.data);
};
```

## Безопасность

- Всегда используйте HTTPS в production
- Храните токены в secure cookies (HttpOnly, Secure, SameSite)
- Никогда не храните чувствительные данные в localStorage
- Используйте CORS правильно на сервере
- Регулярно обновляйте токены через refresh endpoint
- Реализуйте rate limiting на сервере

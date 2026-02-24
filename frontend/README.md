# AuthApp - Современная система аутентификации

Красиво оформленное приложение аутентификации с JWT токенами, регистрацией и защищённым dashboard.

## Скриншоты

### Главная страница
- Героический дизайн с информацией о приложении
- Кнопки для входа и регистрации
- Отображение основных возможностей системы

### Регистрация
- Форма с валидацией пароля
- Красивый UI с индикаторами требований
- Защита от дублирования username/email

### Вход
- Простая форма входа
- Демо учетные данные для тестирования
- Автоматический редирект на dashboard после входа

### Dashboard
- Защищённая страница с информацией профиля
- Отображение username, email, role, ID
- Статистика и информация о безопасности
- Кнопка выхода

## Особенности

✨ **Современный дизайн**
- Тёмная тема с голубыми акцентами
- Плавные переходы и анимации
- Полностью адаптивен для мобильных устройств

🔐 **Безопасность**
- JWT токены для аутентификации
- Refresh token механика
- Автоматическое обновление токенов
- Хеширование паролей (bcrypt)

⚡ **Производительность**
- Оптимизированный код на Next.js 16
- Lazy loading компонентов
- Кеширование API запросов

🎨 **Технологии**
- React 19.2
- Next.js 16 (App Router)
- TypeScript
- Tailwind CSS v4
- shadcn/ui компоненты
- Lucide иконки

## Быстрый старт

### 1. Установка

```bash
# Скачайте проект и распакуйте архив
unzip project.zip
cd project

# Установите зависимости
pnpm install
```

### 2. Конфигурация

Создайте `.env.local` файл:

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8081
NEXT_PUBLIC_API_PORT=8081
```

### 3. Запуск

```bash
pnpm dev
# Откройте http://localhost:3000
```

### 4. Backend

Убедитесь, что backend API запущен на порту **8081**:

```bash
# Стартуйте ваш backend на порту 8081
# Backend должен иметь endpoints:
# POST /register
# POST /login
# GET /me
# POST /refresh
# POST /logout
# GET /check-username
# GET /check-email
```

## Структура

```
project/
├── app/              # Next.js приложение
│   ├── page.tsx      # Главная страница
│   ├── login/        # Страница входа
│   ├── register/     # Страница регистрации
│   └── dashboard/    # Защищённый dashboard
├── components/       # React компоненты
│   ├── login-form.tsx
│   ├── register-form.tsx
│   └── dashboard.tsx
├── lib/              # Утилиты
│   ├── api.ts        # API клиент
│   └── auth-context.tsx  # Auth контекст
└── API_EXAMPLES.md   # Примеры API запросов
```

## API Endpoints

### Регистрация
```bash
POST /register
Content-Type: application/json

{
  "username": "john",
  "email": "john@example.com",
  "password": "Test1234"
}
```

### Вход
```bash
POST /login
Content-Type: application/json

{
  "username": "john",
  "password": "Test1234"
}
```

### Получение пользователя
```bash
GET /me
Authorization: Bearer ACCESS_TOKEN
```

### Обновление токена
```bash
POST /refresh
Content-Type: application/json

{
  "refreshToken": "REFRESH_TOKEN"
}
```

### Выход
```bash
POST /logout
Authorization: Bearer ACCESS_TOKEN
```

### Проверка username
```bash
GET /check-username?username=john
```

### Проверка email
```bash
GET /check-email?email=john@example.com
```

## Цветовая схема

| Элемент | Цвет | Hex |
|---------|------|-----|
| Фон | Тёмно-серый | #0f1117 |
| Текст | Светло-серый | #e6edf3 |
| Акцент | Голубой | #58a6ff |
| Карточки | Серый | #161b22 |
| Границы | Тёмный серый | #30363d |

## Требования

- Node.js 18+
- pnpm 9+ (или npm/yarn)
- Backend API на порту 8081

## Установка и запуск на Ubuntu

```bash
# 1. Убедитесь, что Node.js установлен
node --version
npm --version

# 2. Скачайте и распакуйте проект
unzip project.zip
cd project

# 3. Установите pnpm (если нет)
npm install -g pnpm

# 4. Установите зависимости
pnpm install

# 5. Создайте .env.local файл
echo "NEXT_PUBLIC_API_BASE_URL=http://localhost:8081" > .env.local
echo "NEXT_PUBLIC_API_PORT=8081" >> .env.local

# 6. Запустите приложение
pnpm dev

# 7. Откройте в браузере
# http://localhost:3000
```

## Команды

```bash
# Разработка
pnpm dev              # Запуск dev сервера

# Build
pnpm build            # Build для production
pnpm start            # Запуск production версии

# Качество кода
pnpm lint             # Проверка кода
pnpm type-check       # Проверка типов
pnpm format           # Форматирование кода

# Testing
pnpm test             # Запуск тестов
```

## Тестирование

### Демо учетные данные

```
Username: testuser
Password: Test1234
Email: test@example.com
```

### Тестирование в Postman

1. Откройте Postman
2. Создайте POST запрос на `http://localhost:8081/login`
3. Введите демо учетные данные
4. Копируйте accessToken из ответа
5. Используйте в заголовке Authorization: Bearer TOKEN

## Производительность

- Время загрузки: < 2s
- FCP (First Contentful Paint): < 1s
- LCP (Largest Contentful Paint): < 2.5s
- CLS (Cumulative Layout Shift): < 0.1

## Бизнес-логика

### Аутентификация

1. **Регистрация**: Новый пользователь создает аккаунт
2. **Вход**: Пользователь получает access и refresh токены
3. **Защита маршрутов**: Dashboard требует валидный access token
4. **Refresh**: Автоматическое обновление токена при истечении
5. **Выход**: Инвалидация сессии на сервере

### Хранилище

- Access Token: Хранится в localStorage (для фронтенда)
- Refresh Token: Хранится в localStorage
- Рекомендация: В production используйте HttpOnly cookies

## Безопасность

✅ Реализовано:
- JWT аутентификация
- Хеширование паролей
- Валидация входных данных
- XSS защита через React
- CORS поддержка

⚠️ Рекомендации:
- Используйте HTTPS в production
- Храните токены в HttpOnly cookies
- Реализуйте rate limiting на сервере
- Регулярно обновляйте зависимости
- Добавьте 2FA для дополнительной безопасности

## Развертывание

### Vercel (рекомендуется)

```bash
# 1. Push на GitHub
git push origin main

# 2. Откройте https://vercel.com/import
# 3. Выберите репозиторий
# 4. Установите переменные окружения
# 5. Deploy!
```

### Docker

```dockerfile
FROM node:18-alpine
WORKDIR /app
COPY package.json pnpm-lock.yaml ./
RUN npm install -g pnpm && pnpm install
COPY . .
RUN pnpm build
EXPOSE 3000
CMD ["pnpm", "start"]
```

## Основной стек технологий

| Технология | Версия | Назначение |
|-----------|--------|-----------|
| React | 19.2 | UI библиотека |
| Next.js | 16 | React фреймворк |
| TypeScript | 5 | Статический типизм |
| Tailwind CSS | 4 | Стили |
| shadcn/ui | Latest | Компоненты UI |
| Lucide | Latest | Иконки |

## Доступные компоненты UI

- Button
- Input
- Label
- Card
- Alert
- Select
- Checkbox
- Radio
- Textarea
- Dropdown Menu
- Dialog
- Tabs
- Toast уведомления

## Развитие проекта

Возможные улучшения:
- Добавить 2FA аутентификацию
- Интеграция с OAuth (Google, GitHub)
- Система восстановления пароля
- Email верификация
- Аудит логи
- User management панель
- Интеграция с аналитикой

## Лицензия

MIT License - свободное использование

## Поддержка

Для вопросов и проблем:
1. Проверьте документацию в SETUP.md
2. Посмотрите примеры API в API_EXAMPLES.md
3. Проверьте консоль браузера на ошибки

## Спасибо за использование AuthApp!

Если вам понравилось приложение, поделитесь им с друзьями ⭐

---

**Версия**: 1.0.0  
**Последнее обновление**: 2024-02-24  
**API Порт**: 8081  
**Dev Порт**: 3000  
**Фронтенд Порт**: 3000

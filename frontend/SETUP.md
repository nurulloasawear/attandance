# Руководство по установке и запуску

## Современный фронтенд для системы аутентификации

Приложение использует:
- **Next.js 16** (React 19.2)
- **TypeScript** 
- **Tailwind CSS v4**
- **shadcn/ui компоненты**
- **Lucide иконки**
- **JWT аутентификация**

## Локальная установка

### 1. Скачивание проекта

```bash
# Распакуйте архив
unzip project.zip
cd project
```

### 2. Установка зависимостей

```bash
# Используя pnpm (рекомендуется)
pnpm install

# Или npm
npm install

# Или yarn
yarn install
```

### 3. Запуск фронтенда на порту 3000

```bash
pnpm dev
# или
npm run dev
```

Откройте http://localhost:3000 в браузере

### 4. Настройка окружения

Создайте файл `.env.local` в корне проекта:

```env
# API Configuration
NEXT_PUBLIC_API_BASE_URL=http://localhost:8081
NEXT_PUBLIC_API_PORT=8081
NEXT_PUBLIC_API_TIMEOUT=10000
```

## Запуск на конкретном порту

Если вам нужно запустить на другом порту (не 3000):

```bash
pnpm dev -- -p 8000
# или
npm run dev -- -p 8000
```

## Запуск backend API

Убедитесь, что backend API запущен на **порту 8081**:

```bash
# Если у вас есть backend
cd backend
npm install
npm start
# или
npm run dev
```

Backend должен работать на `http://localhost:8081`

## Структура проекта

```
project/
├── app/
│   ├── layout.tsx          # Root layout с AuthProvider
│   ├── page.tsx            # Главная страница с героем
│   ├── globals.css         # Глобальные стили и темы
│   ├── login/
│   │   └── page.tsx        # Страница входа
│   ├── register/
│   │   └── page.tsx        # Страница регистрации
│   └── dashboard/
│       └── page.tsx        # Защищённый dashboard
│
├── components/
│   ├── login-form.tsx      # Компонент формы входа
│   ├── register-form.tsx   # Компонент формы регистрации
│   ├── dashboard.tsx       # Компонент dashboard
│   └── ui/                 # shadcn компоненты
│
├── lib/
│   ├── api.ts              # API клиент для запросов
│   ├── auth-context.tsx    # React Context для аутентификации
│   └── utils.ts            # Утилиты
│
├── public/                 # Статические файлы
└── package.json            # Зависимости
```

## Основные страницы

### 1. Главная страница (`/`)
- Отображает информацию о приложении
- Кнопки для входа/регистрации
- Информация о текущем статусе аутентификации
- Красивый современный дизайн с градиентом

### 2. Регистрация (`/register`)
- Форма создания нового аккаунта
- Валидация пароля (минимум 8 символов)
- Проверка совпадения паролей
- Показ требований к паролю

### 3. Вход (`/login`)
- Форма входа с username и password
- Демо учетные данные для тестирования
- Обработка ошибок входа
- Ссылка на регистрацию

### 4. Dashboard (`/dashboard`)
- Защищённая страница (требует аутентификации)
- Отображение информации профиля
- Статистика сессии
- Кнопка выхода

## Цветовая схема

Применена современная тёмная тема:
- **Фон**: `#0f1117` (тёмный фон)
- **Основной текст**: `#e6edf3` (светлый серый)
- **Акцент**: `#58a6ff` (яркий голубой)
- **Карточки**: `#161b22` (немного светлее фона)
- **Границы**: `#30363d` (тёмно-серый)

## Авторизация и защита маршрутов

Приложение использует JWT токены:

```typescript
// lib/auth-context.tsx
- login() - Вход в систему
- register() - Регистрация
- logout() - Выход
- isAuthenticated - Флаг авторизации
- user - Данные пользователя
```

Dashboard автоматически перенаправляет на `/login` если пользователь не авторизован.

## API Integration

Все запросы к API проходят через:

```typescript
// lib/api.ts
- register(credentials)
- login(credentials)
- logout()
- getMe()
- refreshToken()
- checkUsername()
- checkEmail()
```

Все методы поддерживают обработку ошибок и автоматическое обновление токенов.

## Развертывание на Vercel

### 1. Подготовка к deployments

```bash
# Создайте Git репозиторий
git init
git add .
git commit -m "Initial commit"
```

### 2. Push на GitHub

```bash
git remote add origin https://github.com/ваше-имя/ваш-репо.git
git push -u origin main
```

### 3. Deploy на Vercel

1. Откройте https://vercel.com/import
2. Выберите ваш GitHub репозиторий
3. Установите переменные окружения:
   - `NEXT_PUBLIC_API_BASE_URL`: https://your-backend.com
   - `NEXT_PUBLIC_API_PORT`: 8081
4. Нажмите Deploy

## Тестирование

### Unit тесты

```bash
npm run test
```

### Build проверка

```bash
npm run build
npm run start
```

### Проверка типов

```bash
npm run type-check
```

## Troubleshooting

### Ошибка: "API not found"
- Убедитесь, что backend запущен на `http://localhost:8081`
- Проверьте значение `NEXT_PUBLIC_API_BASE_URL` в `.env.local`

### Ошибка: "CORS error"
- Добавьте на backend CORS headers
- Убедитесь, что origin разрешен

### Ошибка: "Token expired"
- Приложение автоматически обновляет токены
- Если не работает, проверьте refresh token

### Залипает на "Loading..."
- Проверьте консоль браузера на ошибки
- Убедитесь, что API доступен

## Переменные окружения

| Переменная | Описание | По умолчанию |
|-----------|---------|-----------|
| `NEXT_PUBLIC_API_BASE_URL` | URL backend API | `http://localhost:8081` |
| `NEXT_PUBLIC_API_PORT` | Порт API | `8081` |
| `NEXT_PUBLIC_API_TIMEOUT` | Timeout запросов (мс) | `10000` |

## Полезные команды

```bash
# Установка зависимостей
pnpm install

# Разработка
pnpm dev

# Build для production
pnpm build

# Запуск production версии
pnpm start

# Type checking
pnpm type-check

# Lint код
pnpm lint

# Format код
pnpm format
```

## Поддержка браузеров

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Производительность

Приложение оптимизировано:
- Image optimization через Next.js
- Code splitting и lazy loading
- Минификация CSS/JS
- Кеширование API запросов через SWR

## Безопасность

Реализованы меры безопасности:
- JWT токены хранятся в локальном хранилище
- HTTPS рекомендуется для production
- Валидация входных данных
- XSS защита через React
- CSRF защита через SameSite cookies

## Поддержка

Если возникли проблемы:
1. Проверьте консоль браузера (F12 → Console)
2. Проверьте Network вкладку для ошибок API
3. Убедитесь, что backend запущен
4. Проверьте конфигурацию `.env.local`

## Лицензия

MIT License - свободное использование в личных и коммерческих проектах.

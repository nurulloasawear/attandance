# TimeChamp - Employee Attendance Management System

A modern, full-stack employee attendance and time-tracking application built with Next.js 16, React 19, and microservices architecture.

## Project Overview

TimeChamp is inspired by Workfolio but with a clean, minimalist white design aesthetic. The application provides:

- **Employee Attendance Tracking** - Clock in/out functionality
- **Timesheet Management** - Track weekly/monthly hours
- **Team Management** - Admin dashboard for user management
- **Analytics & Reports** - Detailed attendance reports and insights
- **User Profiles & Settings** - Personalized user experience

## Architecture

This is a **frontend application** that communicates with multiple backend microservices:

- **Auth Service** - Authentication and JWT token management
- **User Service** - User profiles and team management
- **Attendance Service** - Attendance tracking and events

## Technology Stack

- **Framework**: Next.js 16 (App Router)
- **UI Library**: React 19
- **Styling**: Tailwind CSS 4 + shadcn/ui components
- **Charts**: Recharts
- **Form Handling**: React Hook Form + Zod
- **HTTP Client**: Fetch API with custom wrapper
- **Icons**: Lucide React

## Installation & Setup

### Prerequisites

- Node.js 18+
- pnpm (recommended) or npm

### Step 1: Install Dependencies

```bash
pnpm install
```

### Step 2: Environment Configuration

Create a `.env.local` file in the root directory with the following variables:

```env
# API Service URLs (adjust ports as needed for your setup)
NEXT_PUBLIC_AUTH_API_URL=http://localhost:8081
NEXT_PUBLIC_USER_API_URL=http://localhost:8081
NEXT_PUBLIC_ATTENDANCE_API_URL=http://localhost:8082
```

**Note**: The default URLs assume:
- Auth & User services run on `localhost:8081`
- Attendance service runs on `localhost:8082`

Adjust these based on your actual microservices setup.

### Step 3: CORS Configuration (Important!)

Since this frontend communicates directly with microservices (no API gateway), each backend service must allow CORS requests from your frontend origin.

Each microservice should be configured to accept requests from:
- **Headers**: `Authorization`, `Content-Type`
- **Methods**: `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`
- **Origin**: `http://localhost:3000` (development)
- **Credentials**: `include`

### Step 4: Run Development Server

```bash
pnpm dev
```

Visit `http://localhost:3000` in your browser.

## Project Structure

```
app/
├── page.tsx                  # Landing page
├── layout.tsx                # Root layout with theme
├── globals.css               # Global styles & design tokens
├── auth/
│   ├── login/page.tsx        # Login page
│   └── register/page.tsx     # Registration page
└── app/
    ├── layout.tsx            # Dashboard layout (sidebar + topbar)
    ├── overview/page.tsx     # Dashboard home with KPIs & charts
    ├── attendance/page.tsx   # Attendance tracking
    ├── timesheets/page.tsx   # Timesheet management
    ├── team/page.tsx         # Team management (admin)
    ├── reports/page.tsx      # Analytics & reports
    └── settings/page.tsx     # User settings & profile

components/
├── dashboard-sidebar.tsx     # Collapsible navigation sidebar
├── dashboard-topbar.tsx      # Header with user menu
└── ui/                       # shadcn/ui components

lib/
├── api-client.ts             # Custom HTTP client with JWT handling
└── utils.ts                  # Utility functions

```

## API Integration

### Authentication Flow

1. **Login/Register**: User provides credentials
2. **Tokens Received**: API returns `accessToken` and `refreshToken`
3. **Token Storage**:
   - `accessToken` → `sessionStorage` (cleared on browser close)
   - `refreshToken` → `localStorage` (persisted)
4. **API Requests**: All requests include `Authorization: Bearer {accessToken}`
5. **Token Refresh**: On `401` response, automatically refresh token and retry

### API Client Usage

The custom `ApiClient` class handles all HTTP communication:

```typescript
import { authClient, userClient, attendanceClient } from '@/lib/api-client';

// Set tokens after login
authClient.setTokens(accessToken, refreshToken);

// Make requests
const user = await userClient.get('/api/internal/users/me/profile');
const events = await attendanceClient.get('/api/v1/events/attendance?from=2024-01-01&to=2024-01-31');

// Logout
authClient.clearTokens();
```

## Design System

### Colors

- **Background**: `#ffffff` (white)
- **Foreground**: `#111827` (dark gray)
- **Primary**: `#2563eb` (blue)
- **Secondary**: `#f7f8fa` (light gray)
- **Borders**: `#e5e7eb` (gray)
- **Muted**: `#6b7280` (medium gray)

### Key Features

- Clean, minimal white design with soft shadows
- Smooth animations (200-300ms transitions)
- Rounded corners (14-18px)
- Responsive mobile-first layout
- Accessible color contrasts

## Key Pages

### Landing Page (`/`)
- Hero section with value proposition
- Feature highlights
- CTA buttons for login/signup

### Authentication (`/auth/login`, `/auth/register`)
- Clean form layouts
- Form validation
- Error handling with toast notifications

### Dashboard (`/app/*`)
- **Overview**: KPIs, charts, recent activity
- **Attendance**: Clock in/out, attendance history
- **Timesheets**: Weekly timesheets, export as CSV/Excel
- **Team**: Employee directory, role management (admin only)
- **Reports**: Attendance analytics, department comparisons
- **Settings**: Profile, password, notifications, security

## Development Tips

### Adding New Pages

1. Create folder structure: `app/app/[feature]/page.tsx`
2. Use the `DashboardTopbar` and `DashboardSidebar` components
3. Add navigation item in `components/dashboard-sidebar.tsx`

### API Integration

1. Use `userClient`, `authClient`, or `attendanceClient` from `lib/api-client.ts`
2. Handle errors with proper user feedback
3. Implement loading states with skeleton loaders
4. Test with actual backend services

### Charts & Visualizations

All charts use Recharts with consistent styling:
- Blue color scheme matching brand
- Grid lines: `#e5e7eb`
- Tooltip styling: white background with border

## Troubleshooting

### CORS Errors

**Problem**: `Access to XMLHttpRequest blocked by CORS policy`

**Solution**: Ensure backend services have CORS enabled:
- Check `Access-Control-Allow-Origin` header includes `http://localhost:3000`
- Verify `OPTIONS` preflight requests are handled

### 401 Unauthorized

**Problem**: Getting 401 errors on every request

**Solution**:
1. Check tokens are being stored correctly in `sessionStorage`/`localStorage`
2. Verify token format: `Bearer {token}` in Authorization header
3. Ensure backend validates JWT correctly
4. Try logging in again to get fresh tokens

### Connection Refused

**Problem**: `GET http://localhost:8081 net::ERR_CONNECTION_REFUSED`

**Solution**:
1. Verify microservices are running
2. Check correct ports in `.env.local`
3. Ensure services are accessible on `localhost`

## Production Deployment

For production deployment:

1. **Update API URLs**: Use production service URLs in `.env.local`
2. **Build**: `pnpm build`
3. **Deploy**: Use Vercel, Netlify, or your preferred hosting
4. **CORS**: Configure CORS for production origin
5. **Security**: Use HTTPS, secure cookie settings, etc.

## License

MIT

## Support

For issues or questions, contact the development team or check the documentation.

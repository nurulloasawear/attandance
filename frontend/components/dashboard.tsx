'use client';

import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import { User, Mail, Shield, Key, LogOut, Lock } from 'lucide-react';

export function Dashboard() {
  const router = useRouter();
  const { user, logout, isLoading } = useAuth();

  const handleLogout = async () => {
    try {
      await logout();
      router.push('/login');
    } catch (err) {
      console.error('Logout failed:', err);
    }
  };

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="text-center">
          <Lock className="w-12 h-12 text-destructive mx-auto mb-4" />
          <p className="text-lg text-foreground">Не авторизованы. Пожалуйста войдите.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Welcome Card */}
      <div className="rounded-xl border border-border bg-gradient-to-br from-accent/10 via-accent/5 to-card p-8">
        <h2 className="text-3xl font-bold text-foreground mb-2">
          Добро пожаловать, {user.username}! 👋
        </h2>
        <p className="text-muted-foreground">
          Вы успешно вошли в систему. Вот ваша информация профиля.
        </p>
      </div>

      {/* Profile Information */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Username */}
        <div className="rounded-lg border border-border bg-card/50 p-6 hover:bg-card/80 transition-colors">
          <div className="flex items-start justify-between mb-3">
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                Имя пользователя
              </p>
              <p className="text-2xl font-bold text-accent">{user.username}</p>
            </div>
            <User className="w-6 h-6 text-accent/50" />
          </div>
        </div>

        {/* Email */}
        <div className="rounded-lg border border-border bg-card/50 p-6 hover:bg-card/80 transition-colors">
          <div className="flex items-start justify-between mb-3">
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                Email
              </p>
              <p className="text-xl font-semibold text-foreground break-all">{user.email}</p>
            </div>
            <Mail className="w-6 h-6 text-accent/50" />
          </div>
        </div>

        {/* Role */}
        <div className="rounded-lg border border-border bg-card/50 p-6 hover:bg-card/80 transition-colors">
          <div className="flex items-start justify-between mb-3">
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                Роль
              </p>
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 bg-accent rounded-full" />
                <p className="text-lg font-semibold text-foreground capitalize">{user.role}</p>
              </div>
            </div>
            <Shield className="w-6 h-6 text-accent/50" />
          </div>
        </div>

        {/* Public ID */}
        <div className="rounded-lg border border-border bg-card/50 p-6 hover:bg-card/80 transition-colors">
          <div className="flex items-start justify-between mb-3">
            <div>
              <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
                Публичный ID
              </p>
              <p className="text-sm font-mono text-accent/80 break-all">{user.publicId}</p>
            </div>
            <Key className="w-6 h-6 text-accent/50" />
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        <div className="rounded-lg border border-border bg-card p-4">
          <p className="text-xs text-muted-foreground mb-1">Статус</p>
          <p className="text-lg font-bold text-accent">Активен</p>
        </div>
        <div className="rounded-lg border border-border bg-card p-4">
          <p className="text-xs text-muted-foreground mb-1">Сессия</p>
          <p className="text-lg font-bold text-accent">Валидна</p>
        </div>
        <div className="rounded-lg border border-border bg-card p-4">
          <p className="text-xs text-muted-foreground mb-1">Порт API</p>
          <p className="text-lg font-bold text-accent">8081</p>
        </div>
      </div>

      {/* Logout Button */}
      <Button
        onClick={handleLogout}
        disabled={isLoading}
        className="w-full h-12 text-base bg-destructive hover:bg-destructive/90 text-white font-semibold rounded-lg transition-all"
      >
        {isLoading ? (
          <div className="flex items-center gap-2">
            <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
            Выход...
          </div>
        ) : (
          <div className="flex items-center gap-2">
            <LogOut className="w-5 h-5" />
            Выход из аккаунта
          </div>
        )}
      </Button>

      {/* Security Info */}
      <div className="rounded-lg border border-border bg-card/30 p-4">
        <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider mb-2">
          Информация безопасности
        </p>
        <ul className="space-y-2 text-sm text-muted-foreground">
          <li className="flex items-center gap-2">
            <div className="w-1.5 h-1.5 bg-accent rounded-full" />
            Сеанс защищен JWT токеном
          </li>
          <li className="flex items-center gap-2">
            <div className="w-1.5 h-1.5 bg-accent rounded-full" />
            Данные зашифрованы
          </li>
          <li className="flex items-center gap-2">
            <div className="w-1.5 h-1.5 bg-accent rounded-full" />
            Поддержка HTTPS
          </li>
        </ul>
      </div>
    </div>
  );
}

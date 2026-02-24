'use client';

import Link from 'next/link';
import { useAuth } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import { LogOut, Lock, Zap, Shield } from 'lucide-react';

export default function Home() {
  const { isAuthenticated, user, logout } = useAuth();

  return (
    <main className="min-h-screen bg-gradient-to-br from-background via-background to-card">
      {/* Navigation */}
      <nav className="border-b border-border backdrop-blur-xl bg-background/80">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex items-center justify-between">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-accent rounded-lg flex items-center justify-center">
              <Lock className="w-6 h-6 text-accent-foreground" />
            </div>
            <span className="text-xl font-bold text-foreground">AuthApp</span>
          </div>
          {isAuthenticated && (
            <div className="flex items-center gap-3">
              <span className="text-sm text-muted-foreground">{user?.username}</span>
              <button
                onClick={logout}
                className="flex items-center gap-2 px-4 py-2 text-sm text-foreground hover:bg-secondary rounded-lg transition-colors"
              >
                <LogOut className="w-4 h-4" />
                Выход
              </button>
            </div>
          )}
        </div>
      </nav>

      {/* Hero Section */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left Content */}
          <div className="space-y-8">
            <div className="space-y-4">
              <div className="inline-flex items-center gap-2 px-4 py-2 bg-accent/10 rounded-full border border-accent/20">
                <div className="w-2 h-2 bg-accent rounded-full animate-pulse" />
                <span className="text-sm text-accent font-medium">Современная система аутентификации</span>
              </div>

              <h1 className="text-5xl md:text-6xl font-bold text-foreground leading-tight">
                Безопасный вход в ваш аккаунт
              </h1>

              <p className="text-xl text-muted-foreground leading-relaxed">
                Быстрая и надежная система регистрации и входа с поддержкой JWT токенов, refresh механики и полной защитой данных.
              </p>
            </div>

            {/* CTA Buttons */}
            {isAuthenticated ? (
              <div className="flex flex-col sm:flex-row gap-4 pt-4">
                <Link href="/dashboard" className="flex-1">
                  <Button className="w-full h-12 text-base bg-accent hover:bg-accent/90 text-accent-foreground font-semibold rounded-lg">
                    Перейти в Dashboard
                  </Button>
                </Link>
              </div>
            ) : (
              <div className="flex flex-col sm:flex-row gap-4 pt-4">
                <Link href="/register" className="flex-1">
                  <Button className="w-full h-12 text-base bg-accent hover:bg-accent/90 text-accent-foreground font-semibold rounded-lg transition-all">
                    Создать аккаунт
                  </Button>
                </Link>
                <Link href="/login" className="flex-1">
                  <button className="w-full px-6 h-12 text-base font-semibold text-accent border border-accent/30 rounded-lg hover:bg-accent/10 transition-colors">
                    Вход
                  </button>
                </Link>
              </div>
            )}

            {/* Stats */}
            <div className="grid grid-cols-3 gap-4 pt-8 border-t border-border">
              <div>
                <div className="text-2xl font-bold text-accent">100%</div>
                <div className="text-sm text-muted-foreground">Безопасность</div>
              </div>
              <div>
                <div className="text-2xl font-bold text-accent">5мс</div>
                <div className="text-sm text-muted-foreground">Отклик</div>
              </div>
              <div>
                <div className="text-2xl font-bold text-accent">24/7</div>
                <div className="text-sm text-muted-foreground">Поддержка</div>
              </div>
            </div>
          </div>

          {/* Right Visual */}
          <div className="relative h-96 hidden lg:block">
            <div className="absolute inset-0 bg-gradient-to-br from-accent/20 via-accent/5 to-transparent rounded-2xl border border-accent/20" />
            <div className="absolute inset-0 flex items-center justify-center">
              <div className="relative">
                {/* Animated circles */}
                <div className="absolute w-64 h-64 bg-accent/10 rounded-full blur-3xl animate-pulse" />
                <div className="absolute w-48 h-48 bg-accent/5 rounded-full blur-2xl left-12 top-12 animate-pulse" style={{ animationDelay: '1s' }} />
                
                {/* Center icon */}
                <div className="relative z-10 flex items-center justify-center">
                  <div className="w-24 h-24 bg-accent/10 rounded-2xl flex items-center justify-center border border-accent/30">
                    <Lock className="w-12 h-12 text-accent" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Features Section */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mt-20">
          <div className="group p-6 rounded-xl border border-border bg-card/50 hover:bg-card/80 hover:border-accent/30 transition-all">
            <div className="w-12 h-12 bg-accent/10 rounded-lg flex items-center justify-center mb-4 group-hover:bg-accent/20 transition-colors">
              <Shield className="w-6 h-6 text-accent" />
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">Безопасность</h3>
            <p className="text-muted-foreground">Защита паролей с помощью bcrypt и современные стандарты шифрования</p>
          </div>

          <div className="group p-6 rounded-xl border border-border bg-card/50 hover:bg-card/80 hover:border-accent/30 transition-all">
            <div className="w-12 h-12 bg-accent/10 rounded-lg flex items-center justify-center mb-4 group-hover:bg-accent/20 transition-colors">
              <Zap className="w-6 h-6 text-accent" />
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">Скорость</h3>
            <p className="text-muted-foreground">Быстрая обработка запросов на порту 8081 с оптимизированным кодом</p>
          </div>

          <div className="group p-6 rounded-xl border border-border bg-card/50 hover:bg-card/80 hover:border-accent/30 transition-all">
            <div className="w-12 h-12 bg-accent/10 rounded-lg flex items-center justify-center mb-4 group-hover:bg-accent/20 transition-colors">
              <Lock className="w-6 h-6 text-accent" />
            </div>
            <h3 className="text-lg font-semibold text-foreground mb-2">Надежность</h3>
            <p className="text-muted-foreground">Обработка ошибок и стабильная работа при любых условиях</p>
          </div>
        </div>
      </div>

      {/* Footer */}
      <footer className="border-t border-border backdrop-blur-xl bg-background/50">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="flex flex-col md:flex-row justify-between items-center gap-4">
            <p className="text-sm text-muted-foreground">
              AuthApp © 2024. Современная система аутентификации.
            </p>
            <p className="text-sm text-muted-foreground">
              API запущен на порту <span className="text-accent font-semibold">8081</span>
            </p>
          </div>
        </div>
      </footer>
    </main>
  );
}

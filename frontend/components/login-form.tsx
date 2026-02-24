'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth-context';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { AlertCircle, LogIn } from 'lucide-react';

export function LoginForm() {
  const router = useRouter();
  const { login, isLoading, error } = useAuth();
  const [formError, setFormError] = useState<string | null>(null);
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    setFormError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError(null);

    if (!formData.username || !formData.password) {
      setFormError('Имя пользователя и пароль обязательны');
      return;
    }

    try {
      await login({
        username: formData.username,
        password: formData.password,
      });
      router.push('/dashboard');
    } catch (err: any) {
      setFormError(err.message || 'Ошибка входа');
    }
  };

  return (
    <div className="space-y-6">
      {/* Error Message */}
      {(formError || error) && (
        <div className="flex items-start gap-3 p-4 rounded-lg bg-destructive/10 border border-destructive/20">
          <AlertCircle className="w-5 h-5 text-destructive flex-shrink-0 mt-0.5" />
          <p className="text-sm text-destructive">{formError || error}</p>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        {/* Username */}
        <div className="space-y-2">
          <Label htmlFor="username" className="text-foreground font-medium">
            Имя пользователя
          </Label>
          <Input
            id="username"
            name="username"
            type="text"
            placeholder="john_doe"
            value={formData.username}
            onChange={handleChange}
            disabled={isLoading}
            className="h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus:border-accent focus:ring-accent/30"
          />
        </div>

        {/* Password */}
        <div className="space-y-2">
          <Label htmlFor="password" className="text-foreground font-medium">
            Пароль
          </Label>
          <Input
            id="password"
            name="password"
            type="password"
            placeholder="••••••••"
            value={formData.password}
            onChange={handleChange}
            disabled={isLoading}
            className="h-11 bg-card border-border text-foreground placeholder:text-muted-foreground focus:border-accent focus:ring-accent/30"
          />
        </div>

        {/* Submit Button */}
        <Button
          type="submit"
          disabled={isLoading}
          className="w-full h-11 text-base bg-accent hover:bg-accent/90 text-accent-foreground font-semibold rounded-lg transition-all disabled:opacity-50 disabled:cursor-not-allowed"
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 border-2 border-accent-foreground border-t-transparent rounded-full animate-spin" />
              Вход...
            </div>
          ) : (
            <div className="flex items-center gap-2">
              <LogIn className="w-5 h-5" />
              Войти
            </div>
          )}
        </Button>
      </form>

      {/* Info Box */}
      <div className="p-4 rounded-lg bg-card border border-border">
        <p className="text-xs font-semibold text-foreground mb-3">Демо учетные данные:</p>
        <div className="space-y-2 text-xs text-muted-foreground">
          <p>
            <span className="text-accent font-medium">Пользователь:</span> testuser
          </p>
          <p>
            <span className="text-accent font-medium">Пароль:</span> Test1234
          </p>
        </div>
      </div>
    </div>
  );
}

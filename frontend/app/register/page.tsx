import { RegisterForm } from '@/components/register-form';
import Link from 'next/link';

export default function RegisterPage() {
  return (
    <main className="min-h-screen bg-gradient-to-br from-background via-background to-card flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Back Button */}
        <Link href="/" className="inline-flex items-center gap-2 text-muted-foreground hover:text-foreground transition-colors mb-8">
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
          Вернуться
        </Link>

        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-foreground mb-2">Создать аккаунт</h1>
          <p className="text-muted-foreground">Присоединитесь к нашему сервису</p>
        </div>

        {/* Form */}
        <RegisterForm />

        {/* Footer */}
        <div className="mt-6 text-center">
          <p className="text-muted-foreground">
            Уже есть аккаунт?{' '}
            <Link href="/login" className="text-accent hover:text-accent/80 font-semibold transition-colors">
              Войти
            </Link>
          </p>
        </div>
      </div>
    </main>
  );
}

'use client';

import { useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';

import { authClient, userClient, attendanceClient } from '@/lib/api-client';

import {
  AlertCircle,
  Loader2,
  User,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  Clock3,
} from 'lucide-react';

export default function LoginPage() {
  const router = useRouter();

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPass, setShowPass] = useState(false);

  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

  const canSubmit = useMemo(() => {
    return formData.username.trim().length > 0 && formData.password.length > 0 && !isLoading;
  }, [formData.username, formData.password, isLoading]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const payload = {
        username: formData.username.trim(),
        password: formData.password,
      };

      const response: any = await authClient.post('/api/auth/login', payload);

      const accessToken = response?.accessToken;
      const refreshToken = response?.refreshToken;

      if (!accessToken || !refreshToken) {
        setError('Login succeeded but tokens were not returned by server.');
        return;
      }

      authClient.setTokens(accessToken, refreshToken);
      userClient.setTokens(accessToken, refreshToken);
      attendanceClient.setTokens(accessToken, refreshToken);

      const me: any = await userClient.get('/api/internal/users/me');
      const sid = me?.sid;

      if (sid) {
        authClient.setSessionId(sid);
        userClient.setSessionId(sid);
        attendanceClient.setSessionId(sid);
      }

      router.replace('/app/overview');
    } catch (err: any) {
      const msg =
          err?.data?.message ||
          err?.data?.error ||
          err?.message ||
          'Failed to login. Please check your credentials.';
      setError(String(msg));
    } finally {
      setIsLoading(false);
    }
  };

  return (
      <div className="relative min-h-screen overflow-hidden bg-background">
        {/* Background */}
        <div className="pointer-events-none absolute inset-0">
          <div className="absolute -top-40 -right-40 h-[420px] w-[420px] rounded-full bg-primary/20 blur-3xl" />
          <div className="absolute -bottom-52 -left-40 h-[520px] w-[520px] rounded-full bg-blue-500/15 blur-3xl" />
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,rgba(37,99,235,0.12),transparent_60%),radial-gradient(ellipse_at_bottom,rgba(59,130,246,0.10),transparent_55%)]" />
          <div className="absolute inset-0 bg-[linear-gradient(to_bottom,transparent,rgba(0,0,0,0.02))]" />
        </div>

        {/* Content */}
        <div className="relative flex min-h-screen items-center justify-center p-4">
          <Card className="w-full max-w-md border-border/60 bg-white/70 shadow-2xl backdrop-blur-xl dark:bg-card/70">
            <div className="p-7 sm:p-8">
              {/* Header */}
              <div className="mb-6 space-y-2 animate-in fade-in slide-in-from-bottom-2 duration-500">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div className="grid h-10 w-10 place-items-center rounded-2xl bg-primary/10 text-primary">
                      <Clock3 className="h-5 w-5" />
                    </div>
                    <div>
                      <div className="text-lg font-semibold leading-tight">TimeChamp</div>
                      <div className="text-xs text-muted-foreground leading-tight">
                        Attendance • Teams • Analytics
                      </div>
                    </div>
                  </div>

                  <span className="rounded-full border border-border/60 bg-background/60 px-3 py-1 text-xs text-muted-foreground">
                  Secure Login
                </span>
                </div>

                <div className="pt-2">
                  <h1 className="text-2xl font-bold tracking-tight">Welcome back</h1>
                  <p className="text-sm text-muted-foreground">
                    Sign in to continue to your dashboard.
                  </p>
                </div>
              </div>

              {/* Error */}
              {error && (
                  <Alert variant="destructive" className="mb-5 animate-in fade-in duration-300">
                    <AlertCircle className="h-4 w-4" />
                    <AlertDescription>{error}</AlertDescription>
                  </Alert>
              )}

              {/* Form */}
              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Username */}
                <div className="space-y-2 animate-in fade-in slide-in-from-bottom-2 duration-700">
                  <Label htmlFor="username">Username</Label>
                  <div className="relative">
                    <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        id="username"
                        name="username"
                        type="text"
                        placeholder="bob_test"
                        value={formData.username}
                        onChange={handleChange}
                        disabled={isLoading}
                        required
                        autoComplete="username"
                        className="pl-10 h-11 bg-background/60 border-border/70 focus-visible:ring-primary/30"
                    />
                  </div>
                </div>

                {/* Password */}
                <div className="space-y-2 animate-in fade-in slide-in-from-bottom-2 duration-700">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="password">Password</Label>
                    <Link
                        href="/auth/forgot-password"
                        className="text-xs font-medium text-primary hover:underline"
                    >
                      Forgot?
                    </Link>
                  </div>

                  <div className="relative">
                    <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />

                    <Input
                        id="password"
                        name="password"
                        type={showPass ? 'text' : 'password'}
                        placeholder="••••••••"
                        value={formData.password}
                        onChange={handleChange}
                        disabled={isLoading}
                        required
                        autoComplete="current-password"
                        className="pl-10 pr-12 h-11 bg-background/60 border-border/70 focus-visible:ring-primary/30"
                    />

                    <button
                        type="button"
                        onClick={() => setShowPass(v => !v)}
                        className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-2 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition"
                        aria-label={showPass ? 'Hide password' : 'Show password'}
                    >
                      {showPass ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>

                {/* Submit */}
                <div className="pt-2 animate-in fade-in slide-in-from-bottom-2 duration-700">
                  <Button
                      type="submit"
                      disabled={!canSubmit}
                      className="group relative w-full h-11 overflow-hidden rounded-xl"
                  >
                    <span className="absolute inset-0 bg-gradient-to-r from-primary to-blue-500 opacity-100 transition-opacity group-hover:opacity-90" />
                    <span className="absolute inset-0 opacity-0 group-hover:opacity-100 transition-opacity bg-[radial-gradient(circle_at_top,rgba(255,255,255,0.35),transparent_55%)]" />
                    <span className="relative flex items-center justify-center gap-2">
                    {isLoading ? (
                        <>
                          <Loader2 className="h-4 w-4 animate-spin" />
                          Signing in...
                        </>
                    ) : (
                        <>
                          Sign In
                          <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
                        </>
                    )}
                  </span>
                  </Button>

                  <p className="mt-3 text-center text-xs text-muted-foreground">
                    By continuing, you agree to our{' '}
                    <span className="text-foreground/80">Terms</span> and{' '}
                    <span className="text-foreground/80">Privacy</span>.
                  </p>
                </div>
              </form>

              {/* Footer */}
              <div className="mt-6 text-center text-sm animate-in fade-in duration-700">
                <span className="text-muted-foreground">Don&apos;t have an account? </span>
                <Link href="/auth/register" className="text-primary hover:underline font-semibold">
                  Create one
                </Link>
              </div>
            </div>

            {/* Bottom glow line */}
            <div className="h-1 w-full bg-gradient-to-r from-primary/40 via-blue-500/40 to-primary/40" />
          </Card>
        </div>
      </div>
  );
}
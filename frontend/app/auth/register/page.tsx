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
  Mail,
  Lock,
  Eye,
  EyeOff,
  ArrowRight,
  Clock3,
  BadgeCheck,
} from 'lucide-react';

function passwordScore(pw: string) {
  let s = 0;
  if (pw.length >= 8) s++;
  if (/[A-Z]/.test(pw)) s++;
  if (/[0-9]/.test(pw)) s++;
  if (/[^A-Za-z0-9]/.test(pw)) s++;
  return s; // 0..4
}

export default function RegisterPage() {
  const router = useRouter();

  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const [formData, setFormData] = useState({
    username: '',
    email: '',
    firstName: '',
    lastName: '',
    password: '',
    confirmPassword: '',
  });

  const score = useMemo(() => passwordScore(formData.password), [formData.password]);

  const passwordsMatch = useMemo(() => {
    if (!formData.password || !formData.confirmPassword) return true;
    return formData.password === formData.confirmPassword;
  }, [formData.password, formData.confirmPassword]);

  const canSubmit = useMemo(() => {
    const ok =
        formData.firstName.trim() &&
        formData.lastName.trim() &&
        formData.username.trim() &&
        formData.email.trim() &&
        formData.password.length >= 8 &&
        passwordsMatch;

    return Boolean(ok) && !isLoading;
  }, [formData, passwordsMatch, isLoading]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    if (!passwordsMatch) {
      setError('Passwords do not match');
      return;
    }

    if (formData.password.length < 8) {
      setError('Password must be at least 8 characters');
      return;
    }

    setIsLoading(true);

    try {
      const { confirmPassword, ...registerData } = formData;

      // trims
      const payload = {
        ...registerData,
        username: registerData.username.trim(),
        email: registerData.email.trim(),
        firstName: registerData.firstName.trim(),
        lastName: registerData.lastName.trim(),
      };

      const response: any = await authClient.post('/api/auth/register', payload);

      const accessToken = response?.accessToken;
      const refreshToken = response?.refreshToken;

      if (!accessToken || !refreshToken) {
        setError('Register succeeded but tokens were not returned by server.');
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
          'Failed to create account. Please try again.';
      setError(String(msg));
    } finally {
      setIsLoading(false);
    }
  };

  const strengthLabel =
      score <= 1 ? 'Weak' : score === 2 ? 'Fair' : score === 3 ? 'Good' : 'Strong';

  return (
      <div className="relative min-h-screen overflow-hidden bg-background">
        {/* Background */}
        <div className="pointer-events-none absolute inset-0">
          <div className="absolute -top-40 -right-40 h-[420px] w-[420px] rounded-full bg-primary/20 blur-3xl" />
          <div className="absolute -bottom-52 -left-40 h-[520px] w-[520px] rounded-full bg-blue-500/15 blur-3xl" />
          <div className="absolute inset-0 bg-[radial-gradient(ellipse_at_top,rgba(37,99,235,0.12),transparent_60%),radial-gradient(ellipse_at_bottom,rgba(59,130,246,0.10),transparent_55%)]" />
          <div className="absolute inset-0 bg-[linear-gradient(to_bottom,transparent,rgba(0,0,0,0.02))]" />
        </div>

        <div className="relative flex min-h-screen items-center justify-center p-4">
          <Card className="w-full max-w-xl border-border/60 bg-white/70 shadow-2xl backdrop-blur-xl dark:bg-card/70">
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
                        Create your workspace account
                      </div>
                    </div>
                  </div>

                  <span className="inline-flex items-center gap-2 rounded-full border border-border/60 bg-background/60 px-3 py-1 text-xs text-muted-foreground">
                  <BadgeCheck className="h-3.5 w-3.5 text-primary" />
                  Quick setup
                </span>
                </div>

                <div className="pt-2">
                  <h1 className="text-2xl font-bold tracking-tight">Create account</h1>
                  <p className="text-sm text-muted-foreground">
                    Start tracking attendance and managing your team in minutes.
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

              <form onSubmit={handleSubmit} className="space-y-4">
                {/* Names */}
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 animate-in fade-in slide-in-from-bottom-2 duration-700">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">First name</Label>
                    <div className="relative">
                      <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                          id="firstName"
                          name="firstName"
                          placeholder="Alice"
                          value={formData.firstName}
                          onChange={handleChange}
                          disabled={isLoading}
                          required
                          className="pl-10 h-11 bg-background/60 border-border/70 focus-visible:ring-primary/30"
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="lastName">Last name</Label>
                    <div className="relative">
                      <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                          id="lastName"
                          name="lastName"
                          placeholder="Johnson"
                          value={formData.lastName}
                          onChange={handleChange}
                          disabled={isLoading}
                          required
                          className="pl-10 h-11 bg-background/60 border-border/70 focus-visible:ring-primary/30"
                      />
                    </div>
                  </div>
                </div>

                {/* Username + Email */}
                <div className="space-y-4 animate-in fade-in slide-in-from-bottom-2 duration-700">
                  <div className="space-y-2">
                    <Label htmlFor="username">Username</Label>
                    <div className="relative">
                      <User className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                          id="username"
                          name="username"
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

                  <div className="space-y-2">
                    <Label htmlFor="email">Email</Label>
                    <div className="relative">
                      <Mail className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                          id="email"
                          name="email"
                          type="email"
                          placeholder="name@company.com"
                          value={formData.email}
                          onChange={handleChange}
                          disabled={isLoading}
                          required
                          autoComplete="email"
                          className="pl-10 h-11 bg-background/60 border-border/70 focus-visible:ring-primary/30"
                      />
                    </div>
                  </div>
                </div>

                {/* Passwords */}
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 animate-in fade-in slide-in-from-bottom-2 duration-700">
                  <div className="space-y-2">
                    <Label htmlFor="password">Password</Label>
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
                          autoComplete="new-password"
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

                    {/* Strength meter */}
                    <div className="pt-1">
                      <div className="flex items-center justify-between text-xs text-muted-foreground">
                        <span>Password strength</span>
                        <span className="font-medium text-foreground/80">{strengthLabel}</span>
                      </div>
                      <div className="mt-2 h-2 w-full rounded-full bg-muted overflow-hidden">
                        <div
                            className="h-2 rounded-full bg-primary transition-all"
                            style={{ width: `${(score / 4) * 100}%` }}
                        />
                      </div>
                      <p className="mt-2 text-xs text-muted-foreground">
                        Tip: add uppercase, numbers, and symbols for stronger password.
                      </p>
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="confirmPassword">Confirm password</Label>
                    <div className="relative">
                      <Lock className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                      <Input
                          id="confirmPassword"
                          name="confirmPassword"
                          type={showConfirm ? 'text' : 'password'}
                          placeholder="••••••••"
                          value={formData.confirmPassword}
                          onChange={handleChange}
                          disabled={isLoading}
                          required
                          autoComplete="new-password"
                          className={`pl-10 pr-12 h-11 bg-background/60 border-border/70 focus-visible:ring-primary/30 ${
                              !passwordsMatch ? 'border-destructive/70 focus-visible:ring-destructive/30' : ''
                          }`}
                      />
                      <button
                          type="button"
                          onClick={() => setShowConfirm(v => !v)}
                          className="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-2 text-muted-foreground hover:text-foreground hover:bg-muted/50 transition"
                          aria-label={showConfirm ? 'Hide password' : 'Show password'}
                      >
                        {showConfirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                      </button>
                    </div>

                    {!passwordsMatch && (
                        <p className="text-xs text-destructive mt-1">Passwords do not match</p>
                    )}
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
                          Creating account...
                        </>
                    ) : (
                        <>
                          Create account
                          <ArrowRight className="h-4 w-4 transition-transform group-hover:translate-x-0.5" />
                        </>
                    )}
                  </span>
                  </Button>

                  <p className="mt-3 text-center text-xs text-muted-foreground">
                    By creating an account, you agree to our{' '}
                    <span className="text-foreground/80">Terms</span> and{' '}
                    <span className="text-foreground/80">Privacy</span>.
                  </p>
                </div>
              </form>

              {/* Footer */}
              <div className="mt-6 text-center text-sm animate-in fade-in duration-700">
                <span className="text-muted-foreground">Already have an account? </span>
                <Link href="/auth/login" className="text-primary hover:underline font-semibold">
                  Sign in
                </Link>
              </div>
            </div>

            <div className="h-1 w-full bg-gradient-to-r from-primary/40 via-blue-500/40 to-primary/40" />
          </Card>
        </div>
      </div>
  );
}
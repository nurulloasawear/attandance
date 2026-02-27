'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card } from '@/components/ui/card';
import { authClient, userClient, attendanceClient } from '@/lib/api-client';
import { AlertCircle, Loader2 } from 'lucide-react';
import { Alert, AlertDescription } from '@/components/ui/alert';

export default function LoginPage() {
  const router = useRouter();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [formData, setFormData] = useState({
    username: '',
    password: '',
  });

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
      <div className="min-h-screen bg-secondary flex items-center justify-center p-4">
        <Card className="w-full max-w-md bg-white border border-border shadow-lg">
          <div className="p-8 space-y-6">
            <div className="text-center space-y-2">
              <h1 className="text-3xl font-bold text-primary">TimeChamp</h1>
              <h2 className="text-xl font-semibold">Welcome Back</h2>
              <p className="text-muted-foreground">Sign in to your account to continue</p>
            </div>

            {error && (
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertDescription>{error}</AlertDescription>
                </Alert>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input
                    id="username"
                    name="username"
                    type="text"
                    placeholder="john.doe"
                    value={formData.username}
                    onChange={handleChange}
                    disabled={isLoading}
                    required
                    autoComplete="username"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                    id="password"
                    name="password"
                    type="password"
                    placeholder="••••••••"
                    value={formData.password}
                    onChange={handleChange}
                    disabled={isLoading}
                    required
                    autoComplete="current-password"
                />
              </div>

              <Button type="submit" className="w-full" disabled={isLoading}>
                {isLoading ? (
                    <>
                      <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                      Signing in...
                    </>
                ) : (
                    'Sign In'
                )}
              </Button>
            </form>

            <div className="text-center text-sm">
              <span className="text-muted-foreground">Don't have an account? </span>
              <Link href="/auth/register" className="text-primary hover:underline font-medium">
                Sign up
              </Link>
            </div>

            <div className="text-center">
              <Link href="/auth/forgot-password" className="text-sm text-primary hover:underline">
                Forgot password?
              </Link>
            </div>
          </div>
        </Card>
      </div>
  );
}
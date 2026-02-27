'use client';

import { DashboardSidebar } from '@/components/dashboard-sidebar';
import { DashboardTopbar } from '@/components/dashboard-topbar';
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { userClient } from '@/lib/api-client';

export default function AppLayout({
                                    children,
                                  }: {
  children: React.ReactNode;
}) {
  const router = useRouter();
  const [user, setUser] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const init = async () => {
      const token =
          typeof window !== 'undefined'
              ? sessionStorage.getItem('accessToken')
              : null;

      if (!token) {
        router.replace('/auth/login');
        return;
      }

      try {
        const response = await userClient.get(
            '/api/internal/users/me/profile'
        );
        setUser(response);
      } catch (error) {
        sessionStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        router.replace('/auth/login');
      } finally {
        setIsLoading(false);
      }
    };

    init();
  }, [router]);

  if (isLoading) {
    return (
        <div className="flex items-center justify-center min-h-screen bg-secondary">
          <div className="text-center">
            <div className="w-8 h-8 border-4 border-border border-t-primary rounded-full animate-spin mx-auto mb-4" />
            <p className="text-muted-foreground">Loading...</p>
          </div>
        </div>
    );
  }

  if (!user) return null;

  return (
      <div className="flex h-screen bg-secondary">
        <DashboardSidebar />
        <div className="flex-1 flex flex-col overflow-hidden">
          <DashboardTopbar user={user} />
          <main className="flex-1 overflow-auto">{children}</main>
        </div>
      </div>
  );
}
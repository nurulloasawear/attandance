'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { cn } from '@/lib/utils';
import {
  BarChart3,
  Users,
  Clock,
  FileText,
  Settings,
  Home,
  ChevronLeft,
  HelpCircle,
} from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';

type NavItem = {
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
  adminOnly?: boolean;
};

const navItems: NavItem[] = [
  { href: '/app/overview', label: 'Overview', icon: Home },
  { href: '/app/attendance', label: 'Attendance', icon: Clock },
  { href: '/app/timesheets', label: 'Timesheets', icon: FileText },
  { href: '/app/team', label: 'Team', icon: Users, adminOnly: true },
  { href: '/app/reports', label: 'Reports', icon: BarChart3 },
  { href: '/app/settings', label: 'Settings', icon: Settings },
];

function isPathActive(pathname: string, href: string) {
  if (href === '/app/overview') return pathname === href || pathname === '/app';
  return pathname === href || pathname.startsWith(href + '/');
}

export function DashboardSidebar({
                                   isAdmin = true, // TODO: прокинь реальную роль из /me (role === ROLE_ADMIN || ROLE_SUPER_ADMIN)
                                 }: {
  isAdmin?: boolean;
}) {
  const pathname = usePathname();
  const [collapsed, setCollapsed] = useState(false);

  // Восстанавливаем состояние после перезагрузки
  useEffect(() => {
    try {
      const v = localStorage.getItem('tc.sidebar.collapsed');
      if (v === '1') setCollapsed(true);
    } catch {}
  }, []);

  useEffect(() => {
    try {
      localStorage.setItem('tc.sidebar.collapsed', collapsed ? '1' : '0');
    } catch {}
  }, [collapsed]);

  const visibleItems = useMemo(
      () => navItems.filter((x) => !x.adminOnly || isAdmin),
      [isAdmin]
  );

  return (
      <aside
          className={cn(
              'bg-sidebar border-r border-sidebar-border h-screen sticky top-0 flex flex-col',
              'transition-[width] duration-300 ease-in-out',
              collapsed ? 'w-[76px]' : 'w-64'
          )}
      >
        {/* Header */}
        <div className="px-4 py-4 border-b border-sidebar-border">
          <div className="flex items-center justify-between gap-3">
            <Link
                href="/app/overview"
                className={cn(
                    'flex items-center gap-3 rounded-xl px-2 py-2 hover:bg-sidebar-accent transition-colors',
                    collapsed && 'justify-center px-2'
                )}
            >
              <div className="h-9 w-9 rounded-xl bg-sidebar-primary text-sidebar-primary-foreground flex items-center justify-center font-bold shadow-sm">
                TC
              </div>
              {!collapsed && (
                  <div className="leading-tight">
                    <div className="font-semibold text-sm">TimeChamp</div>
                    <div className="text-xs text-sidebar-foreground/70">Dashboard</div>
                  </div>
              )}
            </Link>

            <button
                type="button"
                onClick={() => setCollapsed((v) => !v)}
                className={cn(
                    'h-9 w-9 rounded-xl border border-sidebar-border bg-card/40 hover:bg-sidebar-accent transition-colors',
                    'inline-flex items-center justify-center'
                )}
                aria-label={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
                title={collapsed ? 'Expand' : 'Collapse'}
            >
              <ChevronLeft
                  className={cn(
                      'h-4 w-4 transition-transform duration-300',
                      collapsed ? 'rotate-180' : 'rotate-0'
                  )}
              />
            </button>
          </div>
        </div>

        {/* Nav */}
        <nav className="flex-1 px-3 py-3 overflow-y-auto">
          <div className="space-y-1">
            {visibleItems.map((item) => {
              const Icon = item.icon;
              const active = isPathActive(pathname, item.href);

              return (
                  <Link
                      key={item.href}
                      href={item.href}
                      className={cn(
                          'group flex items-center gap-3 rounded-xl px-3 py-2.5 transition-colors',
                          active
                              ? 'bg-sidebar-primary text-sidebar-primary-foreground shadow-sm'
                              : 'text-sidebar-foreground hover:bg-sidebar-accent'
                      )}
                      title={collapsed ? item.label : undefined}
                      aria-current={active ? 'page' : undefined}
                  >
                    <Icon className="h-5 w-5 shrink-0" />
                    {!collapsed && (
                        <span className="text-sm font-medium">{item.label}</span>
                    )}

                    {/* Active indicator */}
                    {!collapsed && active && (
                        <span className="ml-auto h-2 w-2 rounded-full bg-sidebar-primary-foreground/80" />
                    )}
                  </Link>
              );
            })}
          </div>
        </nav>

        {/* Footer / Help */}
        <div className="p-3 border-t border-sidebar-border">
          <div
              className={cn(
                  'rounded-2xl border border-sidebar-border bg-card/40 p-3',
                  collapsed ? 'flex items-center justify-center' : ''
              )}
          >
            {collapsed ? (
                <a
                    href="#"
                    className="h-10 w-10 inline-flex items-center justify-center rounded-xl hover:bg-sidebar-accent transition-colors"
                    title="Help & Support"
                    aria-label="Help & Support"
                >
                  <HelpCircle className="h-5 w-5" />
                </a>
            ) : (
                <div className="flex items-start gap-3">
                  <div className="mt-0.5 h-9 w-9 rounded-xl bg-sidebar-accent flex items-center justify-center">
                    <HelpCircle className="h-5 w-5" />
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-semibold leading-tight">Need help?</p>
                    <p className="text-xs text-sidebar-foreground/70">
                      Contact support anytime
                    </p>
                    <a
                        href="#"
                        className="mt-2 inline-flex text-xs font-medium text-primary hover:underline"
                    >
                      Open support
                    </a>
                  </div>
                </div>
            )}
          </div>
        </div>
      </aside>
  );
}
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
  ChevronDown,
} from 'lucide-react';
import { useState } from 'react';

const navItems = [
  { href: '/app/overview', label: 'Overview', icon: Home },
  { href: '/app/attendance', label: 'Attendance', icon: Clock },
  { href: '/app/timesheets', label: 'Timesheets', icon: FileText },
  { href: '/app/team', label: 'Team', icon: Users, adminOnly: true },
  { href: '/app/reports', label: 'Reports', icon: BarChart3 },
  { href: '/app/settings', label: 'Settings', icon: Settings },
];

export function DashboardSidebar() {
  const pathname = usePathname();
  const [collapsed, setCollapsed] = useState(false);

  return (
    <aside
      className={cn(
        'bg-sidebar border-r border-sidebar-border transition-all duration-300 flex flex-col h-screen sticky top-0',
        collapsed ? 'w-20' : 'w-64'
      )}
    >
      {/* Logo Section */}
      <div className="p-6 border-b border-sidebar-border flex items-center justify-between">
        {!collapsed && (
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-sidebar-primary flex items-center justify-center text-sidebar-primary-foreground font-bold">
              TC
            </div>
            <span className="font-bold text-lg">TimeChamp</span>
          </div>
        )}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="p-1 hover:bg-sidebar-accent rounded-lg transition-colors"
        >
          <ChevronDown
            className={cn(
              'w-5 h-5 transition-transform',
              collapsed ? 'rotate-90' : ''
            )}
          />
        </button>
      </div>

      {/* Navigation */}
      <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = pathname === item.href || pathname.startsWith(item.href + '/');
          
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                'flex items-center gap-3 px-4 py-3 rounded-lg transition-colors whitespace-nowrap',
                isActive
                  ? 'bg-sidebar-primary text-sidebar-primary-foreground'
                  : 'text-sidebar-foreground hover:bg-sidebar-accent'
              )}
              title={collapsed ? item.label : undefined}
            >
              <Icon className="w-5 h-5 flex-shrink-0" />
              {!collapsed && <span>{item.label}</span>}
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className="p-4 border-t border-sidebar-border">
        <div className={cn(
          'px-4 py-3 rounded-lg bg-sidebar-accent text-sidebar-accent-foreground text-sm',
          collapsed && 'text-center'
        )}>
          {!collapsed && <p className="font-medium">Need help?</p>}
          {!collapsed && <p className="text-xs opacity-75">Contact support</p>}
        </div>
      </div>
    </aside>
  );
}

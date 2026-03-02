'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Plus, MoreVertical, Search, Loader2, RefreshCw } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';
import { userClient } from '@/lib/api-client';

type TeamMember = {
  id?: string | number;
  publicId?: string;
  username?: string;
  firstName?: string;
  lastName?: string;
  email?: string;

  role?: string;          // если backend даёт
  department?: string;    // если backend даёт
  status?: 'Active' | 'Away' | 'Inactive' | string;
  lastSeen?: string;      // ISO или строка

  createdAt?: string;     // ISO, если есть
};

const USERS_ENDPOINT = '/api/internal/users';

function fullName(u: TeamMember) {
  const fn = (u.firstName || '').trim();
  const ln = (u.lastName || '').trim();
  const n = `${fn} ${ln}`.trim();
  return n || u.username || u.email || 'Unknown';
}

function safeLower(v?: string) {
  return (v || '').toLowerCase();
}

function monthKey(d: Date) {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
}

export default function TeamPage() {
  const [items, setItems] = useState<TeamMember[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState('');

  const load = async (opts?: { silent?: boolean }) => {
    const silent = opts?.silent ?? false;
    setError('');

    if (silent) setIsRefreshing(true);
    else setIsLoading(true);

    try {
      // ⚠️ Если у тебя пагинация — поменяй тут на { params: { page, size } }
      const res: any = await userClient.get(USERS_ENDPOINT);

      // Поддержим разные форматы ответа:
      // 1) массив
      // 2) { items: [...] }
      // 3) { content: [...] } (spring pageable)
      const list: TeamMember[] =
          Array.isArray(res) ? res : (res?.items || res?.content || []);

      setItems(list);
    } catch (e: any) {
      const msg =
          e?.data?.message ||
          e?.data?.error ||
          e?.message ||
          'Failed to load team members';
      setError(String(msg));
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const filteredMembers = useMemo(() => {
    const q = safeLower(searchQuery.trim());
    if (!q) return items;

    return items.filter((m) => {
      const n = safeLower(fullName(m));
      const e = safeLower(m.email);
      const u = safeLower(m.username);
      return n.includes(q) || e.includes(q) || u.includes(q);
    });
  }, [items, searchQuery]);

  const stats = useMemo(() => {
    const total = items.length;

    const activeNow = items.filter((m) =>
        safeLower(m.status).includes('active')
    ).length;

    const departments = new Set(
        items.map((m) => (m.department || '').trim()).filter(Boolean)
    ).size;

    // "New this month" — если есть createdAt, считаем, иначе —
    const now = new Date();
    const currentMonth = monthKey(now);
    const newThisMonth = items.some((m) => m.createdAt)
        ? items.filter((m) => {
          if (!m.createdAt) return false;
          const d = new Date(m.createdAt);
          return !isNaN(d.getTime()) && monthKey(d) === currentMonth;
        }).length
        : null;

    return { total, activeNow, departments, newThisMonth };
  }, [items]);

  // ---- Actions (подставишь реальные endpoints) ----
  const onAddMember = () => {
    // сюда обычно открыть modal / route
    alert('TODO: open "Add Member" modal / page');
  };

  const onViewDetails = (m: TeamMember) => {
    alert(`TODO: view details for ${fullName(m)} (${m.publicId || m.id || m.username})`);
  };

  const onEdit = (m: TeamMember) => {
    alert(`TODO: edit ${fullName(m)}`);
  };

  const onChangeRole = (m: TeamMember) => {
    alert(`TODO: change role for ${fullName(m)}`);
  };

  const onRemove = async (m: TeamMember) => {
    // Примерно так будет выглядеть:
    // await userClient.delete(`/api/internal/users/${m.publicId}`);
    alert(`TODO: remove ${fullName(m)}`);
  };

  return (
      <div className="p-6 space-y-6 max-w-7xl">
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-2">
            <h1 className="text-3xl font-bold">Team Management</h1>
            <p className="text-muted-foreground">Manage team members and their roles</p>
          </div>

          <div className="flex items-center gap-2">
            <Button
                variant="outline"
                className="gap-2"
                onClick={() => load({ silent: true })}
                disabled={isRefreshing || isLoading}
            >
              {isRefreshing ? <Loader2 className="w-4 h-4 animate-spin" /> : <RefreshCw className="w-4 h-4" />}
              Refresh
            </Button>

            <Button className="gap-2" onClick={onAddMember}>
              <Plus className="w-4 h-4" />
              Add Member
            </Button>
          </div>
        </div>

        {/* Search */}
        <Card className="p-4 bg-white border border-border">
          <div className="relative">
            <Search className="absolute left-3 top-3 w-4 h-4 text-muted-foreground" />
            <Input
                placeholder="Search members by name / email / username..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-10"
            />
          </div>
        </Card>

        {/* Error */}
        {error && (
            <Card className="p-4 border border-destructive/30 bg-destructive/5">
              <p className="text-sm text-destructive font-medium">Error</p>
              <p className="text-sm text-muted-foreground mt-1">{error}</p>
              <div className="mt-3">
                <Button variant="outline" onClick={() => load()} className="gap-2">
                  <RefreshCw className="w-4 h-4" />
                  Try again
                </Button>
              </div>
            </Card>
        )}

        {/* Stats */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Total Members</p>
            <p className="text-3xl font-bold mt-2">{stats.total}</p>
          </Card>
          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Active Now</p>
            <p className="text-3xl font-bold mt-2">{stats.activeNow}</p>
          </Card>
          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Departments</p>
            <p className="text-3xl font-bold mt-2">{stats.departments}</p>
          </Card>
          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">New This Month</p>
            <p className="text-3xl font-bold mt-2">{stats.newThisMonth === null ? '—' : stats.newThisMonth}</p>
          </Card>
        </div>

        {/* Team List */}
        <Card className="p-6 bg-white border border-border">
          <div className="space-y-4">
            <div className="flex items-center justify-between gap-4">
              <h3 className="text-lg font-semibold">Team Members</h3>
              {isLoading && (
                  <div className="flex items-center gap-2 text-sm text-muted-foreground">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Loading...
                  </div>
              )}
            </div>

            {!isLoading && !error && filteredMembers.length === 0 && (
                <div className="py-10 text-center">
                  <p className="text-sm text-muted-foreground">No members found.</p>
                </div>
            )}

            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 font-medium text-muted-foreground">Name</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Email</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Role</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Department</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Status</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Last Seen</th>
                  <th className="text-right py-3 font-medium text-muted-foreground">Actions</th>
                </tr>
                </thead>

                <tbody>
                {filteredMembers.map((member) => {
                  const name = fullName(member);
                  const email = member.email || '—';
                  const role = member.role || '—';
                  const dept = member.department || '—';
                  const status = member.status || '—';
                  const lastSeen = member.lastSeen || '—';

                  const statusLower = safeLower(String(status));
                  const statusClass =
                      statusLower.includes('active')
                          ? 'bg-green-100 text-green-700'
                          : statusLower.includes('away')
                              ? 'bg-amber-100 text-amber-700'
                              : 'bg-gray-100 text-gray-700';

                  return (
                      <tr
                          key={String(member.publicId || member.id || member.username || email)}
                          className="border-b border-border hover:bg-secondary/60 transition-colors"
                      >
                        <td className="py-4 font-medium">{name}</td>
                        <td className="py-4 text-muted-foreground">{email}</td>
                        <td className="py-4">
                        <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs font-medium">
                          {role}
                        </span>
                        </td>
                        <td className="py-4 text-muted-foreground">{dept}</td>
                        <td className="py-4">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${statusClass}`}>
                          {String(status)}
                        </span>
                        </td>
                        <td className="py-4 text-muted-foreground">{lastSeen}</td>
                        <td className="py-4 text-right">
                          <DropdownMenu>
                            <DropdownMenuTrigger asChild>
                              <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                                <MoreVertical className="h-4 w-4" />
                              </Button>
                            </DropdownMenuTrigger>
                            <DropdownMenuContent align="end">
                              <DropdownMenuItem onClick={() => onViewDetails(member)}>
                                View Details
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => onEdit(member)}>
                                Edit
                              </DropdownMenuItem>
                              <DropdownMenuItem onClick={() => onChangeRole(member)}>
                                Change Role
                              </DropdownMenuItem>
                              <DropdownMenuItem
                                  className="text-destructive"
                                  onClick={() => onRemove(member)}
                              >
                                Remove
                              </DropdownMenuItem>
                            </DropdownMenuContent>
                          </DropdownMenu>
                        </td>
                      </tr>
                  );
                })}
                </tbody>
              </table>
            </div>
          </div>
        </Card>
      </div>
  );
}
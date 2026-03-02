'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
} from 'recharts';
import { Clock, AlertCircle, CheckCircle2, RefreshCw, Loader2 } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { attendanceClient } from '@/lib/api-client';

type DayHours = { day: string; hours: number; target?: number };
type PieItem = { name: 'Present' | 'Late' | 'Absent'; value: number; fill?: string };
type ActivityItem = { time: string; event: string; status: 'success' | 'neutral' | 'danger' };

type OverviewResponse = {
  kpi: {
    todayStatus?: string;        // "Present"
    checkedInAt?: string;        // "09:00"
    hoursToday?: number;         // 4.5
    weekHours?: number;          // 39.5
    weekOvertimeHours?: number;  // 2
    punctualityStatus?: string;  // "On Time"
  };

  hoursLast7Days: DayHours[];
  teamAttendance: {
    presentPercent: number;
    latePercent: number;
    absentPercent: number;
  };

  recentActivity: ActivityItem[];
};

const KpiCard = ({ icon: Icon, title, value, subtitle, action }: any) => (
    <Card className="p-6 bg-white border border-border hover:shadow-lg transition-shadow">
      <div className="flex items-start justify-between">
        <div className="space-y-2 flex-1">
          <p className="text-muted-foreground text-sm">{title}</p>
          <p className="text-3xl font-bold text-foreground">{value}</p>
          {subtitle && <p className="text-xs text-muted-foreground">{subtitle}</p>}
        </div>
        <div className="w-12 h-12 rounded-lg bg-secondary flex items-center justify-center text-primary">
          <Icon className="w-6 h-6" />
        </div>
      </div>
      {action && <div className="mt-4 pt-4 border-t border-border">{action}</div>}
    </Card>
);

function toISODate(d: Date) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function startOfWeek(d: Date) {
  // Mon-based
  const day = (d.getDay() + 6) % 7; // Mon=0..Sun=6
  const x = new Date(d);
  x.setDate(d.getDate() - day);
  x.setHours(0, 0, 0, 0);
  return x;
}
function endOfWeek(d: Date) {
  const s = startOfWeek(d);
  const e = new Date(s);
  e.setDate(s.getDate() + 6);
  e.setHours(23, 59, 59, 999);
  return e;
}

export default function OverviewPage() {
  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState('');

  const [data, setData] = useState<OverviewResponse>({
    kpi: {},
    hoursLast7Days: [],
    teamAttendance: { presentPercent: 0, latePercent: 0, absentPercent: 0 },
    recentActivity: [],
  });

  const now = new Date();
  const from = useMemo(() => toISODate(startOfWeek(now)), [now]);
  const to = useMemo(() => toISODate(endOfWeek(now)), [now]);

  const load = async (opts?: { silent?: boolean }) => {
    const silent = opts?.silent ?? false;
    setError('');
    if (silent) setIsRefreshing(true);
    else setIsLoading(true);

    try {
      // ✅ TODO: поменяй endpoint под свой backend
      // Например: /api/internal/attendance/overview или /api/dashboard/overview
      const res: any = await attendanceClient.get('/api/internal/dashboard/overview', {
        params: { from, to },
      });

      // Нормализация под разные ответы
      const normalized: OverviewResponse = {
        kpi: res?.kpi || res?.summary || {},
        hoursLast7Days: res?.hoursLast7Days || res?.chartData || [],
        teamAttendance: res?.teamAttendance || res?.attendance || {
          presentPercent: 0,
          latePercent: 0,
          absentPercent: 0,
        },
        recentActivity: res?.recentActivity || res?.events || [],
      };

      setData(normalized);
    } catch (e: any) {
      setError(e?.data?.message || e?.data?.error || e?.message || 'Failed to load overview');
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  useEffect(() => {
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const pieData: PieItem[] = useMemo(() => {
    const t = data.teamAttendance || { presentPercent: 0, latePercent: 0, absentPercent: 0 };
    return [
      { name: 'Present', value: Number(t.presentPercent) || 0, fill: '#2563eb' },
      { name: 'Late', value: Number(t.latePercent) || 0, fill: '#3b82f6' },
      { name: 'Absent', value: Number(t.absentPercent) || 0, fill: '#f3f4f6' },
    ];
  }, [data.teamAttendance]);

  const kpi = data.kpi || {};

  return (
      <div className="p-6 space-y-6 max-w-7xl">
        {/* Header */}
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-2">
            <h1 className="text-3xl font-bold">Welcome Back</h1>
            <p className="text-muted-foreground">
              Here's your attendance overview for this week ({from} → {to})
            </p>
          </div>

          <Button
              variant="outline"
              className="gap-2"
              onClick={() => load({ silent: true })}
              disabled={isLoading || isRefreshing}
          >
            {isRefreshing ? <Loader2 className="w-4 h-4 animate-spin" /> : <RefreshCw className="w-4 h-4" />}
            Refresh
          </Button>
        </div>

        {/* Error */}
        {error && (
            <Card className="p-4 border border-destructive/30 bg-destructive/5">
              <p className="text-sm text-destructive font-medium">Error</p>
              <p className="text-sm text-muted-foreground mt-1">{error}</p>
            </Card>
        )}

        {/* KPI Cards */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <KpiCard
              icon={CheckCircle2}
              title="Today"
              value={kpi.todayStatus ?? (isLoading ? 'Loading...' : '—')}
              subtitle={kpi.checkedInAt ? `Checked in at ${kpi.checkedInAt}` : undefined}
          />
          <KpiCard
              icon={Clock}
              title="Hours Today"
              value={kpi.hoursToday != null ? `${Number(kpi.hoursToday).toFixed(1)}h` : (isLoading ? 'Loading...' : '—')}
              subtitle={kpi.todayStatus ? 'Still working' : undefined}
          />
          <KpiCard
              icon={Clock}
              title="This Week"
              value={kpi.weekHours != null ? `${Number(kpi.weekHours).toFixed(1)}h` : (isLoading ? 'Loading...' : '—')}
              subtitle={
                kpi.weekOvertimeHours != null ? `${Number(kpi.weekOvertimeHours).toFixed(1)} hours overtime` : undefined
              }
          />
          <KpiCard
              icon={AlertCircle}
              title="Status"
              value={kpi.punctualityStatus ?? (isLoading ? 'Loading...' : '—')}
              subtitle={kpi.punctualityStatus ? 'Delays monitoring enabled' : undefined}
          />
        </div>

        {/* Charts Section */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Hours Chart */}
          <Card className="lg:col-span-2 p-6 bg-white border border-border">
            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-semibold">Hours Tracked</h3>
                <p className="text-sm text-muted-foreground">Last 7 days</p>
              </div>

              {isLoading ? (
                  <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                    <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading chart...
                  </div>
              ) : data.hoursLast7Days.length === 0 ? (
                  <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                    No chart data for this week.
                  </div>
              ) : (
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={data.hoursLast7Days}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="day" />
                      <YAxis />
                      <Tooltip />
                      <Bar dataKey="hours" radius={[8, 8, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
              )}
            </div>
          </Card>

          {/* Attendance Pie Chart */}
          <Card className="p-6 bg-white border border-border">
            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-semibold">Attendance</h3>
                <p className="text-sm text-muted-foreground">Team overview</p>
              </div>

              {isLoading ? (
                  <div className="h-[300px] flex items-center justify-center text-muted-foreground">
                    <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading chart...
                  </div>
              ) : (
                  <>
                    <ResponsiveContainer width="100%" height={300}>
                      <PieChart>
                        <Pie
                            data={pieData}
                            cx="50%"
                            cy="50%"
                            innerRadius={60}
                            outerRadius={90}
                            dataKey="value"
                        >
                          {pieData.map((entry, idx) => (
                              <Cell key={`cell-${idx}`} fill={entry.fill} />
                          ))}
                        </Pie>
                      </PieChart>
                    </ResponsiveContainer>

                    <div className="space-y-2 text-sm">
                      {pieData.map((item) => (
                          <div key={item.name} className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                              <div className="w-3 h-3 rounded-full" style={{ backgroundColor: item.fill }} />
                              <span className="text-muted-foreground">{item.name}</span>
                            </div>
                            <span className="font-medium">{item.value}%</span>
                          </div>
                      ))}
                    </div>
                  </>
              )}
            </div>
          </Card>
        </div>

        {/* Recent Activity */}
        <Card className="p-6 bg-white border border-border">
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold">Recent Activity</h3>
              <p className="text-sm text-muted-foreground">Latest attendance events</p>
            </div>

            {isLoading ? (
                <div className="py-8 flex items-center justify-center text-muted-foreground">
                  <Loader2 className="w-5 h-5 animate-spin mr-2" /> Loading activity...
                </div>
            ) : (data.recentActivity || []).length === 0 ? (
                <div className="py-8 text-center text-muted-foreground">
                  No recent events.
                </div>
            ) : (
                <div className="space-y-3">
                  {(data.recentActivity || []).map((item, idx) => (
                      <div
                          key={idx}
                          className="flex items-center justify-between py-3 border-b border-border last:border-0"
                      >
                        <div className="flex items-center gap-3">
                          <div
                              className={`w-2 h-2 rounded-full ${
                                  item.status === 'success'
                                      ? 'bg-green-500'
                                      : item.status === 'danger'
                                          ? 'bg-red-500'
                                          : 'bg-blue-500'
                              }`}
                          />
                          <span className="text-foreground">{item.event}</span>
                        </div>
                        <span className="text-sm text-muted-foreground">{item.time}</span>
                      </div>
                  ))}
                </div>
            )}
          </div>
        </Card>
      </div>
  );
}
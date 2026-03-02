'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import {
  BarChart,
  Bar,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { Download, Loader2, RefreshCw } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { attendanceClient } from '@/lib/api-client';

type MonthlyTrend = {
  month: string;      // "Jan" or "2026-01"
  hours: number;
  target?: number;
  overtime?: number;
};

type DeptStats = {
  department: string;
  present: number;
  absent: number;
  late: number;
};

type Performer = {
  name: string;
  hours?: number;
  overtime?: number;
};

type Attention = {
  name: string;
  status: string;
  count: string | number;
};

type ReportsResponse = {
  summary: {
    avgDailyHours?: number;      // 8.1
    totalOvertime?: number;      // 28
    attendanceRate?: number;     // 98.2
    punctuality?: number;        // 95.5
    deltaAvgDailyHours?: number; // +0.1
    deltaAttendanceRate?: number;// +2.1
  };

  monthlyTrends: MonthlyTrend[];
  departmentAttendance: DeptStats[];

  topPerformers?: Performer[];
  needsAttention?: Attention[];
};

function toISODate(d: Date) {
  const yyyy = d.getFullYear();
  const mm = String(d.getMonth() + 1).padStart(2, '0');
  const dd = String(d.getDate()).padStart(2, '0');
  return `${yyyy}-${mm}-${dd}`;
}

function clampNum(v: any) {
  const n = Number(v);
  return Number.isFinite(n) ? n : 0;
}

export default function ReportsPage() {
  // default: текущий месяц
  const now = new Date();
  const startDefault = new Date(now.getFullYear(), now.getMonth(), 1);
  const endDefault = new Date(now.getFullYear(), now.getMonth() + 1, 0);

  const [dateFrom, setDateFrom] = useState(toISODate(startDefault));
  const [dateTo, setDateTo] = useState(toISODate(endDefault));

  const [isLoading, setIsLoading] = useState(true);
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [error, setError] = useState('');

  const [data, setData] = useState<ReportsResponse>({
    summary: {},
    monthlyTrends: [],
    departmentAttendance: [],
    topPerformers: [],
    needsAttention: [],
  });

  const load = async (opts?: { silent?: boolean }) => {
    const silent = opts?.silent ?? false;
    setError('');
    if (silent) setIsRefreshing(true);
    else setIsLoading(true);

    try {
      // ✅ TODO #1: поменяй endpoint под твой backend
      // Например: /api/internal/reports/summary или /api/reports
      const res: any = await attendanceClient.get('/api/internal/reports', {
        params: { dateFrom, dateTo },
      });

      // Поддержим разные форматы
      const normalized: ReportsResponse = {
        summary: res?.summary || {},
        monthlyTrends: res?.monthlyTrends || res?.monthlyData || [],
        departmentAttendance: res?.departmentAttendance || res?.departmentData || [],
        topPerformers: res?.topPerformers || [],
        needsAttention: res?.needsAttention || [],
      };

      setData(normalized);
    } catch (e: any) {
      const msg =
          e?.data?.message ||
          e?.data?.error ||
          e?.message ||
          'Failed to load reports';
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

  const summary = useMemo(() => {
    const s = data.summary || {};
    return {
      avgDailyHours: s.avgDailyHours ?? null,
      totalOvertime: s.totalOvertime ?? null,
      attendanceRate: s.attendanceRate ?? null,
      punctuality: s.punctuality ?? null,
      deltaAvgDailyHours: s.deltaAvgDailyHours ?? null,
      deltaAttendanceRate: s.deltaAttendanceRate ?? null,
    };
  }, [data.summary]);

  const onApply = () => load();
  const onRefresh = () => load({ silent: true });

  const exportReport = async () => {
    setError('');
    try {
      // ✅ TODO #2: endpoint для экспорта (PDF/CSV)
      // Вариант A: backend отдаёт blob
      const blobRes = await fetch(
          `${process.env.NEXT_PUBLIC_ATTENDANCE_API_URL || 'http://localhost:8082'}/api/internal/reports/export?dateFrom=${encodeURIComponent(
              dateFrom
          )}&dateTo=${encodeURIComponent(dateTo)}`,
          {
            method: 'GET',
            headers: {
              // если нужно — Authorization и X-Device возьмутся из attendanceClient,
              // но тут fetch напрямую. Можно сделать через attendanceClient.request как blob, если добавишь поддержку blob.
            },
          }
      );

      if (!blobRes.ok) throw new Error('Export failed');

      const blob = await blobRes.blob();
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `report_${dateFrom}_${dateTo}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    } catch (e: any) {
      setError(e?.message || 'Failed to export report');
    }
  };

  return (
      <div className="p-6 space-y-6 max-w-7xl">
        <div className="flex items-start justify-between gap-4">
          <div className="space-y-2">
            <h1 className="text-3xl font-bold">Reports</h1>
            <p className="text-muted-foreground">
              View detailed attendance and productivity reports
            </p>
          </div>

          <div className="flex gap-2">
            <Button
                variant="outline"
                className="gap-2"
                onClick={onRefresh}
                disabled={isLoading || isRefreshing}
            >
              {isRefreshing ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
              ) : (
                  <RefreshCw className="w-4 h-4" />
              )}
              Refresh
            </Button>

            <Button className="gap-2" onClick={exportReport} disabled={isLoading}>
              <Download className="w-4 h-4" />
              Export Report
            </Button>
          </div>
        </div>

        {/* Filters */}
        <Card className="p-4 bg-white border border-border">
          <div className="flex gap-4 items-end flex-wrap">
            <div className="min-w-[280px]">
              <label className="text-sm font-medium">Date Range</label>
              <div className="flex gap-2 mt-2 items-center">
                <Input type="date" value={dateFrom} onChange={(e) => setDateFrom(e.target.value)} />
                <span className="text-muted-foreground">to</span>
                <Input type="date" value={dateTo} onChange={(e) => setDateTo(e.target.value)} />
              </div>
            </div>
            <Button variant="outline" onClick={onApply} disabled={isLoading}>
              Apply Filters
            </Button>

            {isLoading && (
                <div className="flex items-center gap-2 text-sm text-muted-foreground">
                  <Loader2 className="w-4 h-4 animate-spin" /> Loading...
                </div>
            )}
          </div>
        </Card>

        {/* Error */}
        {error && (
            <Card className="p-4 border border-destructive/30 bg-destructive/5">
              <p className="text-sm text-destructive font-medium">Error</p>
              <p className="text-sm text-muted-foreground mt-1">{error}</p>
            </Card>
        )}

        {/* Summary Cards */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Avg Daily Hours</p>
            <p className="text-3xl font-bold mt-2">
              {summary.avgDailyHours === null ? '—' : `${summary.avgDailyHours.toFixed(1)}h`}
            </p>
            <p className="text-xs mt-2 text-muted-foreground">
              {summary.deltaAvgDailyHours === null ? '—' : `${summary.deltaAvgDailyHours >= 0 ? '+' : ''}${summary.deltaAvgDailyHours.toFixed(1)}h vs target`}
            </p>
          </Card>

          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Total Overtime</p>
            <p className="text-3xl font-bold mt-2">
              {summary.totalOvertime === null ? '—' : `${Math.round(summary.totalOvertime)}h`}
            </p>
            <p className="text-xs text-muted-foreground mt-2">This period</p>
          </Card>

          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Attendance Rate</p>
            <p className="text-3xl font-bold mt-2">
              {summary.attendanceRate === null ? '—' : `${summary.attendanceRate.toFixed(1)}%`}
            </p>
            <p className="text-xs mt-2 text-muted-foreground">
              {summary.deltaAttendanceRate === null ? '—' : `${summary.deltaAttendanceRate >= 0 ? '+' : ''}${summary.deltaAttendanceRate.toFixed(1)}% vs last period`}
            </p>
          </Card>

          <Card className="p-4 bg-white border border-border">
            <p className="text-muted-foreground text-sm">Avg Punctuality</p>
            <p className="text-3xl font-bold mt-2">
              {summary.punctuality === null ? '—' : `${summary.punctuality.toFixed(1)}%`}
            </p>
            <p className="text-xs text-muted-foreground mt-2">On time arrivals</p>
          </Card>
        </div>

        {/* Monthly Trends */}
        <Card className="p-6 bg-white border border-border">
          <div className="space-y-4">
            <h3 className="text-lg font-semibold">Monthly Trends</h3>

            {data.monthlyTrends.length === 0 ? (
                <p className="text-sm text-muted-foreground">No data for selected period.</p>
            ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <LineChart data={data.monthlyTrends}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="hours" strokeWidth={2} />
                    <Line type="monotone" dataKey="target" strokeWidth={2} strokeDasharray="5 5" />
                  </LineChart>
                </ResponsiveContainer>
            )}
          </div>
        </Card>

        {/* Department Comparison */}
        <Card className="p-6 bg-white border border-border">
          <div className="space-y-4">
            <h3 className="text-lg font-semibold">Department Attendance</h3>

            {data.departmentAttendance.length === 0 ? (
                <p className="text-sm text-muted-foreground">No department stats available.</p>
            ) : (
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={data.departmentAttendance}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="department" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Bar dataKey="present" radius={[8, 8, 0, 0]} />
                    <Bar dataKey="absent" radius={[8, 8, 0, 0]} />
                    <Bar dataKey="late" radius={[8, 8, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
            )}
          </div>
        </Card>

        {/* Top Performers + Needs Attention */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <Card className="p-6 bg-white border border-border">
            <h3 className="text-lg font-semibold mb-4">Top Performers</h3>

            {(data.topPerformers || []).length === 0 ? (
                <p className="text-sm text-muted-foreground">No data.</p>
            ) : (
                <div className="space-y-3">
                  {(data.topPerformers || []).map((p, idx) => (
                      <div
                          key={`${p.name}-${idx}`}
                          className="flex items-center justify-between p-3 border border-border rounded-lg"
                      >
                        <div>
                          <p className="font-medium">{p.name}</p>
                          <p className="text-sm text-muted-foreground">
                            {p.hours != null ? `${clampNum(p.hours)}h` : '—'}
                          </p>
                        </div>
                        <span className="px-3 py-1 bg-green-100 text-green-700 rounded text-xs font-medium">
                    +{p.overtime != null ? `${clampNum(p.overtime)}h` : '—'}
                  </span>
                      </div>
                  ))}
                </div>
            )}
          </Card>

          <Card className="p-6 bg-white border border-border">
            <h3 className="text-lg font-semibold mb-4">Needs Attention</h3>

            {(data.needsAttention || []).length === 0 ? (
                <p className="text-sm text-muted-foreground">No data.</p>
            ) : (
                <div className="space-y-3">
                  {(data.needsAttention || []).map((x, idx) => (
                      <div
                          key={`${x.name}-${idx}`}
                          className="flex items-center justify-between p-3 border border-border rounded-lg"
                      >
                        <div>
                          <p className="font-medium">{x.name}</p>
                          <p className="text-sm text-muted-foreground">{x.status}</p>
                        </div>
                        <span className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded text-xs font-medium">
                    {x.count}
                  </span>
                      </div>
                  ))}
                </div>
            )}
          </Card>
        </div>
      </div>
  );
}
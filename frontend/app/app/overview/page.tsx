'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import {
  LineChart,
  Line,
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
import { Clock, AlertCircle, CheckCircle2, Users } from 'lucide-react';
import { useState, useEffect } from 'react';

// Sample data - Replace with real API calls
const chartData = [
  { day: 'Mon', hours: 8, target: 8 },
  { day: 'Tue', hours: 8, target: 8 },
  { day: 'Wed', hours: 7.5, target: 8 },
  { day: 'Thu', hours: 8, target: 8 },
  { day: 'Fri', hours: 8, target: 8 },
  { day: 'Sat', hours: 0, target: 0 },
  { day: 'Sun', hours: 0, target: 0 },
];

const attendanceData = [
  { name: 'Present', value: 85, fill: '#2563eb' },
  { name: 'Late', value: 10, fill: '#3b82f6' },
  { name: 'Absent', value: 5, fill: '#f3f4f6' },
];

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
    {action && (
      <div className="mt-4 pt-4 border-t border-border">
        {action}
      </div>
    )}
  </Card>
);

export default function OverviewPage() {
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    // Fetch dashboard data from API
    // const fetchData = async () => {
    //   try {
    //     const data = await attendanceClient.get('/api/v1/events/attendance?from=2024-01-01&to=2024-01-31');
    //   } catch (error) {
    //     console.error('Failed to fetch data:', error);
    //   }
    // };
    // fetchData();
  }, []);

  return (
    <div className="p-6 space-y-6 max-w-7xl">
      {/* Header */}
      <div className="space-y-2">
        <h1 className="text-3xl font-bold">Welcome Back</h1>
        <p className="text-muted-foreground">Here's your attendance overview for this week</p>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KpiCard
          icon={CheckCircle2}
          title="Today"
          value="Present"
          subtitle="Checked in at 9:00 AM"
        />
        <KpiCard
          icon={Clock}
          title="Hours Today"
          value="4.5h"
          subtitle="Still working"
        />
        <KpiCard
          icon={Clock}
          title="This Week"
          value="39.5h"
          subtitle="2 hours overtime"
        />
        <KpiCard
          icon={AlertCircle}
          title="Status"
          value="On Time"
          subtitle="No delays recorded"
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
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="day" />
                <YAxis />
                <Tooltip
                  contentStyle={{
                    backgroundColor: '#ffffff',
                    border: '1px solid #e5e7eb',
                    borderRadius: '8px',
                  }}
                />
                <Bar dataKey="hours" fill="#2563eb" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </Card>

        {/* Attendance Pie Chart */}
        <Card className="p-6 bg-white border border-border">
          <div className="space-y-4">
            <div>
              <h3 className="text-lg font-semibold">Attendance</h3>
              <p className="text-sm text-muted-foreground">Team overview</p>
            </div>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={attendanceData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  dataKey="value"
                >
                  {attendanceData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.fill} />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
            <div className="space-y-2 text-sm">
              {attendanceData.map((item) => (
                <div key={item.name} className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div
                      className="w-3 h-3 rounded-full"
                      style={{ backgroundColor: item.fill }}
                    />
                    <span className="text-muted-foreground">{item.name}</span>
                  </div>
                  <span className="font-medium">{item.value}%</span>
                </div>
              ))}
            </div>
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
          <div className="space-y-3">
            {[
              { time: '09:00 AM', event: 'Checked in', status: 'success' },
              { time: '01:00 PM', event: 'Lunch break', status: 'neutral' },
              { time: '06:00 PM', event: 'Checked out', status: 'success' },
              { time: 'Yesterday', event: 'Full day recorded', status: 'success' },
            ].map((item, idx) => (
              <div key={idx} className="flex items-center justify-between py-3 border-b border-border last:border-0">
                <div className="flex items-center gap-3">
                  <div className={`w-2 h-2 rounded-full ${
                    item.status === 'success' ? 'bg-green-500' : 'bg-blue-500'
                  }`} />
                  <span className="text-foreground">{item.event}</span>
                </div>
                <span className="text-sm text-muted-foreground">{item.time}</span>
              </div>
            ))}
          </div>
        </div>
      </Card>
    </div>
  );
}

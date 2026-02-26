'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
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
import { Download, Calendar } from 'lucide-react';

const monthlyData = [
  { month: 'Jan', hours: 160, target: 160, overtime: 5 },
  { month: 'Feb', hours: 158, target: 160, overtime: 2 },
  { month: 'Mar', hours: 165, target: 160, overtime: 8 },
  { month: 'Apr', hours: 160, target: 160, overtime: 4 },
  { month: 'May', hours: 162, target: 160, overtime: 6 },
  { month: 'Jun', hours: 160, target: 160, overtime: 3 },
];

const departmentData = [
  { department: 'Engineering', present: 24, absent: 2, late: 1 },
  { department: 'Sales', present: 18, absent: 1, late: 2 },
  { department: 'Design', present: 12, absent: 1, late: 0 },
  { department: 'HR', present: 5, absent: 0, late: 0 },
];

export default function ReportsPage() {
  return (
    <div className="p-6 space-y-6 max-w-7xl">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold">Reports</h1>
          <p className="text-muted-foreground">View detailed attendance and productivity reports</p>
        </div>
        <Button className="gap-2">
          <Download className="w-4 h-4" />
          Export Report
        </Button>
      </div>

      {/* Filters */}
      <Card className="p-4 bg-white border border-border">
        <div className="flex gap-4 items-end flex-wrap">
          <div>
            <label className="text-sm font-medium">Date Range</label>
            <div className="flex gap-2 mt-2">
              <input type="date" className="px-3 py-2 border border-border rounded-lg" />
              <span className="text-muted-foreground">to</span>
              <input type="date" className="px-3 py-2 border border-border rounded-lg" />
            </div>
          </div>
          <Button variant="outline">Apply Filters</Button>
        </div>
      </Card>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Avg Daily Hours</p>
          <p className="text-3xl font-bold mt-2">8.1h</p>
          <p className="text-xs text-green-600 mt-2">+0.1h vs target</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Total Overtime</p>
          <p className="text-3xl font-bold mt-2">28h</p>
          <p className="text-xs text-muted-foreground mt-2">This period</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Attendance Rate</p>
          <p className="text-3xl font-bold mt-2">98.2%</p>
          <p className="text-xs text-green-600 mt-2">+2.1% vs last period</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Avg Punctuality</p>
          <p className="text-3xl font-bold mt-2">95.5%</p>
          <p className="text-xs text-muted-foreground mt-2">On time arrivals</p>
        </Card>
      </div>

      {/* Monthly Trends */}
      <Card className="p-6 bg-white border border-border">
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Monthly Trends</h3>
          <ResponsiveContainer width="100%" height={300}>
            <LineChart data={monthlyData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="month" />
              <YAxis />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#ffffff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                }}
              />
              <Legend />
              <Line type="monotone" dataKey="hours" stroke="#2563eb" strokeWidth={2} />
              <Line type="monotone" dataKey="target" stroke="#9ca3af" strokeWidth={2} strokeDasharray="5 5" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </Card>

      {/* Department Comparison */}
      <Card className="p-6 bg-white border border-border">
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Department Attendance</h3>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={departmentData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="department" />
              <YAxis />
              <Tooltip
                contentStyle={{
                  backgroundColor: '#ffffff',
                  border: '1px solid #e5e7eb',
                  borderRadius: '8px',
                }}
              />
              <Legend />
              <Bar dataKey="present" fill="#2563eb" radius={[8, 8, 0, 0]} />
              <Bar dataKey="absent" fill="#ef4444" radius={[8, 8, 0, 0]} />
              <Bar dataKey="late" fill="#f59e0b" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      </Card>

      {/* Top Performers */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <Card className="p-6 bg-white border border-border">
          <h3 className="text-lg font-semibold mb-4">Top Performers</h3>
          <div className="space-y-3">
            {[
              { name: 'Alice Johnson', hours: '170h', overtime: '10h' },
              { name: 'David Brown', hours: '168h', overtime: '8h' },
              { name: 'Bob Smith', hours: '165h', overtime: '5h' },
            ].map((performer, idx) => (
              <div key={idx} className="flex items-center justify-between p-3 border border-border rounded-lg">
                <div>
                  <p className="font-medium">{performer.name}</p>
                  <p className="text-sm text-muted-foreground">{performer.hours}</p>
                </div>
                <span className="px-3 py-1 bg-green-100 text-green-700 rounded text-xs font-medium">
                  +{performer.overtime}
                </span>
              </div>
            ))}
          </div>
        </Card>

        <Card className="p-6 bg-white border border-border">
          <h3 className="text-lg font-semibold mb-4">Needs Attention</h3>
          <div className="space-y-3">
            {[
              { name: 'Carol White', status: 'Multiple lates', count: '5' },
              { name: 'Eve Wilson', status: 'Low attendance', count: '85%' },
              { name: 'Frank Miller', status: 'Exceeding hours', count: '180h' },
            ].map((item, idx) => (
              <div key={idx} className="flex items-center justify-between p-3 border border-border rounded-lg">
                <div>
                  <p className="font-medium">{item.name}</p>
                  <p className="text-sm text-muted-foreground">{item.status}</p>
                </div>
                <span className="px-3 py-1 bg-yellow-100 text-yellow-700 rounded text-xs font-medium">
                  {item.count}
                </span>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}

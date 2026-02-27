'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Clock, LogIn, LogOut, Filter } from 'lucide-react';
import { useState } from 'react';

const attendanceRecords = [
  { date: '2024-01-04', checkIn: '09:00 AM', checkOut: '06:00 PM', hours: '8h 45m', status: 'Present' },
  { date: '2024-01-03', checkIn: '08:45 AM', checkOut: '05:45 PM', hours: '8h 30m', status: 'Present' },
  { date: '2024-01-02', checkIn: '09:15 AM', checkOut: '06:15 PM', hours: '8h 15m', status: 'Late' },
  { date: '2024-01-01', checkIn: '—', checkOut: '—', hours: '—', status: 'Holiday' },
];

export default function AttendancePage() {
  const [selectedDate, setSelectedDate] = useState('');

  return (
    <div className="p-6 space-y-6 max-w-7xl">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold">Attendance</h1>
        <p className="text-muted-foreground">Track your daily attendance and work hours</p>
      </div>

      {/* Quick Actions */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <Button size="lg" className="gap-2 bg-green-600 hover:bg-green-700">
          <LogIn className="w-5 h-5" />
          Clock In
        </Button>
        <Button size="lg" variant="outline" className="gap-2">
          <LogOut className="w-5 h-5" />
          Clock Out
        </Button>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Today Status</p>
          <p className="text-2xl font-bold mt-2">Present</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Hours This Week</p>
          <p className="text-2xl font-bold mt-2">39h 30m</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Overtime</p>
          <p className="text-2xl font-bold mt-2">2h 15m</p>
        </Card>
      </div>

      {/* Filters */}
      <Card className="p-4 bg-white border border-border">
        <div className="flex gap-4 items-end">
          <div className="flex-1">
            <label className="text-sm font-medium">Filter by Date</label>
            <Input
              type="date"
              value={selectedDate}
              onChange={(e) => setSelectedDate(e.target.value)}
              className="mt-2"
            />
          </div>
          <Button variant="outline" className="gap-2">
            <Filter className="w-4 h-4" />
            Apply Filters
          </Button>
        </div>
      </Card>

      {/* Attendance Records Table */}
      <Card className="p-6 bg-white border border-border">
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Attendance Records</h3>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border">
                  <th className="text-left py-3 font-medium text-muted-foreground">Date</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Check In</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Check Out</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Hours</th>
                  <th className="text-left py-3 font-medium text-muted-foreground">Status</th>
                </tr>
              </thead>
              <tbody>
                {attendanceRecords.map((record, idx) => (
                  <tr key={idx} className="border-b border-border hover:bg-secondary transition-colors">
                    <td className="py-4 font-medium">{record.date}</td>
                    <td className="py-4 text-muted-foreground">{record.checkIn}</td>
                    <td className="py-4 text-muted-foreground">{record.checkOut}</td>
                    <td className="py-4 text-foreground">{record.hours}</td>
                    <td className="py-4">
                      <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                        record.status === 'Present' ? 'bg-green-100 text-green-700' :
                        record.status === 'Late' ? 'bg-yellow-100 text-yellow-700' :
                        'bg-gray-100 text-gray-700'
                      }`}>
                        {record.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </Card>
    </div>
  );
}

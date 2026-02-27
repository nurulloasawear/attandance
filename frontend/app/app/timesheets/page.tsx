'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { FileDown, Calendar } from 'lucide-react';

const timesheets = [
  {
    id: 1,
    period: 'Jan 1 - Jan 7, 2024',
    status: 'Approved',
    hours: 40,
    date: '2024-01-08',
  },
  {
    id: 2,
    period: 'Dec 25 - Dec 31, 2023',
    status: 'Submitted',
    hours: 32,
    date: '2024-01-01',
  },
  {
    id: 3,
    period: 'Dec 18 - Dec 24, 2023',
    status: 'Approved',
    hours: 40,
    date: '2023-12-25',
  },
];

export default function TimesheetsPage() {
  return (
    <div className="p-6 space-y-6 max-w-7xl">
      <div className="space-y-2">
        <h1 className="text-3xl font-bold">Timesheets</h1>
        <p className="text-muted-foreground">View and manage your weekly timesheets</p>
      </div>

      {/* Export Section */}
      <Card className="p-6 bg-gradient-to-r from-primary to-blue-600 border-0 text-white">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-semibold">Export Your Timesheets</h3>
            <p className="text-white/80 text-sm">Download your timesheets as CSV or Excel</p>
          </div>
          <div className="flex gap-2">
            <Button variant="secondary" className="gap-2">
              <FileDown className="w-4 h-4" />
              CSV
            </Button>
            <Button variant="secondary" className="gap-2">
              <FileDown className="w-4 h-4" />
              Excel
            </Button>
          </div>
        </div>
      </Card>

      {/* Timesheets List */}
      <Card className="p-6 bg-white border border-border">
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Recent Timesheets</h3>
          <div className="space-y-3">
            {timesheets.map((sheet) => (
              <div
                key={sheet.id}
                className="flex items-center justify-between p-4 border border-border rounded-lg hover:bg-secondary transition-colors group"
              >
                <div className="flex items-center gap-4 flex-1">
                  <div className="w-12 h-12 rounded-lg bg-secondary flex items-center justify-center text-primary group-hover:bg-primary group-hover:text-white transition-colors">
                    <Calendar className="w-6 h-6" />
                  </div>
                  <div className="flex-1">
                    <h4 className="font-semibold text-foreground">{sheet.period}</h4>
                    <p className="text-sm text-muted-foreground">{sheet.hours} hours • {sheet.date}</p>
                  </div>
                </div>
                <div className="flex items-center gap-4">
                  <span className={`px-3 py-1 rounded-full text-xs font-medium ${
                    sheet.status === 'Approved' ? 'bg-green-100 text-green-700' :
                    'bg-blue-100 text-blue-700'
                  }`}>
                    {sheet.status}
                  </span>
                  <Button variant="outline" size="sm">View</Button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </Card>

      {/* Summary */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Total Hours</p>
          <p className="text-3xl font-bold mt-2">112h</p>
          <p className="text-xs text-muted-foreground mt-2">This month</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Pending Approval</p>
          <p className="text-3xl font-bold mt-2">1</p>
          <p className="text-xs text-muted-foreground mt-2">1 timesheet</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Approved</p>
          <p className="text-3xl font-bold mt-2">2</p>
          <p className="text-xs text-muted-foreground mt-2">This month</p>
        </Card>
      </div>
    </div>
  );
}

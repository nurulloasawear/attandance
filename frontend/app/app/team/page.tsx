'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Users, Plus, MoreVertical, Search } from 'lucide-react';
import { useState } from 'react';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu';

const teamMembers = [
  {
    id: 1,
    name: 'Alice Johnson',
    email: 'alice@example.com',
    role: 'Manager',
    department: 'Engineering',
    status: 'Active',
    lastSeen: '2024-01-04 10:30 AM',
  },
  {
    id: 2,
    name: 'Bob Smith',
    email: 'bob@example.com',
    role: 'Developer',
    department: 'Engineering',
    status: 'Active',
    lastSeen: '2024-01-04 09:15 AM',
  },
  {
    id: 3,
    name: 'Carol White',
    email: 'carol@example.com',
    role: 'Designer',
    department: 'Design',
    status: 'Away',
    lastSeen: '2024-01-03 05:00 PM',
  },
  {
    id: 4,
    name: 'David Brown',
    email: 'david@example.com',
    role: 'Admin',
    department: 'Management',
    status: 'Active',
    lastSeen: '2024-01-04 11:00 AM',
  },
];

export default function TeamPage() {
  const [searchQuery, setSearchQuery] = useState('');

  const filteredMembers = teamMembers.filter(member =>
    member.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
    member.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="p-6 space-y-6 max-w-7xl">
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold">Team Management</h1>
          <p className="text-muted-foreground">Manage team members and their roles</p>
        </div>
        <Button className="gap-2">
          <Plus className="w-4 h-4" />
          Add Member
        </Button>
      </div>

      {/* Search */}
      <Card className="p-4 bg-white border border-border">
        <div className="relative">
          <Search className="absolute left-3 top-3 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search members..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-10"
          />
        </div>
      </Card>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Total Members</p>
          <p className="text-3xl font-bold mt-2">{teamMembers.length}</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Active Now</p>
          <p className="text-3xl font-bold mt-2">3</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">Departments</p>
          <p className="text-3xl font-bold mt-2">3</p>
        </Card>
        <Card className="p-4 bg-white border border-border">
          <p className="text-muted-foreground text-sm">New This Month</p>
          <p className="text-3xl font-bold mt-2">1</p>
        </Card>
      </div>

      {/* Team List */}
      <Card className="p-6 bg-white border border-border">
        <div className="space-y-4">
          <h3 className="text-lg font-semibold">Team Members</h3>
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
                {filteredMembers.map((member) => (
                  <tr key={member.id} className="border-b border-border hover:bg-secondary transition-colors">
                    <td className="py-4 font-medium">{member.name}</td>
                    <td className="py-4 text-muted-foreground">{member.email}</td>
                    <td className="py-4">
                      <span className="px-2 py-1 bg-blue-100 text-blue-700 rounded text-xs font-medium">
                        {member.role}
                      </span>
                    </td>
                    <td className="py-4 text-muted-foreground">{member.department}</td>
                    <td className="py-4">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                        member.status === 'Active' ? 'bg-green-100 text-green-700' :
                        'bg-gray-100 text-gray-700'
                      }`}>
                        {member.status}
                      </span>
                    </td>
                    <td className="py-4 text-muted-foreground">{member.lastSeen}</td>
                    <td className="py-4 text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem>View Details</DropdownMenuItem>
                          <DropdownMenuItem>Edit</DropdownMenuItem>
                          <DropdownMenuItem>Change Role</DropdownMenuItem>
                          <DropdownMenuItem className="text-destructive">
                            Remove
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
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

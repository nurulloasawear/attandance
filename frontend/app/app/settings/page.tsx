'use client';

import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Lock, User, Bell, Shield, AlertCircle, Loader2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import {attendanceClient, authClient, userClient} from '@/lib/api-client';
import { useRouter } from 'next/navigation';

type UserProfile = {
  username?: string;
  email?: string;
  firstName?: string;
  lastName?: string;
};

export default function SettingsPage() {
  const router = useRouter();

  const [tab, setTab] = useState('profile');
  const [isLoading, setIsLoading] = useState(true);
  const [isSavingProfile, setIsSavingProfile] = useState(false);
  const [isSavingPassword, setIsSavingPassword] = useState(false);
  const [error, setError] = useState('');

  const [profile, setProfile] = useState<UserProfile>({
    firstName: '',
    lastName: '',
    email: '',
    username: '',
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  useEffect(() => {
    const load = async () => {
      setError('');
      try {
        const data: any = await userClient.get('/api/internal/users/me/profile');
        setProfile({
          username: data?.username ?? '',
          email: data?.email ?? '',
          firstName: data?.firstName ?? '',
          lastName: data?.lastName ?? '',
        });
      } catch (e: any) {
        setError(e?.data?.message || e?.data?.error || e?.message || 'Failed to load profile');
        router.push('/auth/login');
      } finally {
        setIsLoading(false);
      }
    };

    load();
  }, [router]);

  const onProfileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setProfile(prev => ({ ...prev, [name]: value }));
  };

  const onPasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setPasswordForm(prev => ({ ...prev, [name]: value }));
  };

  const saveProfile = async () => {
    setError('');
    setIsSavingProfile(true);
    try {
      const payload = {
        username: profile.username?.trim(),
        email: profile.email?.trim(),
        firstName: profile.firstName?.trim(),
        lastName: profile.lastName?.trim(),
      };

      const updated: any = await userClient.patch('/api/internal/users/me/profile', payload);

      setProfile({
        username: updated?.username ?? payload.username ?? '',
        email: updated?.email ?? payload.email ?? '',
        firstName: updated?.firstName ?? payload.firstName ?? '',
        lastName: updated?.lastName ?? payload.lastName ?? '',
      });
    } catch (e: any) {
      setError(e?.data?.message || e?.data?.error || e?.message || 'Failed to save profile');
    } finally {
      setIsSavingProfile(false);
    }
  };

  const updatePassword = async () => {
    setError('');

    if (!passwordForm.currentPassword || !passwordForm.newPassword) {
      setError('Fill current and new password');
      return;
    }
    if (passwordForm.newPassword.length < 8) {
      setError('New password must be at least 8 characters');
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setIsSavingPassword(true);
    try {
      const payload = {
        username: profile.username?.trim(),
        email: profile.email?.trim(),
        firstName: profile.firstName?.trim(),
        lastName: profile.lastName?.trim(),
      };

      await userClient.patch('/api/internal/users/me/profile', payload, {
        params: { password: passwordForm.newPassword },
      });

      setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (e: any) {
      setError(e?.data?.message || e?.data?.error || e?.message || 'Failed to update password');
    } finally {
      setIsSavingPassword(false);
    }
  };

  const logoutAll = async () => {
    setError('');
    try {
      await authClient.post('/api/auth/logout');
    } catch {}
    authClient.clearTokens();
    userClient.clearTokens();
    attendanceClient.clearTokens();
    router.replace('/auth/login');
  };

  const deleteAccount = async () => {
    setError('');
    try {
      await userClient.delete('/api/internal/users/me');
      authClient.clearTokens();
      userClient.clearTokens();
      router.replace('/auth/register');
    } catch (e: any) {
      setError(e?.data?.message || e?.data?.error || e?.message || 'Failed to delete account');
    }
  };

  if (isLoading) {
    return (
        <div className="p-6">
          <Card className="p-6 bg-white border border-border">
            <div className="flex items-center gap-3">
              <Loader2 className="h-4 w-4 animate-spin" />
              <span className="text-sm text-muted-foreground">Loading settings...</span>
            </div>
          </Card>
        </div>
    );
  }

  return (
      <div className="p-6 space-y-6 max-w-4xl">
        <div className="space-y-2">
          <h1 className="text-3xl font-bold">Settings</h1>
          <p className="text-muted-foreground">Manage your account and preferences</p>
        </div>

        {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>{error}</AlertDescription>
            </Alert>
        )}

        <Tabs value={tab} onValueChange={setTab} className="space-y-4">
          <TabsList className="grid w-full grid-cols-4">
            <TabsTrigger value="profile" className="gap-2">
              <User className="w-4 h-4" />
              <span className="hidden sm:inline">Profile</span>
            </TabsTrigger>
            <TabsTrigger value="password" className="gap-2">
              <Lock className="w-4 h-4" />
              <span className="hidden sm:inline">Password</span>
            </TabsTrigger>
            <TabsTrigger value="notifications" className="gap-2">
              <Bell className="w-4 h-4" />
              <span className="hidden sm:inline">Notifications</span>
            </TabsTrigger>
            <TabsTrigger value="security" className="gap-2">
              <Shield className="w-4 h-4" />
              <span className="hidden sm:inline">Security</span>
            </TabsTrigger>
          </TabsList>

          <TabsContent value="profile" className="space-y-4">
            <Card className="p-6 bg-white border border-border">
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold mb-4">Personal Information</h3>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <Label htmlFor="firstName">First Name</Label>
                    <Input id="firstName" name="firstName" value={profile.firstName || ''} onChange={onProfileChange} />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="lastName">Last Name</Label>
                    <Input id="lastName" name="lastName" value={profile.lastName || ''} onChange={onProfileChange} />
                  </div>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="email">Email Address</Label>
                  <Input id="email" name="email" type="email" value={profile.email || ''} onChange={onProfileChange} />
                </div>

                <Button className="w-full" onClick={saveProfile} disabled={isSavingProfile}>
                  {isSavingProfile ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Saving...
                      </>
                  ) : (
                      'Save Changes'
                  )}
                </Button>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="password" className="space-y-4">
            <Card className="p-6 bg-white border border-border">
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold mb-4">Change Password</h3>
                  <p className="text-sm text-muted-foreground">Update your password</p>
                </div>

                <div className="space-y-2">
                  <Label htmlFor="currentPassword">Current Password</Label>
                  <Input
                      id="currentPassword"
                      name="currentPassword"
                      type="password"
                      value={passwordForm.currentPassword}
                      onChange={onPasswordChange}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="newPassword">New Password</Label>
                  <Input
                      id="newPassword"
                      name="newPassword"
                      type="password"
                      value={passwordForm.newPassword}
                      onChange={onPasswordChange}
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="confirmPassword">Confirm Password</Label>
                  <Input
                      id="confirmPassword"
                      name="confirmPassword"
                      type="password"
                      value={passwordForm.confirmPassword}
                      onChange={onPasswordChange}
                  />
                </div>

                <Button className="w-full" onClick={updatePassword} disabled={isSavingPassword}>
                  {isSavingPassword ? (
                      <>
                        <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                        Updating...
                      </>
                  ) : (
                      'Update Password'
                  )}
                </Button>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="notifications" className="space-y-4">
            <Card className="p-6 bg-white border border-border">
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold mb-4">Notification Preferences</h3>
                </div>

                {[
                  { id: 'email_attendance', label: 'Attendance Reminders', description: 'Get reminded to clock in/out' },
                  { id: 'email_reports', label: 'Weekly Reports', description: 'Receive weekly attendance reports' },
                  { id: 'email_updates', label: 'System Updates', description: 'Get notified about system updates' },
                  { id: 'email_team', label: 'Team Changes', description: 'Be notified when team members join/leave' },
                ].map(n => (
                    <div key={n.id} className="flex items-center justify-between p-4 border border-border rounded-lg">
                      <div>
                        <p className="font-medium text-foreground">{n.label}</p>
                        <p className="text-sm text-muted-foreground">{n.description}</p>
                      </div>
                      <input type="checkbox" defaultChecked className="w-5 h-5 rounded" />
                    </div>
                ))}

                <Button className="w-full" disabled>
                  Save Preferences
                </Button>
              </div>
            </Card>
          </TabsContent>

          <TabsContent value="security" className="space-y-4">
            <Card className="p-6 bg-white border border-border">
              <div className="space-y-6">
                <div>
                  <h3 className="text-lg font-semibold mb-4">Security Settings</h3>
                </div>

                <div className="p-4 border border-border rounded-lg bg-secondary">
                  <h4 className="font-medium text-foreground mb-2">Active Sessions</h4>
                  <p className="text-sm text-muted-foreground mb-4">Manage sessions</p>
                  <Button variant="outline" className="w-full" onClick={logoutAll}>
                    Logout
                  </Button>
                </div>

                <div className="p-4 border border-destructive rounded-lg bg-red-50">
                  <h4 className="font-medium text-destructive mb-2">Danger Zone</h4>
                  <p className="text-sm text-muted-foreground mb-4">Permanently delete your account and all data</p>
                  <Button variant="destructive" className="w-full" onClick={deleteAccount}>
                    Delete Account
                  </Button>
                </div>
              </div>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
  );
}
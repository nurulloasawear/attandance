'use client';

import Link from 'next/link';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import {
  Clock,
  Users,
  BarChart3,
  Lock,
  Zap,
  Shield,
  CheckCircle2,
  ArrowRight,
  Sparkles,
} from 'lucide-react';

const features = [
  {
    title: 'Smart Clock-In/Out',
    description: 'One-tap attendance tracking with automatic timestamps and audit history.',
    icon: Clock,
  },
  {
    title: 'Team Management',
    description: 'Manage employees, departments, roles and permissions in one place.',
    icon: Users,
  },
  {
    title: 'Real-Time Analytics',
    description: 'Dashboards for patterns, late arrivals, overtime, and productivity insights.',
    icon: BarChart3,
  },
  {
    title: 'Secure Authentication',
    description: 'JWT + refresh sessions, device tracking, and admin security controls.',
    icon: Lock,
  },
  {
    title: 'Fast Performance',
    description: 'Optimized microservices architecture with clean API and fast responses.',
    icon: Zap,
  },
  {
    title: 'Data Protection',
    description: 'Enterprise-grade practices with privacy-first defaults and audit logs.',
    icon: Shield,
  },
];

const perks = [
  'Role-based access control',
  'Device sessions & refresh tokens',
  'Attendance exports (CSV/PDF ready)',
  'Audit logs for key actions',
];

export default function Home() {
  return (
      <div className="min-h-screen bg-background text-foreground">
        {/* Soft background */}
        <div className="pointer-events-none fixed inset-0 -z-10">
          <div className="absolute left-1/2 top-[-140px] h-[420px] w-[820px] -translate-x-1/2 rounded-full bg-primary/15 blur-3xl" />
          <div className="absolute right-[-120px] top-[240px] h-[360px] w-[520px] rounded-full bg-blue-500/10 blur-3xl" />
          <div className="absolute left-[-120px] top-[520px] h-[360px] w-[520px] rounded-full bg-indigo-500/10 blur-3xl" />
        </div>

        {/* Navigation */}
        <nav className="sticky top-0 z-50 border-b border-border/60 bg-background/80 backdrop-blur">
          <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
            <Link href="/" className="flex items-center gap-2">
            <span className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-primary/10 text-primary">
              <Sparkles className="h-5 w-5" />
            </span>
              <span className="text-lg font-semibold tracking-tight">TimeChamp</span>
            </Link>

            <div className="flex items-center gap-2">
              <Link href="/auth/login">
                <Button variant="ghost" className="hidden sm:inline-flex">
                  Sign In
                </Button>
              </Link>
              <Link href="/auth/register">
                <Button className="gap-2">
                  Get Started <ArrowRight className="h-4 w-4" />
                </Button>
              </Link>
            </div>
          </div>
        </nav>

        {/* Hero */}
        <header className="mx-auto max-w-7xl px-4 pb-16 pt-14 sm:px-6 lg:px-8 lg:pb-24 lg:pt-20">
          <div className="grid items-center gap-10 lg:grid-cols-2">
            <div className="space-y-6">
              <div className="inline-flex items-center gap-2 rounded-full border border-border bg-card px-3 py-1 text-sm text-muted-foreground shadow-sm">
              <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-primary">
                <CheckCircle2 className="h-4 w-4" />
              </span>
                Attendance + Teams + Analytics — in one platform
              </div>

              <h1 className="text-balance text-4xl font-bold tracking-tight sm:text-5xl lg:text-6xl">
                Employee Attendance
                <span className="block text-primary">Made Simple</span>
              </h1>

              <p className="max-w-xl text-pretty text-base text-muted-foreground sm:text-lg">
                Track attendance, manage employees, and see real-time insights. Built for modern teams
                with secure sessions, device control, and clean APIs.
              </p>

              <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
                <Link href="/auth/register">
                  <Button size="lg" className="h-12 gap-2 px-6">
                    Start Free Trial <ArrowRight className="h-4 w-4" />
                  </Button>
                </Link>
                <Link href="#features">
                  <Button size="lg" variant="outline" className="h-12 px-6">
                    Explore Features
                  </Button>
                </Link>
              </div>

              <div className="grid gap-2 pt-2 sm:grid-cols-2">
                {perks.map((p) => (
                    <div key={p} className="flex items-center gap-2 text-sm text-muted-foreground">
                      <CheckCircle2 className="h-4 w-4 text-primary" />
                      <span>{p}</span>
                    </div>
                ))}
              </div>
            </div>

            {/* Hero card */}
            <div className="relative">
              <Card className="relative overflow-hidden rounded-3xl border-border bg-card p-6 shadow-sm">
                <div className="absolute inset-0 bg-gradient-to-br from-primary/10 via-transparent to-blue-500/10" />

                <div className="relative space-y-6">
                  <div className="flex items-center justify-between">
                    <div className="space-y-1">
                      <p className="text-sm text-muted-foreground">Today</p>
                      <p className="text-2xl font-semibold tracking-tight">Attendance Overview</p>
                    </div>
                    <span className="rounded-full bg-primary/10 px-3 py-1 text-sm font-medium text-primary">
                    Live
                  </span>
                  </div>

                  <div className="grid grid-cols-3 gap-3">
                    <Card className="rounded-2xl border-border bg-background p-4">
                      <p className="text-sm text-muted-foreground">Present</p>
                      <p className="mt-2 text-3xl font-bold">42</p>
                      <p className="mt-1 text-xs text-muted-foreground">+6 since 9:00</p>
                    </Card>
                    <Card className="rounded-2xl border-border bg-background p-4">
                      <p className="text-sm text-muted-foreground">Late</p>
                      <p className="mt-2 text-3xl font-bold">3</p>
                      <p className="mt-1 text-xs text-muted-foreground">-1 vs yesterday</p>
                    </Card>
                    <Card className="rounded-2xl border-border bg-background p-4">
                      <p className="text-sm text-muted-foreground">Remote</p>
                      <p className="mt-2 text-3xl font-bold">9</p>
                      <p className="mt-1 text-xs text-muted-foreground">stable</p>
                    </Card>
                  </div>

                  <Card className="rounded-2xl border-border bg-background p-4">
                    <div className="flex items-center justify-between">
                      <p className="text-sm font-medium">Security</p>
                      <span className="text-xs text-muted-foreground">JWT + sessions</span>
                    </div>
                    <div className="mt-3 flex items-center gap-3">
                      <div className="h-2 flex-1 overflow-hidden rounded-full bg-muted">
                        <div className="h-full w-[82%] rounded-full bg-primary" />
                      </div>
                      <span className="text-sm font-semibold">82%</span>
                    </div>
                    <p className="mt-2 text-xs text-muted-foreground">
                      Device tracking and refresh tokens enabled
                    </p>
                  </Card>
                </div>
              </Card>

              <div className="absolute -bottom-8 -left-6 -z-10 h-40 w-40 rounded-full bg-primary/20 blur-2xl" />
              <div className="absolute -top-10 -right-10 -z-10 h-44 w-44 rounded-full bg-blue-500/15 blur-2xl" />
            </div>
          </div>
        </header>

        {/* Stats */}
        <section className="border-y border-border bg-secondary/60">
          <div className="mx-auto grid max-w-7xl grid-cols-1 gap-6 px-4 py-10 sm:px-6 md:grid-cols-3 lg:px-8">
            {[
              { label: 'Teams onboarded', value: '1000+' },
              { label: 'Uptime', value: '99.9%' },
              { label: 'Support', value: '24/7' },
            ].map((s) => (
                <Card key={s.label} className="rounded-3xl border-border bg-card p-6 shadow-sm">
                  <p className="text-sm text-muted-foreground">{s.label}</p>
                  <p className="mt-2 text-4xl font-bold text-primary">{s.value}</p>
                  <p className="mt-1 text-sm text-muted-foreground">Reliable, secure, modern</p>
                </Card>
            ))}
          </div>
        </section>

        {/* Features */}
        <section id="features" className="mx-auto max-w-7xl px-4 py-16 sm:px-6 lg:px-8 lg:py-20">
          <div className="mx-auto max-w-2xl text-center">
            <h2 className="text-3xl font-bold tracking-tight sm:text-4xl">Powerful Features</h2>
            <p className="mt-3 text-base text-muted-foreground sm:text-lg">
              Everything you need to manage employee attendance — clean UI and clean APIs.
            </p>
          </div>

          <div className="mt-10 grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {features.map((feature, i) => {
              const Icon = feature.icon;
              return (
                  <Card
                      key={i}
                      className="group rounded-3xl border-border bg-card p-6 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
                  >
                    <div className="flex items-start gap-4">
                      <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-primary/10 text-primary transition-colors group-hover:bg-primary group-hover:text-primary-foreground">
                        <Icon className="h-6 w-6" />
                      </div>
                      <div className="space-y-2">
                        <h3 className="text-lg font-semibold">{feature.title}</h3>
                        <p className="text-sm leading-relaxed text-muted-foreground">
                          {feature.description}
                        </p>
                      </div>
                    </div>
                  </Card>
              );
            })}
          </div>
        </section>

        {/* CTA */}
        <section className="px-4 pb-16 sm:px-6 lg:px-8">
          <div className="mx-auto max-w-7xl">
            <Card className="relative overflow-hidden rounded-[2rem] border-border bg-primary p-10 text-primary-foreground shadow-sm">
              <div className="absolute inset-0 bg-gradient-to-br from-white/10 via-transparent to-black/10" />
              <div className="relative flex flex-col items-start justify-between gap-6 md:flex-row md:items-center">
                <div className="space-y-2">
                  <h3 className="text-3xl font-bold">Ready to Get Started?</h3>
                  <p className="text-primary-foreground/90">
                    Create your workspace and invite your team in minutes.
                  </p>
                </div>
                <div className="flex w-full flex-col gap-3 sm:w-auto sm:flex-row">
                  <Link className="w-full sm:w-auto" href="/auth/register">
                    <Button size="lg" variant="secondary" className="h-12 w-full px-6 sm:w-auto">
                      Start Your Free Trial
                    </Button>
                  </Link>
                  <Link className="w-full sm:w-auto" href="/auth/login">
                    <Button
                        size="lg"
                        variant="outline"
                        className="h-12 w-full border-white/30 bg-transparent px-6 text-white hover:bg-white/10 sm:w-auto"
                    >
                      Sign In
                    </Button>
                  </Link>
                </div>
              </div>
            </Card>
          </div>
        </section>

        {/* Footer */}
        <footer className="border-t border-border bg-background">
          <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
            <div className="grid gap-10 md:grid-cols-4">
              <div className="space-y-3">
                <div className="flex items-center gap-2">
                <span className="inline-flex h-9 w-9 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <Sparkles className="h-5 w-5" />
                </span>
                  <span className="text-lg font-semibold">TimeChamp</span>
                </div>
                <p className="text-sm text-muted-foreground">
                  Modern attendance management for modern teams.
                </p>
              </div>

              {[
                {
                  title: 'Product',
                  links: ['Features', 'Pricing', 'Security'],
                },
                {
                  title: 'Company',
                  links: ['About', 'Blog', 'Contact'],
                },
                {
                  title: 'Legal',
                  links: ['Privacy', 'Terms', 'Compliance'],
                },
              ].map((col) => (
                  <div key={col.title}>
                    <h4 className="font-semibold">{col.title}</h4>
                    <ul className="mt-4 space-y-2 text-sm text-muted-foreground">
                      {col.links.map((l) => (
                          <li key={l}>
                            <a href="#" className="transition-colors hover:text-foreground">
                              {l}
                            </a>
                          </li>
                      ))}
                    </ul>
                  </div>
              ))}
            </div>

            <div className="mt-10 flex flex-col gap-4 border-t border-border pt-8 sm:flex-row sm:items-center sm:justify-between">
              <p className="text-sm text-muted-foreground">
                © 2026 TimeChamp. All rights reserved.
              </p>
              <div className="flex gap-4 text-sm text-muted-foreground">
                <a href="#" className="hover:text-foreground">
                  Twitter
                </a>
                <a href="#" className="hover:text-foreground">
                  LinkedIn
                </a>
                <a href="#" className="hover:text-foreground">
                  GitHub
                </a>
              </div>
            </div>
          </div>
        </footer>
      </div>
  );
}
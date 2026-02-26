'use client';

import { Button } from '@/components/ui/button';
import Link from 'next/link';
import { Clock, Users, BarChart3, Lock, Zap, Shield } from 'lucide-react';

const features = [
  {
    title: 'Smart Clock-In/Out',
    description: 'One-tap attendance tracking with automatic timestamps',
    icon: Clock,
  },
  {
    title: 'Team Management',
    description: 'Manage employees, roles, and permissions effortlessly',
    icon: Users,
  },
  {
    title: 'Real-Time Analytics',
    description: 'Visualize attendance patterns and productivity metrics',
    icon: BarChart3,
  },
  {
    title: 'Secure Authentication',
    description: 'JWT-based security with refresh token management',
    icon: Lock,
  },
  {
    title: 'Fast Performance',
    description: 'Built for speed with optimized microservices architecture',
    icon: Zap,
  },
  {
    title: 'Data Protection',
    description: 'Enterprise-grade security and data privacy standards',
    icon: Shield,
  },
];

export default function Home() {
  return (
    <div className="min-h-screen bg-white text-foreground">
      {/* Navigation */}
      <nav className="border-b border-border bg-white sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4 flex justify-between items-center">
          <div className="text-2xl font-bold text-primary">TimeChamp</div>
          <div className="flex gap-4">
            <Link href="/auth/login">
              <Button variant="ghost">Sign In</Button>
            </Link>
            <Link href="/auth/register">
              <Button>Get Started</Button>
            </Link>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-32 text-center">
        <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-1000">
          <h1 className="text-5xl md:text-7xl font-bold text-balance">
            Employee Attendance
            <span className="block text-primary">Made Simple</span>
          </h1>
          <p className="text-lg md:text-xl text-muted-foreground max-w-2xl mx-auto text-balance">
            The modern platform for tracking attendance, managing timesheets, and analyzing productivity. Built for teams of any size.
          </p>
          <div className="flex gap-4 justify-center pt-4">
            <Link href="/auth/register">
              <Button size="lg" className="h-12 px-8">Start Free Trial</Button>
            </Link>
            <Link href="#features">
              <Button variant="outline" size="lg" className="h-12 px-8">Learn More</Button>
            </Link>
          </div>
        </div>
      </section>

      {/* Quick Stats */}
      <section className="bg-secondary py-12 border-y border-border">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid grid-cols-3 gap-8 text-center">
            <div>
              <div className="text-3xl font-bold text-primary">1000+</div>
              <p className="text-muted-foreground">Companies Trust Us</p>
            </div>
            <div>
              <div className="text-3xl font-bold text-primary">99.9%</div>
              <p className="text-muted-foreground">Uptime Guaranteed</p>
            </div>
            <div>
              <div className="text-3xl font-bold text-primary">24/7</div>
              <p className="text-muted-foreground">Support Available</p>
            </div>
          </div>
        </div>
      </section>

      {/* Features Grid */}
      <section id="features" className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="text-center mb-16">
          <h2 className="text-4xl font-bold mb-4">Powerful Features</h2>
          <p className="text-lg text-muted-foreground">Everything you need to manage employee attendance</p>
        </div>
        <div className="grid md:grid-cols-3 gap-8">
          {features.map((feature, i) => {
            const Icon = feature.icon;
            return (
              <div
                key={i}
                className="p-6 border border-border rounded-2xl bg-white hover:shadow-lg hover:-translate-y-1 transition-all duration-300 group"
              >
                <div className="w-12 h-12 rounded-lg bg-secondary flex items-center justify-center mb-4 group-hover:bg-primary group-hover:text-white transition-colors">
                  <Icon className="w-6 h-6" />
                </div>
                <h3 className="text-lg font-semibold mb-2">{feature.title}</h3>
                <p className="text-muted-foreground">{feature.description}</p>
              </div>
            );
          })}
        </div>
      </section>

      {/* CTA Section */}
      <section className="bg-primary text-white py-16">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center space-y-6">
          <h2 className="text-4xl font-bold">Ready to Get Started?</h2>
          <p className="text-lg opacity-90">Join hundreds of companies using TimeChamp for attendance management</p>
          <Link href="/auth/register">
            <Button size="lg" variant="secondary" className="h-12 px-8">
              Start Your Free Trial
            </Button>
          </Link>
        </div>
      </section>

      {/* Footer */}
      <footer className="bg-white border-t border-border py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="grid md:grid-cols-4 gap-8 mb-8">
            <div>
              <div className="text-xl font-bold text-primary mb-4">TimeChamp</div>
              <p className="text-muted-foreground">Modern attendance management for modern teams</p>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Product</h4>
              <ul className="space-y-2 text-muted-foreground">
                <li><a href="#" className="hover:text-foreground">Features</a></li>
                <li><a href="#" className="hover:text-foreground">Pricing</a></li>
                <li><a href="#" className="hover:text-foreground">Security</a></li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Company</h4>
              <ul className="space-y-2 text-muted-foreground">
                <li><a href="#" className="hover:text-foreground">About</a></li>
                <li><a href="#" className="hover:text-foreground">Blog</a></li>
                <li><a href="#" className="hover:text-foreground">Contact</a></li>
              </ul>
            </div>
            <div>
              <h4 className="font-semibold mb-4">Legal</h4>
              <ul className="space-y-2 text-muted-foreground">
                <li><a href="#" className="hover:text-foreground">Privacy</a></li>
                <li><a href="#" className="hover:text-foreground">Terms</a></li>
                <li><a href="#" className="hover:text-foreground">Compliance</a></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-border pt-8 flex justify-between items-center">
            <p className="text-muted-foreground">&copy; 2026 TimeChamp. All rights reserved.</p>
            <div className="flex gap-4 text-muted-foreground">
              <a href="#" className="hover:text-foreground">Twitter</a>
              <a href="#" className="hover:text-foreground">LinkedIn</a>
              <a href="#" className="hover:text-foreground">GitHub</a>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
}

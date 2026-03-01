import type { Metadata, Viewport } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import { Analytics } from "@vercel/analytics/next";
import { Toaster } from "sonner";
import "./globals.css";

const geist = Geist({ subsets: ["latin"], variable: "--font-sans" });
const geistMono = Geist_Mono({ subsets: ["latin"], variable: "--font-mono" });

export const metadata: Metadata = {
  metadataBase: new URL(process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"),
  title: {
    default: "TimeChamp — Employee Attendance Management",
    template: "%s — TimeChamp",
  },
  description: "Modern employee attendance tracking and time management system",
  applicationName: "TimeChamp",
  generator: "Next.js",
  manifest: "/manifest.webmanifest",
  icons: {
    icon: [
      { url: "/icon-light-32x32.png", media: "(prefers-color-scheme: light)" },
      { url: "/icon-dark-32x32.png", media: "(prefers-color-scheme: dark)" },
      { url: "/icon.svg", type: "image/svg+xml" },
    ],
    apple: "/apple-icon.png",
  },
  openGraph: {
    type: "website",
    siteName: "TimeChamp",
    title: "TimeChamp — Employee Attendance Management",
    description: "Modern employee attendance tracking and time management system",
    url: "/",
    images: [{ url: "/og.png", width: 1200, height: 630, alt: "TimeChamp" }],
  },
  twitter: {
    card: "summary_large_image",
    title: "TimeChamp — Employee Attendance Management",
    description: "Modern employee attendance tracking and time management system",
    images: ["/og.png"],
  },
  robots: {
    index: true,
    follow: true,
  },
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: [
    { media: "(prefers-color-scheme: light)", color: "#ffffff" },
    { media: "(prefers-color-scheme: dark)", color: "#0f1419" },
  ],
  colorScheme: "light dark",
};

export default function RootLayout({
                                     children,
                                   }: Readonly<{ children: React.ReactNode }>) {
  return (
      <html lang="en" suppressHydrationWarning>
      <body
          className={[
            geist.variable,
            geistMono.variable,
            "min-h-screen font-sans antialiased",
            "bg-background text-foreground",
            "selection:bg-primary/20 selection:text-foreground",
          ].join(" ")}
      >
      {/* Global toast system */}
      <Toaster
          richColors
          closeButton
          position="top-right"
          toastOptions={{
            duration: 3500,
          }}
      />

      {children}

      <Analytics />
      </body>
      </html>
  );
}
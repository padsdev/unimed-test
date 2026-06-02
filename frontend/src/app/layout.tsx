import type { Metadata } from "next";
import { Merriweather_Sans, Source_Sans_3 } from "next/font/google";

import { AppShell } from "@/components/layout/app-shell";
import { AppQueryProvider } from "@/contexts/query-provider";
import { ToastProvider } from "@/components/ui/toast";

import "./globals.css";

const sourceSans = Source_Sans_3({
  variable: "--font-source-sans",
  subsets: ["latin"],
});

const merriweatherSans = Merriweather_Sans({
  variable: "--font-merriweather-sans",
  subsets: ["latin"],
  weight: ["400", "700"],
});

export const metadata: Metadata = {
  title: "Clinica Prime | Operacoes Medicas",
  description: "Painel de gestao de pacientes, atendimentos e historicos clinicos",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="pt-BR" className={`${sourceSans.variable} ${merriweatherSans.variable} h-full antialiased`}>
      <body className="min-h-full flex flex-col">
        <AppQueryProvider>
          <ToastProvider>
            <AppShell>{children}</AppShell>
          </ToastProvider>
        </AppQueryProvider>
      </body>
    </html>
  );
}

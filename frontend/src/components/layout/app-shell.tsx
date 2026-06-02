"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Menu, Stethoscope, X } from "lucide-react";
import { useState } from "react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

const navItems = [
  { href: "/", label: "Painel" },
  { href: "/pacientes", label: "Pacientes" },
  { href: "/atendimentos", label: "Atendimentos" },
  { href: "/procedimentos", label: "Procedimentos" },
  { href: "/historico", label: "Historico" },
];

function NavContent({ onClickItem }: { onClickItem?: () => void }) {
  const pathname = usePathname();

  return (
    <nav className="space-y-2">
      {navItems.map((item) => {
        const active = pathname === item.href;
        return (
          <Link
            key={item.href}
            href={item.href}
            onClick={onClickItem}
            className={cn(
              "block rounded-xl px-3 py-2 text-[0.95rem] font-semibold transition-colors",
              active
                ? "bg-[color:var(--brand-500)] text-[color:var(--primary-foreground)]"
                : "text-[color:var(--text-700)] hover:bg-[color:var(--surface-2)]",
            )}
          >
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const [drawerOpen, setDrawerOpen] = useState(false);

  return (
    <div className="min-h-dvh">
      <div className="mx-auto grid max-w-[1360px] grid-cols-1 gap-4 p-4 lg:grid-cols-[260px_1fr] lg:p-6">
        <aside className="hidden rounded-2xl border border-border bg-card p-4 shadow-[var(--shadow-card)] lg:block">
          <div className="mb-8 flex items-center gap-3 px-2">
            <div className="rounded-lg bg-[color:var(--surface-2)] p-2 text-[color:var(--brand-700)]">
              <Stethoscope className="size-5" />
            </div>
            <div>
              <p className="text-sm font-bold text-[color:var(--text-900)]">Clinica Prime</p>
              <p className="text-xs text-[color:var(--text-500)]">Gestao assistencial</p>
            </div>
          </div>
          <NavContent />
        </aside>

        <div className="flex min-h-0 flex-col gap-4 lg:h-[calc(100dvh-3rem)]">
          <header className="relative flex items-center justify-center rounded-2xl border border-border bg-card px-4 py-3 shadow-[var(--shadow-soft)]">
            <div className="text-center">
              <p className="text-xs font-semibold uppercase tracking-[0.14em] text-[color:var(--text-500)]">
                Plataforma clinica
              </p>
              <p className="text-[1rem] font-bold text-[color:var(--text-900)]">Painel operacional</p>
            </div>
            <Button
              className="absolute right-4 lg:hidden"
              variant="outline"
              size="icon-sm"
              onClick={() => setDrawerOpen(true)}
            >
              <Menu className="size-4" />
            </Button>
          </header>
          <main className="min-h-0 flex-1 overflow-hidden">
            {children}
          </main>
        </div>
      </div>

      {drawerOpen ? (
        <div className="fixed inset-0 z-50 bg-black/30 lg:hidden" role="presentation" onClick={() => setDrawerOpen(false)}>
          <aside
            role="dialog"
            aria-modal="true"
            className="h-full w-[84%] max-w-[320px] bg-card p-4 shadow-xl"
            onClick={(event) => event.stopPropagation()}
          >
            <div className="mb-6 flex items-center justify-between">
              <p className="text-sm font-bold text-[color:var(--text-900)]">Navegacao</p>
              <Button variant="ghost" size="icon-sm" onClick={() => setDrawerOpen(false)}>
                <X className="size-4" />
              </Button>
            </div>
            <NavContent onClickItem={() => setDrawerOpen(false)} />
          </aside>
        </div>
      ) : null}
    </div>
  );
}

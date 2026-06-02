export function SectionFrame({
  title,
  description,
  children,
}: {
  title: string;
  description: string;
  children: React.ReactNode;
}) {
  return (
    <section className="flex h-full min-h-0 flex-col gap-4 rounded-2xl border border-border bg-card p-4 shadow-[var(--shadow-card)] md:p-6">
      <header className="space-y-1">
        <h1 className="text-2xl font-bold text-[color:var(--text-900)]">{title}</h1>
        <p className="text-sm text-[color:var(--text-500)]">{description}</p>
      </header>
      <div className="min-h-0 flex-1 overflow-auto">{children}</div>
    </section>
  );
}

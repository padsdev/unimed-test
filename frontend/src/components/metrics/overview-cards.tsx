interface OverviewCardsProps {
  pacientes: number;
  atendimentos: number;
  procedimentos: number;
}

const metrics = [
  { key: "pacientes", label: "Pacientes ativos" },
  { key: "atendimentos", label: "Atendimentos recentes" },
  { key: "procedimentos", label: "Procedimentos faturaveis" },
] as const;

export function OverviewCards({ pacientes, atendimentos, procedimentos }: OverviewCardsProps) {
  const values = { pacientes, atendimentos, procedimentos };

  return (
    <section className="grid gap-4 md:grid-cols-3">
      {metrics.map((metric) => (
        <article key={metric.key} className="rounded-2xl border border-border bg-card p-4 shadow-[var(--shadow-soft)]">
          <p className="text-sm text-[color:var(--text-500)]">{metric.label}</p>
          <p className="mt-2 text-3xl font-bold text-[color:var(--brand-700)]">{values[metric.key]}</p>
        </article>
      ))}
    </section>
  );
}

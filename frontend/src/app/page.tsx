"use client";

import { AlertTriangle, LoaderCircle } from "lucide-react";

import { SimpleTable } from "@/components/clinical/simple-table";
import { SectionFrame } from "@/components/clinical/section-frame";
import { OverviewCards } from "@/components/metrics/overview-cards";
import { useClinicalOverview } from "@/hooks/use-clinical-overview";
import { Button } from "@/components/ui/button";

export default function Home() {
  const { pacientes, atendimentos, procedimentos, isLoading, isError } = useClinicalOverview();

  if (isLoading) {
    return (
      <section className="flex min-h-[260px] items-center justify-center rounded-2xl border border-border bg-card shadow-[var(--shadow-soft)]">
        <div className="flex items-center gap-3 text-[color:var(--text-700)]">
          <LoaderCircle className="size-5 animate-spin" />
          <p>Carregando indicadores operacionais...</p>
        </div>
      </section>
    );
  }

  if (isError) {
    return (
      <section className="rounded-2xl border border-[color:var(--danger-500)]/30 bg-[color:var(--surface-1)] p-8 shadow-[var(--shadow-soft)]">
        <div className="flex items-center gap-3 text-[color:var(--danger-500)]">
          <AlertTriangle className="size-5" />
          <h1 className="text-xl font-bold">Nao foi possivel carregar o painel</h1>
        </div>
        <p className="mt-2 text-sm text-[color:var(--text-500)]">Tente novamente. Se persistir, valide conectividade com a API.</p>
      </section>
    );
  }

  return (
    <div className="grid h-full min-h-0 grid-rows-[auto_1fr] gap-4">
      <OverviewCards
        pacientes={pacientes.data?.length ?? 0}
        atendimentos={atendimentos.data?.length ?? 0}
        procedimentos={procedimentos.data?.length ?? 0}
      />

      <SectionFrame
        title="Visao executiva"
        description="Acompanhamento rapido dos dados clinicos para triagem operacional e planejamento assistencial."
      >
        <SimpleTable
          headers={["Paciente", "Medico", "Data (UTC)", "Observacao"]}
          rows={
            atendimentos.data?.map((atendimento) => {
              const paciente = pacientes.data?.find((item) => item.id === atendimento.pacienteId);
              return [
                paciente?.nome ?? "Nao identificado",
                atendimento.medico,
                atendimento.dataAtendimento,
                atendimento.observacoes,
              ];
            }) ?? []
          }
        />
        <div className="flex flex-col gap-2 rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-4 text-sm text-[color:var(--text-700)] md:flex-row md:items-center md:justify-between">
          <p>Frontend-first ativo com dados mockados e contratos tipados para acelerar UX.</p>
          <Button variant="secondary">Conferir historico consolidado</Button>
        </div>
      </SectionFrame>
    </div>
  );
}

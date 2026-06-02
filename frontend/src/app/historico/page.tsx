"use client";

import { useMemo } from "react";

import { useClinicalOverview } from "@/hooks/use-clinical-overview";
import { SectionFrame } from "@/components/clinical/section-frame";

export default function HistoricoPage() {
  const { pacientes, atendimentos, procedimentos } = useClinicalOverview();

  const agrupado = useMemo(() => {
    const listaPacientes = pacientes.data ?? [];
    const listaAtendimentos = atendimentos.data ?? [];
    const listaProcedimentos = procedimentos.data ?? [];

    return listaPacientes.map((paciente) => ({
      paciente,
      atendimentos: listaAtendimentos
        .filter((atendimento) => atendimento.pacienteId === paciente.id)
        .map((atendimento) => ({
          ...atendimento,
          procedimentos: listaProcedimentos.filter((procedimento) => procedimento.atendimentoId === atendimento.id),
        })),
    }));
  }, [atendimentos.data, pacientes.data, procedimentos.data]);

  return (
    <div className="h-full min-h-0">
      <SectionFrame
        title="Historico consolidado"
        description="Vista unica de paciente, atendimentos e procedimentos para validacao do endpoint de integracao."
      >
        <div className="space-y-4">
          {agrupado.map((item) => (
            <article
              key={item.paciente.id}
              className="space-y-3 rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-1)] p-4"
            >
              <header>
                <h2 className="text-lg font-bold text-[color:var(--text-900)]">{item.paciente.nome}</h2>
                <p className="text-sm text-[color:var(--text-500)]">CPF {item.paciente.cpf}</p>
              </header>
              <div className="space-y-2">
                {item.atendimentos.map((atendimento) => (
                  <div key={atendimento.id} className="rounded-lg bg-[color:var(--surface-2)] p-3">
                    <p className="text-sm font-semibold text-[color:var(--text-700)]">
                      Atendimento #{atendimento.id} · {atendimento.medico}
                    </p>
                    <p className="text-xs text-[color:var(--text-500)]">{atendimento.dataAtendimento}</p>
                    <ul className="mt-2 space-y-1 text-sm text-[color:var(--text-700)]">
                      {atendimento.procedimentos.map((procedimento) => (
                        <li key={procedimento.id}>
                          {procedimento.nome} - R$ {procedimento.valor.toFixed(2)}
                        </li>
                      ))}
                    </ul>
                  </div>
                ))}
              </div>
            </article>
          ))}
        </div>
      </SectionFrame>
    </div>
  );
}

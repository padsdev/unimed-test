"use client";

import { useState } from "react";
import { ArrowUpDown, ChevronDown, ChevronRight, LoaderCircle } from "lucide-react";

import { SectionFrame } from "@/components/clinical/section-frame";
import { Combobox } from "@/components/ui/combobox";
import { Button } from "@/components/ui/button";
import { useHistoricoPaciente } from "@/hooks/use-historico";
import { useListPacientes } from "@/hooks/use-pacientes";

export default function HistoricoPage() {
  const [selectedPacienteId, setSelectedPacienteId] = useState<number | null>(null);
  const [sortOrder, setSortOrder] = useState<"desc" | "asc">("desc");
  const [expandedAtendimentos, setExpandedAtendimentos] = useState<Set<number>>(new Set());

  const { data: pacientesData } = useListPacientes({ size: 100 });
  const { data: historico, isLoading, isError } = useHistoricoPaciente(selectedPacienteId, sortOrder);

  const pacientes = (pacientesData?.items ?? []).map((p) => ({
    value: p.id,
    label: p.nome,
    subtitle: p.cpf,
  }));

  function toggleExpand(id: number) {
    setExpandedAtendimentos((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  return (
    <div className="h-full min-h-0">
      <SectionFrame title="Histórico consolidado" description="Vista hierárquica por paciente com timeline de atendimentos e procedimentos.">
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end">
          <div className="flex-1">
            <label className="mb-1 block text-xs font-semibold uppercase text-[color:var(--text-500)]">Selecionar paciente</label>
            <Combobox
              options={pacientes}
              value={selectedPacienteId}
              onChange={setSelectedPacienteId}
              placeholder="Buscar paciente por nome ou CPF..."
              emptyMessage="Nenhum paciente encontrado"
            />
          </div>
          {selectedPacienteId && (
            <Button
              variant="outline"
              onClick={() => setSortOrder(sortOrder === "desc" ? "asc" : "desc")}
            >
              <ArrowUpDown className="size-4" />
              {sortOrder === "desc" ? "Mais recentes primeiro" : "Mais antigos primeiro"}
            </Button>
          )}
        </div>

        {!selectedPacienteId && (
          <div className="flex min-h-[200px] items-center justify-center rounded-xl border-2 border-dashed border-[color:var(--border-300)]">
            <p className="text-sm text-[color:var(--text-400)]">Selecione um paciente para visualizar o histórico</p>
          </div>
        )}

        {isLoading && (
          <div className="flex min-h-[200px] items-center justify-center text-[color:var(--text-500)]">
            <LoaderCircle className="size-5 animate-spin" />
          </div>
        )}

        {isError && (
          <p className="text-sm text-[color:var(--danger-500)]">Erro ao carregar histórico.</p>
        )}

        {historico && (
          <div className="space-y-4" role="region" aria-label="Histórico do paciente">
            <article className="rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-1)] p-4">
              <header className="mb-4">
                <h2 className="text-xl font-bold text-[color:var(--text-900)]">{historico.paciente.nome}</h2>
                <div className="mt-1 flex flex-wrap gap-4 text-sm text-[color:var(--text-500)]">
                  <span>CPF {historico.paciente.cpf}</span>
                  <span>Nasc. {historico.paciente.dataNascimento}</span>
                  <span>{historico.paciente.telefone}</span>
                </div>
              </header>

              <div className="space-y-2">
                <p className="text-xs font-semibold uppercase text-[color:var(--text-400)]">
                  {historico.atendimentos.length} atendimento(s)
                </p>

                {historico.atendimentos.length === 0 ? (
                  <div className="flex flex-col items-center gap-3 rounded-xl border border-dashed border-[color:var(--border-300)] bg-[color:var(--surface-2)] p-8 text-center">
                    <p className="text-sm text-[color:var(--text-500)]">Nenhum atendimento registrado para este paciente.</p>
                    <Button variant="secondary">Cadastrar atendimento</Button>
                  </div>
                ) : (
                  <div className="relative">
                    <div className="absolute bottom-0 left-[15px] top-0 w-0.5 bg-[color:var(--border-200)]" />
                    {historico.atendimentos.map((atendimento) => {
                      const expanded = expandedAtendimentos.has(atendimento.id);
                      const totalValor = atendimento.procedimentos.reduce((acc, p) => acc + p.valor, 0);
                      return (
                        <div key={atendimento.id} className="relative pb-4 last:pb-0">
                          <div className="absolute left-[11px] top-1.5 size-[10px] rounded-full border-2 border-[color:var(--brand-500)] bg-[color:var(--surface-1)]" />
                          <div className="ml-9 rounded-lg border border-[color:var(--border-200)] bg-[color:var(--surface-2)]">
                            <button
                              type="button"
                              onClick={() => toggleExpand(atendimento.id)}
                              className="flex w-full cursor-pointer items-center justify-between px-4 py-3 text-left"
                              aria-expanded={expanded}
                              aria-controls={`procedimentos-${atendimento.id}`}
                            >
                              <div className="flex-1">
                                <div className="flex items-center gap-2">
                                  <p className="text-sm font-semibold text-[color:var(--text-900)]">
                                    {atendimento.medico}
                                  </p>
                                  {expanded ? <ChevronDown className="size-3.5 text-[color:var(--text-400)]" /> : <ChevronRight className="size-3.5 text-[color:var(--text-400)]" />}
                                </div>
                                <p className="text-xs text-[color:var(--text-500)]">
                                  {new Date(atendimento.dataAtendimento).toLocaleString("pt-BR")}
                                </p>
                              </div>
                              <div className="text-right">
                                <p className="text-sm font-medium text-[color:var(--text-700)]">{atendimento.procedimentos.length} procedimento(s)</p>
                                <p className="text-xs text-[color:var(--text-500)]">R$ {totalValor.toFixed(2)}</p>
                              </div>
                            </button>

                            {expanded && (
                              <div id={`procedimentos-${atendimento.id}`} className="border-t border-[color:var(--border-200)] px-4 py-3">
                                {atendimento.observacoes && (
                                  <p className="mb-3 text-sm text-[color:var(--text-600)] italic">
                                    {atendimento.observacoes}
                                  </p>
                                )}
                                {atendimento.procedimentos.length === 0 ? (
                                  <p className="text-xs text-[color:var(--text-400)]">Nenhum procedimento registrado.</p>
                                ) : (
                                  <table className="w-full text-sm">
                                    <thead>
                                      <tr className="text-xs text-[color:var(--text-500)]">
                                        <th className="pb-1 pr-2 text-left font-medium">Procedimento</th>
                                        <th className="pb-1 text-right font-medium">Valor</th>
                                      </tr>
                                    </thead>
                                    <tbody>
                                      {atendimento.procedimentos.map((proc) => (
                                        <tr key={proc.id} className="border-t border-[color:var(--border-200)] text-[color:var(--text-700)]">
                                          <td className="py-1.5 pr-2">{proc.nome}</td>
                                          <td className="py-1.5 text-right">R$ {proc.valor.toFixed(2)}</td>
                                        </tr>
                                      ))}
                                      <tr className="border-t border-[color:var(--border-200)] font-medium text-[color:var(--text-900)]">
                                        <td className="py-1.5 pr-2">Total</td>
                                        <td className="py-1.5 text-right">R$ {totalValor.toFixed(2)}</td>
                                      </tr>
                                    </tbody>
                                  </table>
                                )}
                              </div>
                            )}
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}
              </div>
            </article>
          </div>
        )}
      </SectionFrame>
    </div>
  );
}

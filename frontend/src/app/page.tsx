"use client";

import { useMemo, useState } from "react";
import {
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  Clock,
  DollarSign,
  LoaderCircle,
  Pencil,
  Plus,
} from "lucide-react";

import { SectionFrame } from "@/components/clinical/section-frame";
import { OverviewCards } from "@/components/metrics/overview-cards";
import { Button } from "@/components/ui/button";
import { Sheet } from "@/components/ui/sheet";
import { useToast } from "@/components/ui/toast";
import {
  useAtualizarAtendimento,
  useCriarAtendimento,
  useListAtendimentos,
} from "@/hooks/use-atendimentos";
import { useListPacientes } from "@/hooks/use-pacientes";
import { useListProcedimentos } from "@/hooks/use-procedimentos";
import type {
  AtualizarAtendimentoRequest,
  Atendimento,
  CriarAtendimentoRequest,
  Paciente,
} from "@/types/domain";

type AtendimentoComPaciente = Atendimento & {
  pacienteNome: string;
  data: Date;
};

type DrawerMode = "create" | "edit";

export default function Home() {
  const [viewMonth, setViewMonth] = useState(() => {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), 1);
  });
  const [selectedDateKey, setSelectedDateKey] = useState<string | null>(null);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerMode, setDrawerMode] = useState<DrawerMode>("create");
  const [editingAtendimento, setEditingAtendimento] = useState<AtendimentoComPaciente | null>(null);

  const { addToast } = useToast();

  const {
    data: pacientesData,
    isLoading: loadingPacientes,
    isError: errorPacientes,
  } = useListPacientes({ size: 100 });
  const {
    data: atendimentosData,
    isLoading: loadingAtendimentos,
    isError: errorAtendimentos,
  } = useListAtendimentos({ size: 100, sort: "dataAtendimento,asc" });
  const {
    data: procedimentosData,
    isLoading: loadingProcedimentos,
    isError: errorProcedimentos,
  } = useListProcedimentos({ size: 100 });

  const criarAtendimento = useCriarAtendimento();
  const atualizarAtendimento = useAtualizarAtendimento();

  const isLoading = loadingPacientes || loadingAtendimentos || loadingProcedimentos;
  const isError = errorPacientes || errorAtendimentos || errorProcedimentos;

  const pacientes = pacientesData?.items ?? [];
  const atendimentos = atendimentosData?.items ?? [];
  const procedimentos = procedimentosData?.items ?? [];
  const now = new Date();

  const atendimentosComPaciente = useMemo(() => {
    const listaAtendimentos = atendimentosData?.items ?? [];
    const listaPacientes = pacientesData?.items ?? [];

    return listaAtendimentos.map((atendimento) => {
      const paciente = listaPacientes.find((p) => p.id === atendimento.pacienteId);
      return {
        ...atendimento,
        pacienteNome: paciente?.nome ?? "Não identificado",
        data: new Date(atendimento.dataAtendimento),
      };
    });
  }, [atendimentosData?.items, pacientesData?.items]);

  const ultimosAtendimentos = [...atendimentosComPaciente]
    .filter((atendimento) => atendimento.data < now)
    .sort((a, b) => b.data.getTime() - a.data.getTime())
    .slice(0, 8);

  const proximosAtendimentos = [...atendimentosComPaciente]
    .filter((atendimento) => atendimento.data >= now)
    .sort((a, b) => a.data.getTime() - b.data.getTime())
    .slice(0, 8);

  const ultimosProcedimentos = [...procedimentos].reverse().slice(0, 6);

  const atendimentosPorDia = useMemo(() => {
    const map = new Map<string, AtendimentoComPaciente[]>();
    for (const atendimento of atendimentosComPaciente) {
      const key = formatDateKey(atendimento.data);
      const lista = map.get(key) ?? [];
      lista.push(atendimento);
      map.set(key, lista);
    }
    return map;
  }, [atendimentosComPaciente]);

  const diasCalendario = useMemo(() => {
    const year = viewMonth.getFullYear();
    const month = viewMonth.getMonth();
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const leading = firstDay.getDay();
    const daysInMonth = lastDay.getDate();
    const cells: Array<Date | null> = [];

    for (let i = 0; i < leading; i++) cells.push(null);
    for (let day = 1; day <= daysInMonth; day++) cells.push(new Date(year, month, day));
    while (cells.length % 7 !== 0) cells.push(null);
    return cells;
  }, [viewMonth]);

  const atendimentosDiaSelecionado = selectedDateKey
    ? (atendimentosPorDia.get(selectedDateKey) ?? [])
        .slice()
        .sort((a, b) => a.data.getTime() - b.data.getTime())
    : [];

  const monthLabel = viewMonth.toLocaleDateString("pt-BR", {
    month: "long",
    year: "numeric",
  });

  const selectedDateLabel = selectedDateKey
    ? parseDateKey(selectedDateKey).toLocaleDateString("pt-BR", {
        weekday: "long",
        day: "2-digit",
        month: "long",
        year: "numeric",
      })
    : null;

  function openCreateDrawer() {
    setDrawerMode("create");
    setEditingAtendimento(null);
    setDrawerOpen(true);
  }

  function openEditDrawer(atendimento: AtendimentoComPaciente) {
    setDrawerMode("edit");
    setEditingAtendimento(atendimento);
    setDrawerOpen(true);
  }

  function handleCreateAtendimento(data: CriarAtendimentoRequest) {
    criarAtendimento.mutate(data, {
      onSuccess: () => {
        addToast({ type: "success", title: "Atendimento criado" });
        setDrawerOpen(false);
      },
      onError: () => {
        addToast({ type: "error", title: "Erro ao criar atendimento" });
      },
    });
  }

  function handleUpdateAtendimento(id: number, data: AtualizarAtendimentoRequest) {
    atualizarAtendimento.mutate(
      { id, data },
      {
        onSuccess: () => {
          addToast({ type: "success", title: "Atendimento atualizado" });
          setDrawerOpen(false);
          setEditingAtendimento(null);
        },
        onError: () => {
          addToast({ type: "error", title: "Erro ao atualizar atendimento" });
        },
      },
    );
  }

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
          <h1 className="text-xl font-bold">Não foi possível carregar o painel</h1>
        </div>
        <p className="mt-2 text-sm text-[color:var(--text-500)]">
          Tente novamente. Se persistir, valide conectividade com a API.
        </p>
      </section>
    );
  }

  return (
    <div className="grid h-full min-h-0 grid-rows-[auto_1fr] gap-4">
      <OverviewCards
        pacientes={pacientesData?.totalItems ?? 0}
        atendimentos={atendimentosData?.totalItems ?? 0}
        procedimentos={procedimentosData?.totalItems ?? 0}
      />

      <SectionFrame
        title="Calendário de atendimentos"
        description="Visão interativa com próximos, últimos, ações rápidas por dia e últimos procedimentos."
      >
        <div className="space-y-4">
          <div className="grid min-h-[42rem] grid-cols-1 gap-4 lg:grid-cols-[1.3fr_1fr]">
          <div className="rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-1)] p-3 h-138">
            <div className="mb-3 flex items-center justify-between gap-2">
              <button
                type="button"
                onClick={() =>
                  setViewMonth((prev) => new Date(prev.getFullYear(), prev.getMonth() - 1, 1))
                }
                className="cursor-pointer rounded-lg border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-1.5 text-[color:var(--text-600)] hover:border-[color:var(--brand-500)]"
              >
                <ChevronLeft className="size-4" />
              </button>
              <p className="text-sm font-semibold capitalize text-[color:var(--text-900)]">{monthLabel}</p>
              <button
                type="button"
                onClick={() =>
                  setViewMonth((prev) => new Date(prev.getFullYear(), prev.getMonth() + 1, 1))
                }
                className="cursor-pointer rounded-lg border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-1.5 text-[color:var(--text-600)] hover:border-[color:var(--brand-500)]"
              >
                <ChevronRight className="size-4" />
              </button>
            </div>

            <div className="mb-2 grid grid-cols-7 text-center text-xs font-semibold uppercase text-[color:var(--text-500)]">
              {["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"].map((weekday) => (
                <p key={weekday} className="py-1">
                  {weekday}
                </p>
              ))}
            </div>

            <div className="grid grid-cols-7 gap-1.5">
              {diasCalendario.map((day, index) => {
                if (!day)
                  return <div key={`empty-${index}`} className="h-20 rounded-md bg-transparent" />;

                const dayKey = formatDateKey(day);
                const dayEvents = atendimentosPorDia.get(dayKey) ?? [];
                const isSelected = selectedDateKey === dayKey;
                const hasPast = dayEvents.some((e) => e.data < now);
                const hasUpcoming = dayEvents.some((e) => e.data >= now);

                return (
                  <button
                    key={dayKey}
                    type="button"
                    onClick={() => {
                      setSelectedDateKey((prev) => (prev === dayKey ? null : dayKey));
                      setViewMonth(new Date(day.getFullYear(), day.getMonth(), 1));
                    }}
                    className={`h-20 rounded-md border p-1 text-left transition-colors ${
                      isSelected
                        ? "border-[color:var(--brand-500)] bg-[color:var(--brand-500)]/10"
                        : "border-[color:var(--border-200)] bg-[color:var(--surface-1)] hover:border-[color:var(--brand-500)]/60"
                    }`}
                  >
                    <p className="text-xs font-semibold text-[color:var(--text-900)]">{day.getDate()}</p>
                    <div className="mt-1 flex items-center gap-1">
                      {hasPast && <span className="size-2 rounded-full bg-[color:var(--text-500)]" />}
                      {hasUpcoming && (
                        <span className="size-2 rounded-full bg-[color:var(--brand-500)]" />
                      )}
                    </div>
                    {dayEvents.length > 0 && (
                      <p className="mt-1 text-[10px] text-[color:var(--text-500)]">{dayEvents.length} ag.</p>
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          <div className="grid min-h-0 grid-rows-1 gap-3">
            {selectedDateKey ? (
              <div className="rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-3 h-138">
                <div className="mb-3 flex flex-wrap items-center justify-between gap-2">
                  <div>
                    <p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">
                      Dia selecionado
                    </p>
                    <p className="text-sm font-medium capitalize text-[color:var(--text-900)]">
                      {selectedDateLabel}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setSelectedDateKey(null)}
                    className="cursor-pointer rounded-md px-2 py-1 text-xs text-[color:var(--text-500)] hover:bg-[color:var(--surface-1)]"
                  >
                    Limpar seleção
                  </button>
                </div>

                <div className="mb-3 flex flex-wrap gap-2">
                  <Button onClick={openCreateDrawer}>
                    <Plus className="size-4" />
                    Novo atendimento
                  </Button>
                  <Button
                    variant="outline"
                    onClick={() => {
                      if (atendimentosDiaSelecionado[0]) openEditDrawer(atendimentosDiaSelecionado[0]);
                    }}
                    disabled={atendimentosDiaSelecionado.length === 0}
                  >
                    <Pencil className="size-4" />
                    Editar atendimento
                  </Button>
                </div>

                <div className="max-h-[23rem] space-y-2 overflow-auto pr-1">
                  {atendimentosDiaSelecionado.length === 0 ? (
                    <p className="text-xs text-[color:var(--text-400)]">Sem atendimentos neste dia.</p>
                  ) : (
                    atendimentosDiaSelecionado.map((item) => (
                      <div
                        key={item.id}
                        className="rounded-lg border border-[color:var(--border-200)] bg-[color:var(--surface-1)] p-2"
                      >
                        <div className="flex items-center justify-between gap-2">
                          <p className="truncate text-xs font-medium text-[color:var(--text-900)]">
                            {item.pacienteNome}
                          </p>
                          <span className="flex items-center gap-1 text-[11px] text-[color:var(--text-500)]">
                            <Clock className="size-3" />
                            {item.data.toLocaleTimeString("pt-BR", {
                              hour: "2-digit",
                              minute: "2-digit",
                            })}
                          </span>
                        </div>
                        <div className="mt-1 flex items-center justify-between gap-2">
                          <p className="text-[11px] text-[color:var(--text-500)]">{item.medico}</p>
                          <button
                            type="button"
                            onClick={() => openEditDrawer(item)}
                            className="cursor-pointer rounded-md px-2 py-1 text-[11px] font-medium text-[color:var(--brand-600)] hover:bg-[color:var(--brand-500)]/10"
                          >
                            Editar
                          </button>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            ) : (
              <div className="grid min-h-0 grid-rows-2 gap-3">
                <div className="rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-3">
                  <p className="mb-2 text-xs font-semibold uppercase text-[color:var(--text-500)]">
                    Próximos atendimentos
                  </p>
                  <div className="space-y-2">
                    {proximosAtendimentos.length === 0 ? (
                      <p className="text-xs text-[color:var(--text-400)]">Sem agendamentos futuros.</p>
                    ) : (
                      proximosAtendimentos.map((item) => (
                        <div key={item.id} className="rounded-lg bg-[color:var(--surface-1)] p-2">
                          <p className="truncate text-xs font-medium text-[color:var(--text-900)]">
                            {item.pacienteNome}
                          </p>
                          <p className="text-[11px] text-[color:var(--text-500)]">
                            {item.data.toLocaleDateString("pt-BR")} · {item.medico}
                          </p>
                        </div>
                      ))
                    )}
                  </div>
                </div>

                <div className="rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-3 h-52">
                  <p className="mb-2 text-xs font-semibold uppercase text-[color:var(--text-500)]">
                    Últimos atendimentos
                  </p>
                  <div className="h-[10.75rem] space-y-2 overflow-auto pr-1">
                    {ultimosAtendimentos.length === 0 ? (
                      <p className="text-xs text-[color:var(--text-400)]">Sem registros.</p>
                    ) : (
                      ultimosAtendimentos.map((item) => (
                        <div key={item.id} className="rounded-lg bg-[color:var(--surface-1)] p-2">
                          <p className="truncate text-xs font-medium text-[color:var(--text-900)]">
                            {item.pacienteNome}
                          </p>
                          <p className="text-[11px] text-[color:var(--text-500)]">{item.medico}</p>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </div>
            )}
          </div>
          </div>

          <div className="rounded-xl border border-[color:var(--border-200)] bg-[color:var(--surface-2)] p-4">
            <div className="mb-3">
              <h2 className="text-base font-semibold text-[color:var(--text-900)]">Últimos procedimentos</h2>
              <p className="text-sm text-[color:var(--text-500)]">Procedimentos mais recentes cadastrados.</p>
            </div>

            <div className="space-y-3">
              {ultimosProcedimentos.length === 0 ? (
                <p className="text-sm text-[color:var(--text-400)]">Nenhum procedimento registrado.</p>
              ) : (
                ultimosProcedimentos.map((procedimento) => {
                  const atendimento = atendimentos.find((a) => a.id === procedimento.atendimentoId);
                  const paciente = atendimento
                    ? pacientes.find((p) => p.id === atendimento.pacienteId)
                    : null;

                  return (
                    <div
                      key={procedimento.id}
                      className="flex items-start gap-3 rounded-lg border border-[color:var(--border-200)] bg-[color:var(--surface-1)] p-3"
                    >
                      <div className="rounded-lg bg-[color:var(--mint-500)]/10 p-2 text-[color:var(--mint-500)]">
                        <DollarSign className="size-4" />
                      </div>
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-[color:var(--text-900)]">
                          {procedimento.nome}
                        </p>
                        <p className="truncate text-xs text-[color:var(--text-500)]">
                          {paciente?.nome ?? "Paciente não identificado"}
                        </p>
                      </div>
                      <div className="whitespace-nowrap text-sm font-semibold text-[color:var(--text-700)]">
                        R$ {procedimento.valor.toFixed(2)}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>
      </SectionFrame>

      <Sheet
        open={drawerOpen}
        onClose={() => {
          setDrawerOpen(false);
          setEditingAtendimento(null);
        }}
        title={drawerMode === "create" ? "Novo atendimento" : "Editar atendimento"}
      >
        <AtendimentoDrawerForm
          key={`${drawerMode}-${editingAtendimento?.id ?? "new"}-${selectedDateKey ?? "none"}`}
          mode={drawerMode}
          pacientes={pacientes}
          selectedDateKey={selectedDateKey}
          atendimento={editingAtendimento}
          loading={criarAtendimento.isPending || atualizarAtendimento.isPending}
          onCreate={handleCreateAtendimento}
          onUpdate={handleUpdateAtendimento}
        />
      </Sheet>
    </div>
  );
}

function AtendimentoDrawerForm({
  mode,
  pacientes,
  selectedDateKey,
  atendimento,
  loading,
  onCreate,
  onUpdate,
}: {
  mode: DrawerMode;
  pacientes: Paciente[];
  selectedDateKey: string | null;
  atendimento: AtendimentoComPaciente | null;
  loading: boolean;
  onCreate: (data: CriarAtendimentoRequest) => void;
  onUpdate: (id: number, data: AtualizarAtendimentoRequest) => void;
}) {
  const [pacienteId, setPacienteId] = useState<number>(() => {
    if (mode === "edit" && atendimento) return atendimento.pacienteId;
    return pacientes[0]?.id ?? 0;
  });
  const [dataAtendimento, setDataAtendimento] = useState(() => {
    if (mode === "edit" && atendimento) return toDateTimeLocal(atendimento.data);
    if (selectedDateKey) return `${selectedDateKey}T09:00`;
    const defaultDate = new Date();
    defaultDate.setMinutes(0, 0, 0);
    return toDateTimeLocal(defaultDate);
  });
  const [medico, setMedico] = useState(() =>
    mode === "edit" && atendimento ? atendimento.medico : "",
  );
  const [observacoes, setObservacoes] = useState(() =>
    mode === "edit" && atendimento ? atendimento.observacoes : "",
  );

  function handleSubmit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!dataAtendimento || !medico || !observacoes) return;

    const isoDate = new Date(dataAtendimento).toISOString();

    if (mode === "create") {
      if (!pacienteId) return;
      onCreate({ pacienteId, dataAtendimento: isoDate, medico, observacoes });
      return;
    }

    if (!atendimento) return;
    onUpdate(atendimento.id, { dataAtendimento: isoDate, medico, observacoes });
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      {mode === "create" && (
        <div>
          <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Paciente</label>
          <select
            value={pacienteId}
            onChange={(e) => setPacienteId(Number(e.target.value))}
            required
            className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
          >
            {pacientes.map((paciente) => (
              <option key={paciente.id} value={paciente.id}>
                {paciente.nome} — {paciente.cpf}
              </option>
            ))}
          </select>
        </div>
      )}

      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Data e hora</label>
        <input
          type="datetime-local"
          value={dataAtendimento}
          onChange={(e) => setDataAtendimento(e.target.value)}
          required
          className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
        />
      </div>

      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Médico</label>
        <input
          type="text"
          value={medico}
          onChange={(e) => setMedico(e.target.value)}
          required
          maxLength={200}
          className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
        />
      </div>

      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Observações</label>
        <textarea
          value={observacoes}
          onChange={(e) => setObservacoes(e.target.value)}
          required
          maxLength={2000}
          rows={4}
          className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
        />
      </div>

      <div className="flex justify-end">
        <Button type="submit" disabled={loading}>
          {loading ? "Salvando..." : "Salvar"}
        </Button>
      </div>
    </form>
  );
}

function formatDateKey(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function parseDateKey(dateKey: string) {
  const [year, month, day] = dateKey.split("-").map(Number);
  return new Date(year, month - 1, day);
}

function toDateTimeLocal(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const hour = String(date.getHours()).padStart(2, "0");
  const minute = String(date.getMinutes()).padStart(2, "0");
  return `${year}-${month}-${day}T${hour}:${minute}`;
}

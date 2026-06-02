"use client";

import { useState } from "react";
import { EllipsisVertical, Eye, LoaderCircle, Search } from "lucide-react";

import { Pagination } from "@/components/ui/pagination";
import { SectionFrame } from "@/components/clinical/section-frame";
import { Dialog } from "@/components/ui/dialog";
import { Sheet } from "@/components/ui/sheet";
import { DropdownMenu } from "@/components/ui/dropdown-menu";
import { useToast } from "@/components/ui/toast";
import { Button } from "@/components/ui/button";
import { useListAtendimentos, useCriarAtendimento, useAtualizarAtendimento, useExcluirAtendimento, useAtendimento } from "@/hooks/use-atendimentos";
import { useListPacientes } from "@/hooks/use-pacientes";
import type { CriarAtendimentoRequest, AtualizarAtendimentoRequest, Atendimento, Paciente } from "@/types/domain";

type SheetMode = "create" | "edit" | "view";

export default function AtendimentosPage() {
  const [page, setPage] = useState(0);
  const [q, setQ] = useState("");
  const [sheetOpen, setSheetOpen] = useState(false);
  const [sheetMode, setSheetMode] = useState<SheetMode>("create");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Atendimento | null>(null);

  const { data, isLoading, isError } = useListAtendimentos({ page, size: 10, q: q || undefined });
  const { data: viewData } = useAtendimento(selectedId);
  const { data: pacientesData } = useListPacientes({ size: 100 });
  const criar = useCriarAtendimento();
  const atualizar = useAtualizarAtendimento();
  const excluir = useExcluirAtendimento();
  const { addToast } = useToast();

  const pacientes = pacientesData?.items ?? [];

  function pacienteNome(id: number) {
    return pacientes.find((p) => p.id === id)?.nome ?? `ID ${id}`;
  }

  function openSheet(mode: SheetMode, id?: number) {
    setSheetMode(mode);
    setSelectedId(id ?? null);
    setSheetOpen(true);
  }

  function handleCreate(data: CriarAtendimentoRequest) {
    criar.mutate(data, {
      onSuccess: () => { addToast({ type: "success", title: "Atendimento registrado" }); setSheetOpen(false); },
      onError: () => { addToast({ type: "error", title: "Erro ao registrar" }); },
    });
  }

  function handleUpdate(data: AtualizarAtendimentoRequest) {
    if (!selectedId) return;
    atualizar.mutate({ id: selectedId, data }, {
      onSuccess: () => { addToast({ type: "success", title: "Atendimento atualizado" }); setSheetOpen(false); },
      onError: () => { addToast({ type: "error", title: "Erro ao atualizar" }); },
    });
  }

  function handleDelete() {
    if (!deleteTarget) return;
    excluir.mutate(deleteTarget.id, {
      onSuccess: () => { addToast({ type: "success", title: "Atendimento excluído" }); setDeleteTarget(null); },
      onError: () => { addToast({ type: "error", title: "Erro ao excluir" }); },
    });
  }

  return (
    <div className="flex h-full min-h-0 flex-col gap-4">
      <SectionFrame title="Atendimentos" description="Registro operacional com busca, paginação e operações de CRUD.">
        <div className="mb-4 flex items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-[color:var(--text-400)]" />
            <input
              type="text"
              value={q}
              onChange={(e) => { setQ(e.target.value); setPage(0); }}
              placeholder="Buscar por médico ou observação..."
              className="w-full rounded-lg border border-border bg-[color:var(--surface-1)] py-2 pl-9 pr-3 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
            />
          </div>
          <Button onClick={() => openSheet("create")}>Novo atendimento</Button>
        </div>

        {isLoading ? (
          <div className="flex min-h-[200px] items-center justify-center text-[color:var(--text-500)]">
            <LoaderCircle className="size-5 animate-spin" />
          </div>
        ) : isError ? (
          <p className="text-sm text-[color:var(--danger-500)]">Erro ao carregar atendimentos.</p>
        ) : (
          <>
            <div className="max-h-[400px] overflow-auto rounded-xl border border-[color:var(--border-200)]">
              <table className="w-full min-w-[640px] border-collapse">
                <thead className="bg-[color:var(--surface-2)] text-left text-sm text-[color:var(--text-700)]">
                  <tr>
                    <th className="px-3 py-2.5 font-semibold">Paciente</th>
                    <th className="px-3 py-2.5 font-semibold">Data</th>
                    <th className="px-3 py-2.5 font-semibold">Médico</th>
                    <th className="px-3 py-2.5 font-semibold">Observações</th>
                    <th className="w-20 px-3 py-2.5" />
                  </tr>
                </thead>
                <tbody>
                  {data?.items.map((item) => (
                    <tr key={item.id} className="border-t border-[color:var(--border-200)] text-sm text-[color:var(--text-700)]">
                      <td className="px-3 py-2.5 font-medium text-[color:var(--text-900)]">{pacienteNome(item.pacienteId)}</td>
                      <td className="px-3 py-2.5">{new Date(item.dataAtendimento).toLocaleDateString("pt-BR")}</td>
                      <td className="px-3 py-2.5">{item.medico}</td>
                      <td className="max-w-[200px] truncate px-3 py-2.5">{item.observacoes}</td>
                      <td className="flex items-center gap-1 px-3 py-2.5">
                        <button
                          type="button"
                          onClick={() => openSheet("view", item.id)}
                          className="cursor-pointer rounded-lg p-1 text-[color:var(--text-400)] hover:bg-[color:var(--surface-2)] hover:text-[color:var(--brand-600)]"
                          title="Visualizar"
                        >
                          <Eye className="size-4" />
                        </button>
                        <DropdownMenu
                          trigger={<EllipsisVertical className="size-4" />}
                          items={[
                            { label: "Editar", onClick: () => openSheet("edit", item.id) },
                            { label: "Excluir", onClick: () => setDeleteTarget(item), variant: "danger" },
                          ]}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {data && (
              <Pagination page={data.page} totalPages={data.totalPages} onPageChange={setPage} />
            )}
          </>
        )}
      </SectionFrame>

      <Sheet open={sheetOpen} onClose={() => setSheetOpen(false)} title={sheetMode === "create" ? "Novo atendimento" : sheetMode === "edit" ? "Editar atendimento" : "Dados do atendimento"}>
        {sheetMode === "view" && viewData ? (
          <div className="space-y-4">
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Paciente</p><p className="text-sm text-[color:var(--text-900)]">{pacienteNome(viewData.pacienteId)}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Data</p><p className="text-sm text-[color:var(--text-900)]">{new Date(viewData.dataAtendimento).toLocaleString("pt-BR")}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Médico</p><p className="text-sm text-[color:var(--text-900)]">{viewData.medico}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Observações</p><p className="text-sm text-[color:var(--text-900)] whitespace-pre-wrap">{viewData.observacoes}</p></div>
          </div>
        ) : (
          <AtendimentoForm
            mode={sheetMode as "create" | "edit"}
            defaultValues={sheetMode === "edit" ? viewData : undefined}
            pacientes={pacientes}
            onSubmit={(d) => sheetMode === "create" ? handleCreate(d as CriarAtendimentoRequest) : handleUpdate(d as AtualizarAtendimentoRequest)}
            loading={criar.isPending || atualizar.isPending}
          />
        )}
      </Sheet>

      <Dialog
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Excluir atendimento"
        description="Tem certeza? Procedimentos vinculados também serão removidos."
        confirmLabel="Excluir"
        variant="danger"
        loading={excluir.isPending}
      />
    </div>
  );
}

function AtendimentoForm({
  mode,
  defaultValues,
  pacientes,
  onSubmit,
  loading,
}: {
  mode: "create" | "edit";
  defaultValues?: Atendimento;
  pacientes: Paciente[];
  onSubmit: (data: CriarAtendimentoRequest | AtualizarAtendimentoRequest) => void;
  loading: boolean;
}) {
  const [pacienteId, setPacienteId] = useState(defaultValues?.pacienteId ?? (pacientes[0]?.id ?? 0));
  const [dataAtendimento, setDataAtendimento] = useState(defaultValues?.dataAtendimento?.slice(0, 16) ?? "");
  const [medico, setMedico] = useState(defaultValues?.medico ?? "");
  const [observacoes, setObservacoes] = useState(defaultValues?.observacoes ?? "");

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const iso = new Date(dataAtendimento).toISOString();
    if (mode === "create") {
      onSubmit({ pacienteId, dataAtendimento: iso, medico, observacoes } as CriarAtendimentoRequest);
    } else {
      onSubmit({ dataAtendimento: iso, medico, observacoes } as AtualizarAtendimentoRequest);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Paciente</label>
        <select value={pacienteId} onChange={(e) => setPacienteId(Number(e.target.value))} required disabled={mode === "edit"} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)] disabled:opacity-50">
          {pacientes.map((p) => <option key={p.id} value={p.id}>{p.nome} — {p.cpf}</option>)}
        </select>
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Data e hora</label>
        <input type="datetime-local" value={dataAtendimento} onChange={(e) => setDataAtendimento(e.target.value)} required className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Médico</label>
        <input type="text" value={medico} onChange={(e) => setMedico(e.target.value)} required maxLength={200} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Observações</label>
        <textarea value={observacoes} onChange={(e) => setObservacoes(e.target.value)} required maxLength={2000} rows={3} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <Button type="submit" disabled={loading}>{loading ? "Salvando..." : "Salvar"}</Button>
      </div>
    </form>
  );
}

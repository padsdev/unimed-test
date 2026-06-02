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
import { useListProcedimentos, useCriarProcedimento, useAtualizarProcedimento, useExcluirProcedimento, useProcedimento } from "@/hooks/use-procedimentos";
import { useListAtendimentos } from "@/hooks/use-atendimentos";
import type { CriarProcedimentoRequest, AtualizarProcedimentoRequest, Procedimento, Atendimento } from "@/types/domain";

type SheetMode = "create" | "edit" | "view";

export default function ProcedimentosPage() {
  const [page, setPage] = useState(0);
  const [q, setQ] = useState("");
  const [sheetOpen, setSheetOpen] = useState(false);
  const [sheetMode, setSheetMode] = useState<SheetMode>("create");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Procedimento | null>(null);

  const { data, isLoading, isError } = useListProcedimentos({ page, size: 10, q: q || undefined });
  const { data: viewData } = useProcedimento(selectedId);
  const { data: atendimentosData } = useListAtendimentos({ size: 100 });
  const criar = useCriarProcedimento();
  const atualizar = useAtualizarProcedimento();
  const excluir = useExcluirProcedimento();
  const { addToast } = useToast();

  const atendimentos = atendimentosData?.items ?? [];

  function atendimentoLabel(id: number) {
    const a = atendimentos.find((at) => at.id === id);
    return a ? `#${a.id} · ${new Date(a.dataAtendimento).toLocaleDateString("pt-BR")}` : `ID ${id}`;
  }

  function openSheet(mode: SheetMode, id?: number) {
    setSheetMode(mode);
    setSelectedId(id ?? null);
    setSheetOpen(true);
  }

  function handleCreate(data: CriarProcedimentoRequest) {
    criar.mutate(data, {
      onSuccess: () => { addToast({ type: "success", title: "Procedimento adicionado" }); setSheetOpen(false); },
      onError: () => { addToast({ type: "error", title: "Erro ao adicionar" }); },
    });
  }

  function handleUpdate(data: AtualizarProcedimentoRequest) {
    if (!selectedId) return;
    atualizar.mutate({ id: selectedId, data }, {
      onSuccess: () => { addToast({ type: "success", title: "Procedimento atualizado" }); setSheetOpen(false); },
      onError: () => { addToast({ type: "error", title: "Erro ao atualizar" }); },
    });
  }

  function handleDelete() {
    if (!deleteTarget) return;
    excluir.mutate(deleteTarget.id, {
      onSuccess: () => { addToast({ type: "success", title: "Procedimento excluído" }); setDeleteTarget(null); },
      onError: () => { addToast({ type: "error", title: "Erro ao excluir" }); },
    });
  }

  return (
    <div className="flex h-full min-h-0 flex-col gap-4">
      <SectionFrame title="Procedimentos" description="Catálogo de procedimentos com valores e vínculo ao atendimento.">
        <div className="mb-4 flex items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-[color:var(--text-400)]" />
            <input
              type="text"
              value={q}
              onChange={(e) => { setQ(e.target.value); setPage(0); }}
              placeholder="Buscar por nome do procedimento..."
              className="w-full rounded-lg border border-border bg-[color:var(--surface-1)] py-2 pl-9 pr-3 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
            />
          </div>
          <Button onClick={() => openSheet("create")}>Novo procedimento</Button>
        </div>

        {isLoading ? (
          <div className="flex min-h-[200px] items-center justify-center text-[color:var(--text-500)]">
            <LoaderCircle className="size-5 animate-spin" />
          </div>
        ) : isError ? (
          <p className="text-sm text-[color:var(--danger-500)]">Erro ao carregar procedimentos.</p>
        ) : (
          <>
            <div className="max-h-[400px] overflow-auto rounded-xl border border-[color:var(--border-200)]">
              <table className="w-full min-w-[560px] border-collapse">
                <thead className="bg-[color:var(--surface-2)] text-left text-sm text-[color:var(--text-700)]">
                  <tr>
                    <th className="px-3 py-2.5 font-semibold">Nome</th>
                    <th className="px-3 py-2.5 font-semibold">Atendimento</th>
                    <th className="px-3 py-2.5 font-semibold">Valor (R$)</th>
                    <th className="w-20 px-3 py-2.5" />
                  </tr>
                </thead>
                <tbody>
                  {data?.items.map((item) => (
                    <tr key={item.id} className="border-t border-[color:var(--border-200)] text-sm text-[color:var(--text-700)]">
                      <td className="px-3 py-2.5 font-medium text-[color:var(--text-900)]">{item.nome}</td>
                      <td className="px-3 py-2.5">{atendimentoLabel(item.atendimentoId)}</td>
                      <td className="px-3 py-2.5">{item.valor.toFixed(2)}</td>
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

      <Sheet open={sheetOpen} onClose={() => setSheetOpen(false)} title={sheetMode === "create" ? "Novo procedimento" : sheetMode === "edit" ? "Editar procedimento" : "Dados do procedimento"}>
        {sheetMode === "view" && viewData ? (
          <div className="space-y-4">
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Nome</p><p className="text-sm text-[color:var(--text-900)]">{viewData.nome}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Atendimento</p><p className="text-sm text-[color:var(--text-900)]">{atendimentoLabel(viewData.atendimentoId)}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Valor</p><p className="text-sm text-[color:var(--text-900)]">R$ {viewData.valor.toFixed(2)}</p></div>
          </div>
        ) : (
          <ProcedimentoForm
            mode={sheetMode as "create" | "edit"}
            defaultValues={sheetMode === "edit" ? viewData : undefined}
            atendimentos={atendimentos}
            onSubmit={(d) => sheetMode === "create" ? handleCreate(d as CriarProcedimentoRequest) : handleUpdate(d as AtualizarProcedimentoRequest)}
            loading={criar.isPending || atualizar.isPending}
          />
        )}
      </Sheet>

      <Dialog
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Excluir procedimento"
        description={`Tem certeza que deseja excluir ${deleteTarget?.nome}?`}
        confirmLabel="Excluir"
        variant="danger"
        loading={excluir.isPending}
      />
    </div>
  );
}

function ProcedimentoForm({
  mode,
  defaultValues,
  atendimentos,
  onSubmit,
  loading,
}: {
  mode: "create" | "edit";
  defaultValues?: Procedimento;
  atendimentos: Atendimento[];
  onSubmit: (data: CriarProcedimentoRequest | AtualizarProcedimentoRequest) => void;
  loading: boolean;
}) {
  const [atendimentoId, setAtendimentoId] = useState(defaultValues?.atendimentoId ?? (atendimentos[0]?.id ?? 0));
  const [nome, setNome] = useState(defaultValues?.nome ?? "");
  const [valor, setValor] = useState(defaultValues?.valor ?? 0);

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (mode === "create") {
      onSubmit({ atendimentoId, nome, valor } as CriarProcedimentoRequest);
    } else {
      onSubmit({ nome, valor } as AtualizarProcedimentoRequest);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Atendimento</label>
        <select value={atendimentoId} onChange={(e) => setAtendimentoId(Number(e.target.value))} required disabled={mode === "edit"} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)] disabled:opacity-50">
          {atendimentos.map((a) => (
            <option key={a.id} value={a.id}>#{a.id} · {new Date(a.dataAtendimento).toLocaleDateString("pt-BR")}</option>
          ))}
        </select>
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Nome</label>
        <input type="text" value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={200} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Valor (R$)</label>
        <input type="number" step="0.01" min="0.01" value={valor} onChange={(e) => setValor(Number(e.target.value))} required className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div className="flex justify-end gap-3 pt-2">
        <Button type="submit" disabled={loading}>{loading ? "Salvando..." : "Salvar"}</Button>
      </div>
    </form>
  );
}

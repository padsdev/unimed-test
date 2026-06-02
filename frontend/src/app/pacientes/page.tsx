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
import { useListPacientes, useCriarPaciente, useAtualizarPaciente, useExcluirPaciente, usePaciente } from "@/hooks/use-pacientes";
import type { CriarPacienteRequest, AtualizarPacienteRequest, Paciente } from "@/types/domain";

type SheetMode = "create" | "edit" | "view";

export default function PacientesPage() {
  const [page, setPage] = useState(0);
  const [q, setQ] = useState("");
  const [sheetOpen, setSheetOpen] = useState(false);
  const [sheetMode, setSheetMode] = useState<SheetMode>("create");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Paciente | null>(null);

  const { data, isLoading, isError } = useListPacientes({ page, size: 10, q: q || undefined });
  const { data: viewData } = usePaciente(selectedId);
  const criar = useCriarPaciente();
  const atualizar = useAtualizarPaciente();
  const excluir = useExcluirPaciente();
  const { addToast } = useToast();

  function openSheet(mode: SheetMode, id?: number) {
    setSheetMode(mode);
    setSelectedId(id ?? null);
    setSheetOpen(true);
  }

  function handleCreate(data: CriarPacienteRequest) {
    criar.mutate(data, {
      onSuccess: () => { addToast({ type: "success", title: "Paciente cadastrado" }); setSheetOpen(false); },
      onError: () => { addToast({ type: "error", title: "Erro ao cadastrar" }); },
    });
  }

  function handleUpdate(data: AtualizarPacienteRequest) {
    if (!selectedId) return;
    atualizar.mutate({ id: selectedId, data }, {
      onSuccess: () => { addToast({ type: "success", title: "Paciente atualizado" }); setSheetOpen(false); },
      onError: () => { addToast({ type: "error", title: "Erro ao atualizar" }); },
    });
  }

  function handleDelete() {
    if (!deleteTarget) return;
    excluir.mutate(deleteTarget.id, {
      onSuccess: () => { addToast({ type: "success", title: "Paciente excluído" }); setDeleteTarget(null); },
      onError: () => { addToast({ type: "error", title: "Erro ao excluir", description: "Paciente pode ter vínculos ativos." }); },
    });
  }

  return (
    <div className="flex h-full min-h-0 flex-col gap-4">
      <SectionFrame title="Pacientes" description="Cadastro assistencial com busca, paginação e operações de CRUD.">
        <div className="mb-4 flex items-center gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-[color:var(--text-400)]" />
            <input
              type="text"
              value={q}
              onChange={(e) => { setQ(e.target.value); setPage(0); }}
              placeholder="Buscar por nome ou CPF..."
              className="w-full rounded-lg border border-border bg-[color:var(--surface-1)] py-2 pl-9 pr-3 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]"
            />
          </div>
          <Button onClick={() => openSheet("create")}>Novo paciente</Button>
        </div>

        {isLoading ? (
          <div className="flex min-h-[200px] items-center justify-center text-[color:var(--text-500)]">
            <LoaderCircle className="size-5 animate-spin" />
          </div>
        ) : isError ? (
          <p className="text-sm text-[color:var(--danger-500)]">Erro ao carregar pacientes.</p>
        ) : (
          <>
            <div className="max-h-[400px] overflow-auto rounded-xl border border-[color:var(--border-200)]">
              <table className="w-full min-w-[640px] border-collapse">
                <thead className="bg-[color:var(--surface-2)] text-left text-sm text-[color:var(--text-700)]">
                  <tr>
                    <th className="px-3 py-2.5 font-semibold">Nome</th>
                    <th className="px-3 py-2.5 font-semibold">CPF</th>
                    <th className="px-3 py-2.5 font-semibold">Telefone</th>
                    <th className="px-3 py-2.5 font-semibold">Email</th>
                    <th className="px-3 py-2.5 font-semibold">Status</th>
                    <th className="w-20 px-3 py-2.5" />
                  </tr>
                </thead>
                <tbody>
                  {data?.items.map((item) => (
                    <tr key={item.id} className="border-t border-[color:var(--border-200)] text-sm text-[color:var(--text-700)]">
                      <td className="px-3 py-2.5 font-medium text-[color:var(--text-900)]">{item.nome}</td>
                      <td className="px-3 py-2.5">{item.cpf}</td>
                      <td className="px-3 py-2.5">{item.telefone}</td>
                      <td className="px-3 py-2.5">{item.email}</td>
                      <td className="px-3 py-2.5">
                        <span className={`inline-block rounded-full px-2 py-0.5 text-xs font-medium ${
                          item.status === "ativo" ? "bg-[color:var(--success-500)]/10 text-[color:var(--success-500)]" : "bg-[color:var(--danger-500)]/10 text-[color:var(--danger-500)]"
                        }`}>
                          {item.status}
                        </span>
                      </td>
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

      <Sheet open={sheetOpen} onClose={() => setSheetOpen(false)} title={sheetMode === "create" ? "Novo paciente" : sheetMode === "edit" ? "Editar paciente" : "Dados do paciente"}>
        {sheetMode === "view" && viewData ? (
          <div className="space-y-4">
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Nome</p><p className="text-sm text-[color:var(--text-900)]">{viewData.nome}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">CPF</p><p className="text-sm text-[color:var(--text-900)]">{viewData.cpf}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Nascimento</p><p className="text-sm text-[color:var(--text-900)]">{viewData.dataNascimento}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Telefone</p><p className="text-sm text-[color:var(--text-900)]">{viewData.telefone}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Email</p><p className="text-sm text-[color:var(--text-900)]">{viewData.email}</p></div>
            <div><p className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Status</p><p className="text-sm text-[color:var(--text-900)]">{viewData.status}</p></div>
          </div>
        ) : (
          <PacienteForm
            mode={sheetMode as "create" | "edit"}
            defaultValues={sheetMode === "edit" ? viewData : undefined}
            onSubmit={(d) => sheetMode === "create" ? handleCreate(d as CriarPacienteRequest) : handleUpdate(d as AtualizarPacienteRequest)}
            loading={criar.isPending || atualizar.isPending}
          />
        )}
      </Sheet>

      <Dialog
        open={deleteTarget !== null}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        title="Excluir paciente"
        description={`Tem certeza que deseja excluir ${deleteTarget?.nome}? Esta ação não pode ser desfeita.`}
        confirmLabel="Excluir"
        variant="danger"
        loading={excluir.isPending}
      />
    </div>
  );
}

function PacienteForm({
  mode,
  defaultValues,
  onSubmit,
  loading,
}: {
  mode: "create" | "edit";
  defaultValues?: Paciente;
  onSubmit: (data: CriarPacienteRequest | AtualizarPacienteRequest) => void;
  loading: boolean;
}) {
  const [nome, setNome] = useState(defaultValues?.nome ?? "");
  const [cpf, setCpf] = useState(defaultValues?.cpf ?? "");
  const [dataNascimento, setDataNascimento] = useState(defaultValues?.dataNascimento ?? "");
  const [telefone, setTelefone] = useState(defaultValues?.telefone ?? "");
  const [email, setEmail] = useState(defaultValues?.email ?? "");
  const [status, setStatus] = useState<"ativo" | "inativo">(defaultValues?.status ?? "ativo");

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (mode === "create") {
      onSubmit({ nome, cpf, dataNascimento, telefone, email } as CriarPacienteRequest);
    } else {
      onSubmit({ nome, dataNascimento, telefone, email, status } as AtualizarPacienteRequest);
    }
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Nome</label>
        <input type="text" value={nome} onChange={(e) => setNome(e.target.value)} required maxLength={150} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">CPF</label>
        <input type="text" value={cpf} onChange={(e) => setCpf(e.target.value)} required={mode === "create"} disabled={mode === "edit"} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)] disabled:opacity-50" placeholder="000.000.000-00" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Nascimento</label>
        <input type="date" value={dataNascimento} onChange={(e) => setDataNascimento(e.target.value)} required className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Telefone</label>
        <input type="text" value={telefone} onChange={(e) => setTelefone(e.target.value)} required className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" placeholder="(11) 98888-1111" />
      </div>
      <div>
        <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Email</label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]" />
      </div>
      {mode === "edit" && (
        <div>
          <label className="text-xs font-semibold uppercase text-[color:var(--text-500)]">Status</label>
          <select value={status} onChange={(e) => setStatus(e.target.value as "ativo" | "inativo")} className="mt-1 w-full rounded-lg border border-border bg-[color:var(--surface-1)] px-3 py-2 text-sm text-[color:var(--text-700)] outline-none focus:border-[color:var(--brand-500)]">
            <option value="ativo">Ativo</option>
            <option value="inativo">Inativo</option>
          </select>
        </div>
      )}
      <div className="flex justify-end gap-3 pt-2">
        <Button type="submit" disabled={loading}>{loading ? "Salvando..." : "Salvar"}</Button>
      </div>
    </form>
  );
}
